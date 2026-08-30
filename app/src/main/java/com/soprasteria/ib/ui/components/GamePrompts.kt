package com.soprasteria.ib.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.soprasteria.ib.engine.Property

@Composable
fun BuyPropertyDialog(property: Property, currentBalance: Int, onBuy: () -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSkip,
        title = { Text("Buy ${property.name}?") },
        text = { Text("Cost: $${property.cost}\nYour balance: $$currentBalance") },
        confirmButton = { TextButton(onClick = onBuy) { Text("Buy it!") } },
        dismissButton = { TextButton(onClick = onSkip) { Text("Skip") } }
    )
}

@Composable
fun RentPaidDialog(payerName: String, ownerName: String, amount: Int, propertyName: String, onAcknowledge: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAcknowledge,
        title = { Text("Rent paid!") },
        text = { Text("$payerName landed on $propertyName and paid $$amount to $ownerName.") },
        confirmButton = { TextButton(onClick = onAcknowledge) { Text("OK") } }
    )
}

@Composable
fun BankruptDialog(playerName: String, onAcknowledge: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAcknowledge,
        title = { Text("Out of the game") },
        text = { Text("$playerName ran out of money and is out of the game. Their properties go back to the bank.") },
        confirmButton = { TextButton(onClick = onAcknowledge) { Text("OK") } }
    )
}

@Composable
fun WinnerDialog(playerName: String, onAcknowledge: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAcknowledge,
        title = { Text("🎉 $playerName wins!") },
        text = { Text("Everyone else is out of the game. Great job, $playerName!") },
        confirmButton = { TextButton(onClick = onAcknowledge) { Text("Nice!") } }
    )
}
