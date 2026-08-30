package com.soprasteria.ib.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soprasteria.ib.engine.Player

private val avatarPalette = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835),
    Color(0xFF8E24AA), Color(0xFFFF8A00)
)

@Composable
fun WalletBar(players: List<Player>, currentPlayerId: Int, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
    ) {
        items(players) { player ->
            val index = players.indexOf(player)
            WalletChip(
                player = player,
                color = avatarPalette[index % avatarPalette.size],
                isActive = player.id == currentPlayerId
            )
        }
    }
}

@Composable
private fun WalletChip(player: Player, color: Color, isActive: Boolean) {
    val animatedBalance by animateIntAsState(
        targetValue = player.balance,
        animationSpec = tween(durationMillis = 600),
        label = "balance-${player.id}"
    )
    val borderWidth = if (isActive) 3.dp else 1.dp

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .border(borderWidth, if (isActive) color else Color.LightGray, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(28.dp)) {}
            androidx.compose.foundation.layout.Column {
                Text(text = player.name, style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(text = "$${formatMoney(animatedBalance)}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

private fun formatMoney(amount: Int): String {
    val s = amount.toString()
    val sb = StringBuilder()
    for ((i, c) in s.reversed().withIndex()) {
        if (i != 0 && i % 3 == 0) sb.append(',')
        sb.append(c)
    }
    return sb.reverse().toString()
}
