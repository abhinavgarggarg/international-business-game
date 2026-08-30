package com.soprasteria.ib.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soprasteria.ib.data.GameRepository
import com.soprasteria.ib.engine.BoardFactory
import com.soprasteria.ib.engine.BoardSpace
import com.soprasteria.ib.engine.DiceRoll
import com.soprasteria.ib.engine.GameEngine
import com.soprasteria.ib.engine.Player
import com.soprasteria.ib.engine.Property
import com.soprasteria.ib.engine.SpaceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Prompt currently shown to the player, if any. Drives which dialog is visible. */
sealed class PendingPrompt {
    data object None : PendingPrompt()
    data class OfferToBuy(val property: Property) : PendingPrompt()
    data class RentPaid(val payerName: String, val ownerName: String, val amount: Int, val propertyName: String) : PendingPrompt()
    data class Bankrupted(val playerName: String) : PendingPrompt()
    data class Winner(val playerName: String) : PendingPrompt()
}

data class GameUiState(
    val board: List<BoardSpace> = emptyList(),
    val properties: Map<Int, Property> = emptyMap(),
    val players: List<Player> = emptyList(),
    val currentPlayerId: Int = -1,
    val lastRoll: DiceRoll? = null,
    val prompt: PendingPrompt = PendingPrompt.None,
    val isPropertyDrawerOpen: Boolean = false
)

class GameViewModel(
    private val repository: GameRepository?,
    playerNames: List<String> = listOf("Player 1", "Player 2")
) : ViewModel() {

    private val boardAndProperties = BoardFactory.buildBoard()
    private val board = boardAndProperties.first
    private val properties = boardAndProperties.second
    private val players = playerNames.mapIndexed { i, name -> Player(id = i + 1, name = name) }.toMutableList()
    private val engine = GameEngine(board, properties, players)

    private val _uiState = MutableStateFlow(freshState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val resumed = repository?.tryResume(engine) ?: false
            if (resumed) refreshState()
        }
    }

    private fun freshState() = GameUiState(
        board = board,
        properties = engine.properties.toMap(),
        players = engine.players.toList(),
        currentPlayerId = engine.currentPlayer().id
    )

    private fun refreshState(lastRoll: DiceRoll? = _uiState.value.lastRoll, prompt: PendingPrompt = PendingPrompt.None) {
        _uiState.value = _uiState.value.copy(
            properties = engine.properties.toMap(),
            players = engine.players.toList(),
            currentPlayerId = engine.currentPlayer().id,
            lastRoll = lastRoll,
            prompt = prompt
        )
    }

    fun rollDiceAndMove() {
        val player = engine.currentPlayer()
        val roll = engine.rollDice()
        engine.movePlayer(player, roll.total)
        val events = engine.drainEvents()

        val space = board[player.position]
        val prompt: PendingPrompt = when {
            events.any { it is com.soprasteria.ib.engine.TurnEvent.Bankrupted } ->
                PendingPrompt.Bankrupted(player.name)
            events.any { it is com.soprasteria.ib.engine.TurnEvent.GameWon } ->
                PendingPrompt.Winner(engine.players.first { it.id == engine.gameWinner }.name)
            space.type == SpaceType.PROPERTY || space.type == SpaceType.AIRLINE -> {
                val prop = engine.properties[space.propertyId]
                if (prop != null && prop.ownerId == null && player.balance >= prop.cost) {
                    PendingPrompt.OfferToBuy(prop)
                } else if (prop != null && prop.ownerId != null && prop.ownerId != player.id) {
                    val owner = engine.players.first { it.id == prop.ownerId }
                    val rentEvent = events.filterIsInstance<com.soprasteria.ib.engine.TurnEvent.RentCharged>().firstOrNull()
                    PendingPrompt.RentPaid(player.name, owner.name, rentEvent?.amount ?: 0, prop.name)
                } else PendingPrompt.None
            }
            else -> PendingPrompt.None
        }

        refreshState(lastRoll = roll, prompt = prompt)
        persist()
    }

    fun buyCurrentProperty(propertyId: Int) {
        val player = engine.currentPlayer()
        engine.buyProperty(player, propertyId)
        refreshState(prompt = PendingPrompt.None)
        persist()
    }

    fun dismissPrompt() {
        refreshState(prompt = PendingPrompt.None)
    }

    fun endTurn() {
        engine.nextTurn()
        refreshState(lastRoll = null, prompt = PendingPrompt.None)
        persist()
    }

    fun togglePropertyDrawer(open: Boolean) {
        _uiState.value = _uiState.value.copy(isPropertyDrawerOpen = open)
    }

    private fun persist() {
        val repo = repository ?: return
        viewModelScope.launch { repo.autoSave(engine) }
    }
}
