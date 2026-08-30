package com.soprasteria.ib.engine

import kotlin.random.Random

class GameEngine(
    val board: List<BoardSpace>,
    val properties: MutableMap<Int, Property>,
    val players: MutableList<Player>,
    private val random: Random = Random.Default
) {
    var currentTurnIndex: Int = 0
        private set
    var bankBalance: Int = 1_000_000
        private set
    var gameWinner: Int? = null
        private set

    private val events = mutableListOf<TurnEvent>()
    fun drainEvents(): List<TurnEvent> {
        val copy = events.toList()
        events.clear()
        return copy
    }

    val boardSize get() = board.size

    fun rollDice(): DiceRoll {
        val d1 = random.nextInt(1, 7)
        val d2 = random.nextInt(1, 7)
        return DiceRoll(d1, d2)
    }

    fun currentPlayer(): Player = activePlayers()[currentTurnIndex % activePlayers().size]

    private fun activePlayers(): List<Player> = players.filter { !it.isBankrupt }

    /**
     * Moves a player by [steps], correctly looping around the board.
     * Awards "Pass Start" money exactly once per lap, even if steps > boardSize.
     */
    fun movePlayer(player: Player, steps: Int) {
        require(steps >= 0)
        val laps = (player.position + steps) / boardSize
        val newPosition = (player.position + steps) % boardSize
        player.position = newPosition
        if (laps > 0) {
            val amount = laps * 2000 // Pass Start bonus
            player.balance += amount
            events += TurnEvent.PassedStart(player.id, amount)
        }
        evaluateSpace(player)
    }

    private fun evaluateSpace(player: Player) {
        val space = board[player.position]
        when (space.type) {
            SpaceType.PROPERTY, SpaceType.AIRLINE -> {
                val prop = properties[space.propertyId] ?: return
                if (prop.ownerId == null) {
                    events += TurnEvent.LandedOnUnownedProperty(player.id, prop.id)
                } else if (prop.ownerId != player.id) {
                    val ownerPlayer = players.first { it.id == prop.ownerId }
                    chargeRent(player, ownerPlayer, prop)
                }
            }
            SpaceType.TAX -> chargeTax(player, space.taxAmount)
            SpaceType.GO_TO_JAIL -> sendToJail(player)
            else -> { /* START, CHANCE, SURPRISE, JAIL(visiting), FREE_PARKING: no forced money change here */ }
        }
    }

    fun calculateRent(prop: Property): Int {
        val ownerId = prop.ownerId ?: return 0
        if (prop.houses in 1..5) {
            return prop.rentTiers[prop.houses]
        }
        val ownsFullSet = ownsColorGroup(ownerId, prop.colorGroup)
        return if (ownsFullSet) prop.rentTiers[0] * 2 else prop.rentTiers[0]
    }

    fun ownsColorGroup(playerId: Int, colorGroup: String): Boolean {
        val groupProps = properties.values.filter { it.colorGroup == colorGroup }
        if (groupProps.isEmpty()) return false
        return groupProps.all { it.ownerId == playerId }
    }

    private fun chargeRent(payer: Player, owner: Player, prop: Property) {
        val amount = calculateRent(prop)
        events += TurnEvent.RentCharged(payer.id, owner.id, amount, prop.id)
        transferOrBankrupt(payer, amount, owner)
    }

    private fun chargeTax(player: Player, amount: Int) {
        events += TurnEvent.TaxCharged(player.id, amount)
        transferOrBankrupt(player, amount, null)
    }

    /**
     * Deducts [amount] from [payer]. If they can't cover it (even after
     * conceptually liquidating - simplified here to cash-on-hand),
     * they are declared bankrupt and their properties are released.
     */
    private fun transferOrBankrupt(payer: Player, amount: Int, creditor: Player?) {
        if (payer.balance >= amount) {
            payer.balance -= amount
            if (creditor != null) creditor.balance += amount else bankBalance += amount
        } else {
            declareBankrupt(payer, creditor)
        }
    }

    fun declareBankrupt(player: Player, creditor: Player?) {
        if (player.isBankrupt) return
        player.isBankrupt = true
        // Release all properties back to the bank (or, in the simplified
        // model, whichever entity forced the bankruptcy).
        for (propId in player.ownedProperties.toList()) {
            val prop = properties[propId] ?: continue
            prop.ownerId = null
            prop.houses = 0
        }
        player.ownedProperties.clear()
        player.balance = 0
        events += TurnEvent.Bankrupted(player.id, creditor?.id)
        checkWinCondition()
    }

    private fun checkWinCondition() {
        val remaining = activePlayers()
        if (remaining.size == 1) {
            gameWinner = remaining.first().id
            events += TurnEvent.GameWon(remaining.first().id)
        }
    }

    fun sendToJail(player: Player) {
        player.inJail = true
        player.jailTurns = 0
        // Standard board jail index assumed to be wherever SpaceType.JAIL is
        val jailIndex = board.indexOfFirst { it.type == SpaceType.JAIL }
        if (jailIndex >= 0) player.position = jailIndex
        events += TurnEvent.SentToJail(player.id)
    }

    fun buyProperty(player: Player, propertyId: Int): Boolean {
        val prop = properties[propertyId] ?: return false
        if (prop.ownerId != null) return false
        if (player.balance < prop.cost) return false
        player.balance -= prop.cost
        bankBalance += prop.cost
        prop.ownerId = player.id
        player.ownedProperties.add(propertyId)
        return true
    }

    /**
     * Restores engine-level metadata (turn index, bank balance, winner) when
     * resuming a saved game. Player and property state are restored
     * separately since they're mutable objects the engine already holds
     * references to.
     */
    fun restoreMeta(turnIndex: Int, bank: Int, winner: Int?) {
        currentTurnIndex = turnIndex
        bankBalance = bank
        gameWinner = winner
    }

    /** Advances currentTurnIndex to the next non-bankrupt player. */
    fun nextTurn() {
        if (gameWinner != null) return
        val n = activePlayers().size
        if (n == 0) return
        currentTurnIndex = (currentTurnIndex + 1) % n
    }
}
