package org.eski.menoback.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.eski.menoback.ui.utils.grid2

@Composable
fun GameScreen(
    vm: GameScreenViewModel = viewModel()
) {
    val gameState by vm.gameState.collectAsState()

    KeyboardInput(vm)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.DarkGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(grid2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeader(
                score = vm.score,
                multiplier = vm.multiplier,
                nBackLevel = vm.nBackLevel,
                nBackStreak = vm.nBackStreak
            )

            Spacer(modifier = Modifier.height(grid2))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                GameBoard(
                    board = vm.board,
                    currentTetrimino = vm.currentTetrimino,
                    currentPiecePosition = vm.currentPiecePosition,
                    modifier = Modifier.fillMaxHeight()
                )

                Spacer(modifier = Modifier.width(16.dp))

                GameSidebar(vm)

            }

            GameStatus(
                gameState = gameState,
                onStartClicked = { vm.startGame() },
                onResumeClicked = { vm.resumeGame() },
                onPauseClicked = { vm.pauseGame() },
                onResetClicked = { vm.resetGame() }
            )
        }
    }
}

