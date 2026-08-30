package com.soprasteria.ib.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soprasteria.ib.engine.DiceRoll

@Composable
fun DiceRoller(
    lastRoll: DiceRoll?,
    enabled: Boolean,
    onRoll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DieFace(value = lastRoll?.die1 ?: 1)
            DieFace(value = lastRoll?.die2 ?: 1)
        }
        Button(onClick = onRoll, enabled = enabled) {
            Text(if (lastRoll == null) "Roll the dice!" else "Roll again")
        }
    }
}

@Composable
private fun DieFace(value: Int) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(value) {
        rotation.snapTo(0f)
        rotation.animateTo(360f, animationSpec = tween(durationMillis = 450))
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .size(56.dp)
            .rotate(rotation.value)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium)
        }
    }
}
