package org.eski.shadyback.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun GameScreen(
    vm: GameScreenViewModel = viewModel { GameScreenViewModel() },
) {
    val nBack by vm.nBack.collectAsState()
    val selectedBaseColor by vm.selectedBaseColor.collectAsState()
    val colorSequence by vm.colorSequence.collectAsState()
    val currentPosition by vm.currentPosition.collectAsState()
    val score by vm.score.collectAsState()
    val gameStarted by vm.gameStarted.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Settings section
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("N-Back: $nBack")
            Button(onClick = { vm.increaseNBack() }) { Text("+") }
            Button(onClick = { vm.decreaseNBack() }) { Text("-") }

            Text("Color: $selectedBaseColor")
            Button(onClick = { vm.changeBaseColor() }) { Text("Change") }
        }

        // Game display
        if (colorSequence.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(colorSequence.last())
            )
        }

        // Score display
        Text("Score: $score", fontSize = 24.sp)

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { vm.advanceGame() },
                enabled = !gameStarted || currentPosition < 20
            ) {
                Text(if (!gameStarted) "Start" else "Next")
            }

            if (gameStarted) {
                Button(onClick = { vm.checkAnswer(true) }) {
                    Text("Match")
                }
                Button(onClick = { vm.checkAnswer(false) }) {
                    Text("No Match")
                }
            }
        }
    }
}