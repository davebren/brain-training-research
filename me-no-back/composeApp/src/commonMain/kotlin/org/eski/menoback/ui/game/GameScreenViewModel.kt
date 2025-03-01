package org.eski.menoback.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eski.menoback.model.Board
import org.eski.menoback.model.Tetrimino
import org.eski.menoback.model.boardHeight
import org.eski.menoback.model.boardWidth
import org.eski.menoback.model.newTetriminoStartPosition
import org.eski.menoback.ui.TetriminoColors
import org.eski.util.deepCopy
import kotlin.random.Random


const val nbackMatchBias = 0.15f
const val GAME_DURATION_SECONDS = 60

class GameScreenViewModel : ViewModel() {
  val tetriminoColors = MutableStateFlow(TetriminoColors())

  private val _gameState = MutableStateFlow<GameState>(GameState.NotStarted)
  val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  val currentTetrimino = MutableStateFlow<Tetrimino?>(null)
  val currentPiecePosition = MutableStateFlow<Tetrimino.Position?>(null)
  val nextTetrimino = MutableStateFlow<Tetrimino?>(null)
  private val tetriminoHistory = mutableListOf<Tetrimino>()

  val board = MutableStateFlow(Board())
  val displayBoard: StateFlow<Board> = combine(board, currentTetrimino, currentPiecePosition) {
    board: Board, tetrimino: Tetrimino?, position: Tetrimino.Position? ->
    if (tetrimino == null || position == null) board
    else board.with(tetrimino, position)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Board())

  private var nBackDecisionMade = false
  val nbackLevel = MutableStateFlow(1)
  val nbackStreak = MutableStateFlow(0)
  val nbackMultiplier = combine(nbackLevel, nbackStreak) { level, streak ->
    1.0f + (streak * (level * 2) * 0.1f)
  }.stateIn(viewModelScope, SharingStarted.Eagerly, 1f)

  // Score
  private var _score by mutableStateOf(0)
  val score: Int get() = _score

  // Game speed (milliseconds per tick)
  private var _gameSpeed by mutableStateOf(1000L)
  val gameSpeed: Long get() = _gameSpeed

  // Game job for coroutine
  private var gameJob: Job? = null
  private val gameScope = CoroutineScope(Dispatchers.Default)

  private var _timeRemaining by mutableStateOf(GAME_DURATION_SECONDS)
  val timeRemaining: Int get() = _timeRemaining

  private var timerJob: Job? = null

  // New methods to increase/decrease n-back level before game starts
  fun increaseNBackLevel() {
    if (_gameState.value == GameState.NotStarted && nbackLevel.value < 15) {
      nbackLevel.value++
    }
  }

  fun decreaseNBackLevel() {
    if (_gameState.value == GameState.NotStarted && nbackLevel.value > 1) {
      nbackLevel.value--
    }
  }

  fun startGame() {
    if (_gameState.value != GameState.Running) {
      resetGame()
      _gameState.value = GameState.Running
      spawnNewPiece()
      startGameLoop()
      startTimer()
    }
  }

  fun pauseGame() {
    if (_gameState.value == GameState.Running) {
      _gameState.value = GameState.Paused
      gameJob?.cancel()
      timerJob?.cancel()
    }
  }

  fun resumeGame() {
    if (_gameState.value == GameState.Paused) {
      _gameState.value = GameState.Running
      startGameLoop()
      startTimer()
    }
  }

  fun resetGame() {
    gameJob?.cancel()
    timerJob?.cancel()
    board.value = Board()
    currentTetrimino.value = null
    nextTetrimino.value = null
    tetriminoHistory.clear()
    nbackStreak.value = 0
    _score = 0
    _gameSpeed = 1000L
    _timeRemaining = GAME_DURATION_SECONDS
    _gameState.value = GameState.NotStarted
  }


  private fun startGameLoop() {
    gameJob?.cancel()
    gameJob = gameScope.launch {
      while (_gameState.value == GameState.Running) {
        delay(_gameSpeed)
        tick()
      }
    }
  }

  private fun tick() {
    if (_gameState.value != GameState.Running) return

    if (!moveTetriminoDown()) {
      lockTetrimino()
      val completedLines = clearFilledRows()
      addScore(completedLines)

      if (!spawnNewPiece()) {
        _gameState.value = GameState.GameOver
        gameJob?.cancel()
      }
    }
  }

  fun leftClicked() {
    if (_gameState.value != GameState.Running) return
    val position = currentPiecePosition.value ?: return

    val newPosition = position.copy(col = position.col - 1)
    if (isValidPosition(currentTetrimino.value, newPosition)) {
      currentPiecePosition.value = newPosition
    }
  }

  fun rightClicked() {
    if (_gameState.value != GameState.Running) return
    val position = currentPiecePosition.value ?: return

    val newPosition = position.copy(col = position.col + 1)
    if (isValidPosition(currentTetrimino.value, newPosition)) {
      currentPiecePosition.value = newPosition
    }
  }

  fun rotatePiece(direction: Rotation) {
    if (_gameState.value != GameState.Running) return
    val tetrimino = currentTetrimino.value ?: return
    val position = currentPiecePosition.value ?: return

    val rotatedPiece = tetrimino.rotate(direction)
    if (isValidPosition(rotatedPiece, position)) {
      currentTetrimino.value = rotatedPiece
    } else {
      // Try wall kick (adjust position if rotation would cause collision)
      for (offset in listOf(-1, 1, -2, 2)) {
        val newPosition = position.copy(col = position.col + offset)
        if (isValidPosition(rotatedPiece, newPosition)) {
          currentTetrimino.value = rotatedPiece
          currentPiecePosition.value = newPosition
          break
        }
      }
    }
  }

