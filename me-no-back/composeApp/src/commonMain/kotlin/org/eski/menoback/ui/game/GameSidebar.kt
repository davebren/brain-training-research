package org.eski.menoback.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.eski.menoback.model.Tetrimino
import org.eski.menoback.ui.utils.grid
import org.eski.menoback.ui.utils.grid2


@Composable
fun RowScope.GameSidebar(vm: GameScreenViewModel) {
  Column(
    modifier = Modifier.weight(0.3f),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Next piece preview
    NextPiecePreview(
      nextTetrimino = vm.nextTetrimino,
      modifier = Modifier
        .width(120.dp)
        .height(120.dp)
    )

    Spacer(modifier = Modifier.height(grid2))



    Spacer(modifier = Modifier.height(16.dp))

    // Game controls
    GameControls(vm)
  }
}

@Composable
fun NextPiecePreview(
  nextTetrimino: Tetrimino?,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Next Piece",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = Color.LightGray
    )

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .width(104.dp)
        .height(104.dp)
        .border(2.dp, Color.LightGray, RoundedCornerShape(4.dp))
        .padding(grid),
      contentAlignment = Alignment.Center
    ) {
      if (nextTetrimino != null) {
        val pieceRows = nextTetrimino.shape.size
        val pieceCols = nextTetrimino.shape[0].size

        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          for (row in 0 until pieceRows) {
            Row {
              for (col in 0 until pieceCols) {
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .padding(1.dp)
                    .background(
                      if (nextTetrimino.shape[row][col] != 0) {
                        when (nextTetrimino.type) {
                          1 -> Color.Cyan
                          2 -> Color.Yellow
                          3 -> Color.Magenta
                          4 -> Color.Blue
                          5 -> Color.Red
                          6 -> Color.Green
                          7 -> Color.Red
                          else -> Color.Gray
                        }
                      } else {
                        Color.Transparent
                      }
                    )
                )
              }
            }
          }
        }
      }
    }
  }
}

