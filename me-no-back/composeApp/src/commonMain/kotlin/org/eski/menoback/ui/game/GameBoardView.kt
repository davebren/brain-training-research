package org.eski.menoback.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.eski.menoback.model.Board


@Composable
fun GameBoard(
  vm: GameScreenViewModel,
  modifier: Modifier = Modifier
) {
  val displayBoard: Board by vm.displayBoard.collectAsState()

  Box(
    modifier = modifier
      .aspectRatio(0.5f)
      .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
      .padding(2.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // Draw each row of the game board
      displayBoard.matrix.forEach { row ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          // Draw each cell in the row
          row.forEach { cell ->
            Box(
              modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(1.dp)
                .background(
                  when (cell) {
                    0 -> Color.LightGray.copy(alpha = 0.2f)
                    1 -> Color.Cyan
                    2 -> Color.Yellow
                    3 -> Color.Magenta
                    4 -> Color.Blue
                    5 -> Color.Red
                    6 -> Color.Green
                    7 -> Color.Red
                    else -> Color.Gray
                  }
                )
            )
          }
        }
      }
    }
  }
}