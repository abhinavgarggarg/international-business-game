package com.soprasteria.ib.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soprasteria.ib.ui.GameViewModel
import com.soprasteria.ib.ui.PendingPrompt
import com.soprasteria.ib.ui.components.BankruptDialog
import com.soprasteria.ib.ui.components.BuyPropertyDialog
import com.soprasteria.ib.ui.components.DiceRoller
import com.soprasteria.ib.ui.components.PropertyDashboard
import com.soprasteria.ib.ui.components.RentPaidDialog
import com.soprasteria.ib.ui.components.WalletBar
import com.soprasteria.ib.ui.components.WinnerDialog

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    val currentPlayer = state.players.firstOrNull { it.id == state.currentPlayerId }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WalletBar(players = state.players, currentPlayerId = state.currentPlayerId)

            currentPlayer?.let {
                Text("${it.name}'s turn")
            }

            BoardScreen(board = state.board, properties = state.properties, players = state.players)

            DiceRoller(
                lastRoll = state.lastRoll,
                enabled = state.prompt == PendingPrompt.None,
                onRoll = { viewModel.rollDiceAndMove() }
            )

            Button(onClick = { viewModel.togglePropertyDrawer(true) }) {
                Text("My Properties")
            }

            Button(
                onClick = { viewModel.endTurn() },
                enabled = state.prompt == PendingPrompt.None && state.lastRoll != null
            ) {
                Text("End turn")
            }
        }
    }

    if (state.isPropertyDrawerOpen && currentPlayer != null) {
        val owned = currentPlayer.ownedProperties.mapNotNull { state.properties[it] }
        PropertyDashboard(
            ownerName = currentPlayer.name,
            ownedProperties = owned,
            onDismiss = { viewModel.togglePropertyDrawer(false) }
        )
    }

    when (val prompt = state.prompt) {
        is PendingPrompt.OfferToBuy -> BuyPropertyDialog(
            property = prompt.property,
            currentBalance = currentPlayer?.balance ?: 0,
            onBuy = { viewModel.buyCurrentProperty(prompt.property.id) },
            onSkip = { viewModel.dismissPrompt() }
        )
        is PendingPrompt.RentPaid -> RentPaidDialog(
            payerName = prompt.payerName,
            ownerName = prompt.ownerName,
            amount = prompt.amount,
            propertyName = prompt.propertyName,
            onAcknowledge = { viewModel.dismissPrompt() }
        )
        is PendingPrompt.Bankrupted -> BankruptDialog(
            playerName = prompt.playerName,
            onAcknowledge = { viewModel.dismissPrompt() }
        )
        is PendingPrompt.Winner -> WinnerDialog(
            playerName = prompt.playerName,
            onAcknowledge = { viewModel.dismissPrompt() }
        )
        PendingPrompt.None -> { /* nothing to show */ }
    }
}
