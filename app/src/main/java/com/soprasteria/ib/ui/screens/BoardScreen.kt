package com.soprasteria.ib.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprasteria.ib.engine.BoardSpace
import com.soprasteria.ib.engine.Player
import com.soprasteria.ib.engine.Property
import com.soprasteria.ib.engine.SpaceType
import com.soprasteria.ib.ui.theme.BoardFelt
import com.soprasteria.ib.ui.theme.ColorGroupSwatches

/** Grid coordinates (row, col) in an 11x11 perimeter for a 40-space board. */
private fun gridPosition(index: Int): Pair<Int, Int> = when {
    index <= 10 -> 10 to (10 - index)
    index <= 20 -> (20 - index) to 0
    index <= 30 -> 0 to (index - 20)
    else -> (index - 30) to 10
}

private val tokenColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835),
    Color(0xFF8E24AA), Color(0xFFFF8A00)
)

@Composable
fun BoardScreen(
    board: List<BoardSpace>,
    properties: Map<Int, Property>,
    players: List<Player>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .size(360.dp)
            .background(BoardFelt, RoundedCornerShape(12.dp))
    ) {
        val cellSize: Dp = maxWidth / 11f

        for (space in board) {
            val (row, col) = gridPosition(space.index)
            BoardCell(
                space = space,
                property = space.propertyId?.let { properties[it] },
                cellSize = cellSize,
                modifier = Modifier.offset(x = cellSize * col, y = cellSize * row)
            )
        }

        // Player tokens: small colored dots stacked in the cell they're standing on.
        players.forEachIndexed { i, player ->
            val (row, col) = gridPosition(player.position)
            Surface(
                shape = CircleShape,
                color = tokenColors[i % tokenColors.size],
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .offset(
                        x = cellSize * col + cellSize * 0.15f + (i * 8).dp,
                        y = cellSize * row + cellSize * 0.6f
                    )
                    .size(cellSize * 0.3f)
            ) {}
        }
    }
}

@Composable
private fun BoardCell(space: BoardSpace, property: Property?, cellSize: Dp, modifier: Modifier = Modifier) {
    val swatch = when {
        property != null -> ColorGroupSwatches[property.colorGroup] ?: Color.Gray
        space.type == SpaceType.TAX -> Color(0xFFB0BEC5)
        space.type == SpaceType.CHANCE -> Color(0xFFFFF176)
        space.type == SpaceType.SURPRISE -> Color(0xFFCE93D8)
        space.type == SpaceType.JAIL -> Color(0xFF90A4AE)
        space.type == SpaceType.GO_TO_JAIL -> Color(0xFFEF9A9A)
        space.type == SpaceType.FREE_PARKING -> Color(0xFFA5D6A7)
        space.type == SpaceType.START -> Color(0xFFFFE082)
        else -> Color.White
    }
    Surface(
        color = swatch,
        border = BorderStroke(0.5.dp, Color.DarkGray),
        modifier = modifier.size(cellSize)
    ) {
        Column {
            Text(
                text = space.name,
                fontSize = 6.sp,
                maxLines = 3,
                color = Color.Black
            )
        }
    }
}