  fun dropPiece() {
    if (_gameState.value != GameState.Running) return

    while (moveTetriminoDown());
    tick()
  }

  fun downClicked(): Boolean {
    if (_gameState.value != GameState.Running) return false
    return moveTetriminoDown()
  }

  private fun moveTetriminoDown(): Boolean {
    val position = currentPiecePosition.value ?: return false

    val newPosition = position.copy(row = position.row + 1)
    val valid = isValidPosition(currentTetrimino.value, newPosition)
    if (valid) currentPiecePosition.value = newPosition

    return valid
  }

  fun nBackMatch() {
    if (_gameState.value != GameState.Running || nBackDecisionMade) return

    val correct = if (tetriminoHistory.size > nbackLevel.value) {
      val nBackPiece = tetriminoHistory[tetriminoHistory.size - nbackLevel.value - 1]
      currentTetrimino.value?.type == nBackPiece.type
    } else {
      false
    }

    nbackStreak.value = if (correct) { nbackStreak.value + 1 } else 0
  }

  fun nBackNoMatch() {
    if (_gameState.value != GameState.Running || nBackDecisionMade) return
    nBackDecisionMade = true

    val correct = if (tetriminoHistory.size > nbackLevel.value) {
      val nBackPiece = tetriminoHistory[tetriminoHistory.size - nbackLevel.value - 1]
      currentTetrimino.value?.type != nBackPiece.type
    } else true

    nbackStreak.value = if (correct) { nbackStreak.value + 1 } else 0
  }

  // TODO: Move to Board.kt.
  private fun isValidPosition(
    tetrimino: Tetrimino?,
    position: Tetrimino.Position
  ): Boolean {
    if (tetrimino == null) return false

    for (row in tetrimino.shape.indices) {
      for (col in tetrimino.shape[row].indices) {
        if (tetrimino.shape[row][col] != 0) {
          val boardRow = position.row + row
          val boardCol = position.col + col

          // Check bounds
          if (boardRow < 0 || boardRow >= boardHeight ||
            boardCol < 0 || boardCol >= boardWidth) {
            return false
          }

          // Check collision with existing blocks
          if (board.value.matrix[boardRow][boardCol] != 0) {
            return false
          }
        }
      }
    }

    return true
  }

  private fun lockTetrimino() {
    val tetrimino = currentTetrimino.value ?: return
    val position = currentPiecePosition.value ?: return

    if (!nBackDecisionMade) nBackNoMatch()

    val boardUpdate = mutableMapOf<Int, Map<Int, Int>>()

    tetrimino.shape.forEachIndexed { row, columns ->
      val rowUpdate = mutableMapOf<Int, Int>()
      boardUpdate[row + position.row] = rowUpdate

      columns.forEachIndexed { column, tetriminoType ->
        if (tetriminoType != 0) rowUpdate[column + position.col] = Tetrimino.lockedType
      }
    }

    board.value = board.value.copy(boardUpdate)
  }

  private fun clearFilledRows(): Int {
    var completedLines = 0
    val newMatrix = board.value.matrix.deepCopy()

    // Iterate from the bottom of the well to the top.
    var row = boardHeight - 1
    while (row >= 0) {
      val columns = newMatrix[row]

      if (columns.all { it != 0 }) {
        // Remove the line and shift everything down.
        for (r in row downTo 1) {
          newMatrix[r] = newMatrix[r - 1].copyOf()
        }
        newMatrix[0] = IntArray(boardWidth) { 0 }
        completedLines++
      } else {
        row--
      }
    }

    if (completedLines > 0) {
      board.value = Board(newMatrix)
    }

    return completedLines
  }

  private fun addScore(completedLines: Int) {
    // Score based on number of lines completed with multiplier
    val baseScore = when (completedLines) {
      0 -> 20
      1 -> 100
      2 -> 300
      3 -> 500
      4 -> 1000
      else -> 0
    }

    _score += (baseScore * nbackMultiplier.value).toInt()

    // Increase game speed based on score milestones
    if (_score > 5000 && _gameSpeed > 500) {
      _gameSpeed = 500L
    } else if (_score > 3000 && _gameSpeed > 700) {
      _gameSpeed = 700L
    } else if (_score > 1000 && _gameSpeed > 850) {
      _gameSpeed = 850L
    }
  }

  private fun spawnNewPiece(): Boolean {
    val spawnedPiece = nextTetrimino.value ?: generateRandomPiece()
    nextTetrimino.value = if (Random.nextFloat() < nbackMatchBias) {
      if (nbackLevel.value == 1) {
        spawnedPiece.copy()
      } else {
        tetriminoHistory.getOrNull((tetriminoHistory.size) - nbackLevel.value) ?: generateRandomPiece()
      }
    } else generateRandomPiece()

    currentTetrimino.value = spawnedPiece
    currentPiecePosition.value = newTetriminoStartPosition

    tetriminoHistory.add(spawnedPiece)
    nBackDecisionMade = false

    return isValidPosition(currentTetrimino.value, newTetriminoStartPosition)
  }

  private fun generateRandomPiece(): Tetrimino {
    return Tetrimino.types[Random.nextInt(Tetrimino.types.size)]
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = gameScope.launch {
      while (_timeRemaining > 0 && _gameState.value == GameState.Running) {
        val drift = timeRemaining % 1000L
        delay(1000L - drift)
        _timeRemaining--
      }

      // End the game when timer reaches zero
      if (_timeRemaining <= 0 && _gameState.value == GameState.Running) {
        _gameState.value = GameState.GameOver
        gameJob?.cancel()
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    gameJob?.cancel()
    timerJob?.cancel()
  }
}

// Game state enum
enum class GameState {
  NotStarted,
  Running,
  Paused,
  GameOver
}