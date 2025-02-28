package org.eski.menoback.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eski.menoback.model.Tetrimino
import kotlin.random.Random

val nbackMatchBias = 0.15f

class GameScreenViewModel : ViewModel() {
  // Size of the game board
  private val boardWidth = 10
  private val boardHeight = 20

  // Game state
  private val _gameState = MutableStateFlow<GameState>(GameState.NotStarted)
  val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  // Game board representation: 0 is empty, other values represent different block types
  private var _board = Array(boardHeight) { IntArray(boardWidth) { 0 } }
  var board by mutableStateOf(_board)
    private set

  // Current piece
  private var _currentTetrimino by mutableStateOf<Tetrimino?>(null)
  val currentTetrimino: Tetrimino? get() = _currentTetrimino

  // Position of current piece
  private var _currentPiecePosition by mutableStateOf(Tetrimino.Position(0, 0))
  val currentPiecePosition: Tetrimino.Position get() = _currentPiecePosition

  // Next piece that will appear
  private var _nextTetrimino by mutableStateOf<Tetrimino?>(null)
  val nextTetrimino: Tetrimino? get() = _nextTetrimino

  // N-back history of pieces (maintains the sequence of previous pieces)
  private val tetriminoHistory = mutableListOf<Tetrimino>()

  // Current n-back level
  private var _nBackLevel by mutableStateOf(1)
  val nBackLevel: Int get() = _nBackLevel

  // N-back correct streak
  private var _nBackStreak by mutableStateOf(0)
  val nBackStreak: Int get() = _nBackStreak

  // Score
  private var _score by mutableStateOf(0)
  val score: Int get() = _score

  // Score multiplier based on n-back streak
  private var _multiplier by mutableStateOf(1.0f)
  val multiplier: Float get() = _multiplier

  // Game speed (milliseconds per tick)
  private var _gameSpeed by mutableStateOf(1000L)
  val gameSpeed: Long get() = _gameSpeed

  // Game job for coroutine
  private var gameJob: Job? = null
  private val gameScope = CoroutineScope(Dispatchers.Default)

  // Flag to know if player has already made an n-back decision for current piece
  private var nBackDecisionMade = false

  // New methods to increase/decrease n-back level before game starts
  fun increaseNBackLevel() {
    if (_gameState.value == GameState.NotStarted && _nBackLevel < 15) {
      _nBackLevel++
    }
  }

  fun decreaseNBackLevel() {
    if (_gameState.value == GameState.NotStarted && _nBackLevel > 1) {
      _nBackLevel--
    }
  }

  fun startGame() {
    if (_gameState.value != GameState.Running) {
      resetGameExceptNBackLevel()
      _gameState.value = GameState.Running
      spawnNewPiece()
      startGameLoop()
    }
  }

  fun pauseGame() {
    if (_gameState.value == GameState.Running) {
      _gameState.value = GameState.Paused
      gameJob?.cancel()
    }
  }

  fun resumeGame() {
    if (_gameState.value == GameState.Paused) {
      _gameState.value = GameState.Running
      startGameLoop()
    }
  }

  fun resetGame() {
    gameJob?.cancel()
    _board = Array(boardHeight) { IntArray(boardWidth) { 0 } }
    board = _board
    _currentTetrimino = null
    _nextTetrimino = null
    tetriminoHistory.clear()
    // Keep n-back level the same - don't reset it
    _nBackStreak = 0
    _score = 0
    _multiplier = 1.0f
    _gameSpeed = 1000L
    _gameState.value = GameState.NotStarted
  }

  // Reset everything except n-back level
  private fun resetGameExceptNBackLevel() {
    gameJob?.cancel()
    _board = Array(boardHeight) { IntArray(boardWidth) { 0 } }
    board = _board
    _currentTetrimino = null
    _nextTetrimino = null
    tetriminoHistory.clear()
    _nBackStreak = 0
    _score = 0
    _multiplier = 1.0f
    _gameSpeed = 1000L
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

    if (canMovePieceDown()) {
      _currentPiecePosition = _currentPiecePosition.copy(row = _currentPiecePosition.row + 1)
    } else {
      // Lock the current piece in place
      lockPiece()

      // Check for completed lines
      val completedLines = checkCompletedLines()
      if (completedLines > 0) {
        // Apply score based on completed lines and multiplier
        addScore(completedLines)
      }

      // Spawn a new piece
      if (!spawnNewPiece()) {
        // Game over if can't spawn new piece
        _gameState.value = GameState.GameOver
        gameJob?.cancel()
      }
    }
  }

  fun leftClicked() {
    if (_gameState.value != GameState.Running) return

    val newPosition = _currentPiecePosition.copy(col = _currentPiecePosition.col - 1)
    if (isValidPosition(_currentTetrimino, newPosition)) {
      _currentPiecePosition = newPosition
    }
  }

  fun rightClicked() {
    if (_gameState.value != GameState.Running) return

    val newPosition = _currentPiecePosition.copy(col = _currentPiecePosition.col + 1)
    if (isValidPosition(_currentTetrimino, newPosition)) {
      _currentPiecePosition = newPosition
    }
  }

