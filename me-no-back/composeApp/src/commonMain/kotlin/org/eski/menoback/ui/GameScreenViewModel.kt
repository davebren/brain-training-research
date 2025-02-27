package org.eski.menoback.ui

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
import kotlin.random.Random

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
  private var _currentPiece by mutableStateOf<Piece?>(null)
  val currentPiece: Piece? get() = _currentPiece

  // Position of current piece
  private var _currentPiecePosition by mutableStateOf(Position(0, 0))
  val currentPiecePosition: Position get() = _currentPiecePosition

  // Next piece that will appear
  private var _nextPiece by mutableStateOf<Piece?>(null)
  val nextPiece: Piece? get() = _nextPiece

  // N-back history of pieces (maintains the sequence of previous pieces)
  private val pieceHistory = mutableListOf<Piece>()

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

  init {
    // Nothing to initialize yet - we'll start the game with startGame()
  }

  fun startGame() {
    if (_gameState.value != GameState.Running) {
      resetGame()
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
    _currentPiece = null
    _nextPiece = null
    pieceHistory.clear()
    _nBackLevel = 1
    _nBackStreak = 0
    _score = 0
    _multiplier = 1.0f
    _gameSpeed = 1000L
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

  fun movePieceLeft() {
    if (_gameState.value != GameState.Running) return

    val newPosition = _currentPiecePosition.copy(col = _currentPiecePosition.col - 1)
    if (isValidPosition(_currentPiece, newPosition)) {
      _currentPiecePosition = newPosition
    }
  }

  fun movePieceRight() {
    if (_gameState.value != GameState.Running) return

    val newPosition = _currentPiecePosition.copy(col = _currentPiecePosition.col + 1)
    if (isValidPosition(_currentPiece, newPosition)) {
      _currentPiecePosition = newPosition
    }
  }

  fun rotatePiece() {
    if (_gameState.value != GameState.Running || _currentPiece == null) return

    val rotatedPiece = _currentPiece!!.rotate()
    if (isValidPosition(rotatedPiece, _currentPiecePosition)) {
      _currentPiece = rotatedPiece
    } else {
      // Try wall kick (adjust position if rotation would cause collision)
      for (offset in listOf(-1, 1, -2, 2)) {
        val newPosition = _currentPiecePosition.copy(col = _currentPiecePosition.col + offset)
        if (isValidPosition(rotatedPiece, newPosition)) {
          _currentPiece = rotatedPiece
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
    val isCorrect = if (pieceHistory.size > _nBackLevel) {
      val nBackPiece = pieceHistory[pieceHistory.size - _nBackLevel - 1]
      _currentPiece?.type == nBackPiece.type
    } else {
      false // Not enough history yet
    }

    handleNBackDecision(isCorrect)
  }

  fun nBackNoMatch() {
    if (_gameState.value != GameState.Running || nBackDecisionMade) return

    // Check if the current piece does NOT match the n-back piece
    val isCorrect = if (pieceHistory.size > _nBackLevel) {
      val nBackPiece = pieceHistory[pieceHistory.size - _nBackLevel - 1]
      _currentPiece?.type != nBackPiece.type
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

      // Increase n-back level periodically
      if (_nBackStreak % 10 == 0 && _nBackLevel < 3) {
        _nBackLevel++
      }
    } else {
      // Incorrect n-back decision
      _nBackStreak = 0
      _multiplier = 1.0f
    }
  }

  // Helper methods

  private fun isValidPosition(piece: Piece?, position: Position): Boolean {
    if (piece == null) return false

    for (row in piece.shape.indices) {
      for (col in piece.shape[row].indices) {
        if (piece.shape[row][col] != 0) {
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
    return isValidPosition(_currentPiece, newPosition)
  }

  private fun lockPiece() {
    if (_currentPiece == null) return

    // Add the piece to the board
    for (row in _currentPiece!!.shape.indices) {
      for (col in _currentPiece!!.shape[row].indices) {
        if (_currentPiece!!.shape[row][col] != 0) {
          val boardRow = _currentPiecePosition.row + row
          val boardCol = _currentPiecePosition.col + col

          if (boardRow >= 0 && boardRow < boardHeight &&
            boardCol >= 0 && boardCol < boardWidth) {
            _board[boardRow][boardCol] = _currentPiece!!.type
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
    val spawnedPiece = _nextPiece ?: generateRandomPiece()
    _nextPiece = generateRandomPiece()

    // Set the current piece and position
    _currentPiece = spawnedPiece
    _currentPiecePosition = Position(row = 0, col = boardWidth / 2 - 2)

    // Add to history for n-back challenge
    if (spawnedPiece != null) {
      pieceHistory.add(spawnedPiece)
      nBackDecisionMade = false
    }

    // Check if the new piece can be placed
    return isValidPosition(_currentPiece, _currentPiecePosition)
  }

  private fun generateRandomPiece(): Piece {
    val pieceTypes = listOf(
      Piece(
        type = 1,
        shape = arrayOf(
          intArrayOf(0, 0, 0, 0),
          intArrayOf(1, 1, 1, 1),
          intArrayOf(0, 0, 0, 0),
          intArrayOf(0, 0, 0, 0)
        )
      ),
      Piece(
        type = 2,
        shape = arrayOf(
          intArrayOf(2, 2),
          intArrayOf(2, 2)
        )
      ),
      Piece(
        type = 3,
        shape = arrayOf(
          intArrayOf(0, 3, 0),
          intArrayOf(3, 3, 3),
          intArrayOf(0, 0, 0)
        )
      ),
      Piece(
        type = 4,
        shape = arrayOf(
          intArrayOf(0, 0, 4),
          intArrayOf(4, 4, 4),
          intArrayOf(0, 0, 0)
        )
      ),
      Piece(
        type = 5,
        shape = arrayOf(
          intArrayOf(5, 0, 0),
          intArrayOf(5, 5, 5),
          intArrayOf(0, 0, 0)
        )
      ),
      Piece(
        type = 6,
        shape = arrayOf(
          intArrayOf(0, 6, 6),
          intArrayOf(6, 6, 0),
          intArrayOf(0, 0, 0)
        )
      ),
      Piece(
        type = 7,
        shape = arrayOf(
          intArrayOf(7, 7, 0),
          intArrayOf(0, 7, 7),
          intArrayOf(0, 0, 0)
        )
      )
    )

    return pieceTypes[Random.nextInt(pieceTypes.size)]
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

// Position data class
data class Position(val row: Int, val col: Int)

// Piece data class with rotation logic
data class Piece(val type: Int, val shape: Array<IntArray>) {
  fun rotate(): Piece {
    val rows = shape.size
    val cols = shape[0].size
    val newShape = Array(cols) { IntArray(rows) }

    // Rotate 90 degrees clockwise
    for (row in 0 until rows) {
      for (col in 0 until cols) {
        newShape[col][rows - 1 - row] = shape[row][col]
      }
    }

    return Piece(type, newShape)
  }

  // Override equals and hashCode because we use Array which doesn't implement them properly
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (this::class != other?.let { it::class }) return false

    other as Piece

    if (type != other.type) return false
    if (!shape.contentDeepEquals(other.shape)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type
    result = 31 * result + shape.contentDeepHashCode()
    return result
  }
}