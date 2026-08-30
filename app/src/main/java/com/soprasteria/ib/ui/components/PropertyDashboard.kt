package com.soprasteria.ib.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soprasteria.ib.engine.Property
import com.soprasteria.ib.ui.theme.ColorGroupSwatches

/**
 * A sliding panel (Material3 ModalBottomSheet) showing everything a player
 * owns, grouped by color set, with upgrade status and current rent.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PropertyDashboard(
    ownerName: String,
    ownedProperties: List<Property>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$ownerName's Portfolio", style = MaterialTheme.typography.headlineMedium)
            Text(
                "${ownedProperties.size} propert${if (ownedProperties.size == 1) "y" else "ies"} owned",
                style = MaterialTheme.typography.bodyLarge
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(6.dp))

            if (ownedProperties.isEmpty()) {
                Text("No properties yet — buy one when you land on it!", style = MaterialTheme.typography.bodyLarge)
            } else {
                val grouped = ownedProperties.groupBy { it.colorGroup }
                LazyColumn {
                    grouped.forEach { (color, props) ->
                        item {
                            Text(
                                text = color,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(props) { prop -> PropertyCardRow(prop) }
                        item { Divider() }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyCardRow(property: Property) {
    val swatch = ColorGroupSwatches[property.colorGroup] ?: MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(color = swatch, shape = RoundedCornerShape(6.dp), modifier = Modifier.size(20.dp)) {}
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(property.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = upgradeLabel(property.houses) + " • rent varies by tier",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun upgradeLabel(houses: Int): String = when (houses) {
    0 -> "No upgrades"
    5 -> "Hotel"
    else -> "$houses house${if (houses > 1) "s" else ""}"
}