  fun rotatePiece(direction: Rotation) {
    if (_gameState.value != GameState.Running || _currentTetrimino == null) return

    val rotatedPiece = _currentTetrimino!!.rotate(direction)
    if (isValidPosition(rotatedPiece, _currentPiecePosition)) {
      _currentTetrimino = rotatedPiece
    } else {
      // Try wall kick (adjust position if rotation would cause collision)
      for (offset in listOf(-1, 1, -2, 2)) {
        val newPosition = _currentPiecePosition.copy(col = _currentPiecePosition.col + offset)
        if (isValidPosition(rotatedPiece, newPosition)) {
          _currentTetrimino = rotatedPiece
          _currentPiecePosition = newPosition
          break
        }
      }
    }
  }

  fun dropPiece() {
    if (_gameState.value != GameState.Running) return

    while (canMovePieceDown()) {
      _currentPiecePosition = _currentPiecePosition.copy(row = _currentPiecePosition.row + 1)
    }

    // Immediately lock and spawn new piece
    tick()
  }

  // N-back memory challenge functions

  fun nBackMatch() {
    if (_gameState.value != GameState.Running || nBackDecisionMade) return

    // Check if the current piece matches the n-back piece
    val isCorrect = if (tetriminoHistory.size > _nBackLevel) {
      val nBackPiece = tetriminoHistory[tetriminoHistory.size - _nBackLevel - 1]
      _currentTetrimino?.type == nBackPiece.type
    } else {
      false // Not enough history yet
    }

    handleNBackDecision(isCorrect)
  }

  fun nBackNoMatch() {
    if (_gameState.value != GameState.Running || nBackDecisionMade) return

    // Check if the current piece does NOT match the n-back piece
    val isCorrect = if (tetriminoHistory.size > _nBackLevel) {
      val nBackPiece = tetriminoHistory[tetriminoHistory.size - _nBackLevel - 1]
      _currentTetrimino?.type != nBackPiece.type
    } else {
      true // Not enough history yet, so "no match" is correct
    }

    handleNBackDecision(isCorrect)
  }

  private fun handleNBackDecision(correct: Boolean) {
    nBackDecisionMade = true

    if (correct) {
      // Correct n-back decision
      _nBackStreak++

      // Increase multiplier for clearing lines
      _multiplier = 1.0f + (_nBackStreak * 0.1f)

      // Add n-back points
      _score += 10 * _nBackLevel

      // Removed: No longer automatically increase n-back level
    } else {
      // Incorrect n-back decision
      _nBackStreak = 0
      _multiplier = 1.0f
    }
  }

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
          if (_board[boardRow][boardCol] != 0) {
            return false
          }
        }
      }
    }

    return true
  }

  private fun canMovePieceDown(): Boolean {
    val newPosition = _currentPiecePosition.copy(row = _currentPiecePosition.row + 1)
    return isValidPosition(_currentTetrimino, newPosition)
  }

  private fun lockPiece() {
    if (_currentTetrimino == null) return

    if (!nBackDecisionMade) nBackNoMatch()

    // Add the piece to the board
    for (row in _currentTetrimino!!.shape.indices) {
      for (col in _currentTetrimino!!.shape[row].indices) {
        if (_currentTetrimino!!.shape[row][col] != 0) {
          val boardRow = _currentPiecePosition.row + row
          val boardCol = _currentPiecePosition.col + col

          if (boardRow >= 0 && boardRow < boardHeight &&
            boardCol >= 0 && boardCol < boardWidth) {
            _board[boardRow][boardCol] = _currentTetrimino!!.type
          }
        }
      }
    }

    // Update the board state
    board = _board.map { it.copyOf() }.toTypedArray()
  }

  private fun checkCompletedLines(): Int {
    var completedLines = 0

    for (row in boardHeight - 1 downTo 0) {
      if (_board[row].all { it != 0 }) {
        // Remove the line and shift everything down
        for (r in row downTo 1) {
          _board[r] = _board[r - 1].copyOf()
        }
        // Clear the top line
        _board[0] = IntArray(boardWidth) { 0 }

        completedLines++
      }
    }

    if (completedLines > 0) {
      // Update the board state
      board = _board.map { it.copyOf() }.toTypedArray()
    }

    return completedLines
  }

  private fun addScore(completedLines: Int) {
    // Score based on number of lines completed with multiplier
    val baseScore = when (completedLines) {
      1 -> 100
      2 -> 300
      3 -> 500
      4 -> 800
      else -> 0
    }

    _score += (baseScore * _multiplier).toInt()

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
    // Use the next piece if available, otherwise generate a new one
    val spawnedPiece = _nextTetrimino ?: generateRandomPiece()
    _nextTetrimino = if (Random.nextFloat() < nbackMatchBias) {
      if (nBackLevel == 1) {
        spawnedPiece.copy()
      } else {
        tetriminoHistory.getOrNull((tetriminoHistory.size) - nBackLevel) ?: generateRandomPiece()
      }
    } else generateRandomPiece()

    // Set the current piece and position
    _currentTetrimino = spawnedPiece
    _currentPiecePosition = Tetrimino.Position(row = 0, col = boardWidth / 2 - 2)

    // Add to history for n-back challenge
    if (spawnedPiece != null) {
      tetriminoHistory.add(spawnedPiece)
      nBackDecisionMade = false
    }

    // Check if the new piece can be placed
    return isValidPosition(_currentTetrimino, _currentPiecePosition)
  }

  private fun generateRandomPiece(): Tetrimino {
    return Tetrimino.types[Random.nextInt(Tetrimino.types.size)]
  }

  override fun onCleared() {
    super.onCleared()
    gameJob?.cancel()
  }
}

// Game state enum
enum class GameState {
  NotStarted,
  Running,
  Paused,
  GameOver
}