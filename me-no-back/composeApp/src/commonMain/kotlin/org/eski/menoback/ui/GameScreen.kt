package org.eski.menoback.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(
    vm: GameScreenViewModel = viewModel()
) {
    val gameState by vm.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game header with title and score
        GameHeader(
            score = vm.score,
            multiplier = vm.multiplier,
            nBackLevel = vm.nBackLevel,
            nBackStreak = vm.nBackStreak
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main game area with board and controls
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            // Game board
            GameBoard(
                board = vm.board,
                currentPiece = vm.currentPiece,
                currentPiecePosition = vm.currentPiecePosition,
                modifier = Modifier.weight(0.7f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Game info and controls
            Column(
                modifier = Modifier.weight(0.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Next piece preview
                NextPiecePreview(
                    nextPiece = vm.nextPiece,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // N-Back controls
                NBackControls(
                    onMatchClicked = { vm.nBackMatch() },
                    onNoMatchClicked = { vm.nBackNoMatch() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Game controls
                GameControls(
                    onMoveLeftClicked = { vm.movePieceLeft() },
                    onMoveRightClicked = { vm.movePieceRight() },
                    onRotateClicked = { vm.rotatePiece() },
                    onDropClicked = { vm.dropPiece() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game status and control buttons
        GameStatus(
            gameState = gameState,
            onStartClicked = { vm.startGame() },
            onResumeClicked = { vm.resumeGame() },
            onPauseClicked = { vm.pauseGame() },
            onResetClicked = { vm.resetGame() }
        )
    }
}

@Composable
fun GameHeader(
    score: Int,
    multiplier: Float,
    nBackLevel: Int,
    nBackStreak: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MeNoBack",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(label = "Score", value = score.toString())
            InfoItem(label = "Multiplier", value = "${multiplier}x")
            InfoItem(label = "$nBackLevel-Back", value = "Streak: $nBackStreak")
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GameBoard(
    board: Array<IntArray>,
    currentPiece: Piece?,
    currentPiecePosition: Position,
    modifier: Modifier = Modifier
) {
    // Create a mutable copy of the board that includes the current piece
    val displayBoard = remember(board, currentPiece, currentPiecePosition) {
        val copy = Array(board.size) { row -> IntArray(board[0].size) { col -> board[row][col] } }

        // Add current piece to the display board
        if (currentPiece != null) {
            for (row in currentPiece.shape.indices) {
                for (col in currentPiece.shape[row].indices) {
                    if (currentPiece.shape[row][col] != 0) {
                        val boardRow = currentPiecePosition.row + row
                        val boardCol = currentPiecePosition.col + col

                        if (boardRow >= 0 && boardRow < copy.size &&
                            boardCol >= 0 && boardCol < copy[0].size) {
                            copy[boardRow][boardCol] = currentPiece.type
                        }
                    }
                }
            }
        }

        copy
    }

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
            displayBoard.forEach { row ->
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

@Composable
fun NextPiecePreview(
    nextPiece: Piece?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Next Piece",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (nextPiece != null) {
                // Determine the size of the piece
                val pieceRows = nextPiece.shape.size
                val pieceCols = nextPiece.shape[0].size

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
                                            if (nextPiece.shape[row][col] != 0) {
                                                when (nextPiece.type) {
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

@Composable
fun NBackControls(
    onMatchClicked: () -> Unit,
    onNoMatchClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "N-Back Match?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onMatchClicked,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("Match")
            }

            Button(
                onClick = onNoMatchClicked,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("No Match")
            }
        }
    }
}

@Composable
fun GameControls(
    onMoveLeftClicked: () -> Unit,
    onMoveRightClicked: () -> Unit,
    onRotateClicked: () -> Unit,
    onDropClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Controls",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rotate button
        Button(
            onClick = onRotateClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Rotate")
//            Icon(Icons.Filled.RotateRight, contentDescription = "Rotate")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rotate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Left/Right buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onMoveLeftClicked,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Move Left")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onMoveRightClicked,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Move Right")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Drop button
        Button(
            onClick = onDropClicked,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
        ) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Drop")
//            Icon(Icons.Filled.ArrowDownward, contentDescription = "Drop")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Drop")
        }
    }
}

@Composable
fun GameStatus(
    gameState: GameState,
    onStartClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    Column(
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