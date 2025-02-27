package org.eski.menoback.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameStatus(
  modifier: Modifier = Modifier,
  gameState: GameState,
  onStartClicked: () -> Unit,
  onResumeClicked: () -> Unit,
  onPauseClicked: () -> Unit,
  onResetClicked: () -> Unit
) {
  Column(
    modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Display game state
    Text(
      text = when (gameState) {
        GameState.NotStarted -> "Press Start to begin"
        GameState.Running -> "Game in progress"
        GameState.Paused -> "Game paused"
        GameState.GameOver -> "Game Over"
      },
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Game control buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      when (gameState) {
        GameState.NotStarted -> {
          Button(
            onClick = onStartClicked,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
          ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start")
          }
        }
        GameState.Running -> {
          Button(
            onClick = onPauseClicked
          ) {
            Icon(Icons.Filled.Close, contentDescription = "Pause")
//                        Icon(Icons.Filled.Pause, contentDescription = "Pause")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pause")
          }
        }
        GameState.Paused -> {
          Button(
            onClick = onResumeClicked,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
          ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Resume")
          }
        }
        GameState.GameOver -> {
          Button(
            onClick = onStartClicked,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
          ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Play Again")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play Again")
          }
        }
      }

      // Reset button (always available)
      Button(
        onClick = onResetClicked,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
      ) {
        Icon(Icons.Filled.Close, contentDescription = "Reset")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Reset")
      }
    }
  }
}