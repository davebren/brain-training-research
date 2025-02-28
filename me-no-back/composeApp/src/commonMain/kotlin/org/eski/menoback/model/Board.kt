package org.eski.menoback.model

import org.eski.util.deepCopy

const val boardWidth = 10
const val boardHeight = 20
val newTetriminoStartPosition = Tetrimino.Position(row = 0, col = boardWidth / 2 - 2)

data class Board(
  val matrix: Array<IntArray> = Array(boardHeight) { IntArray(boardWidth) { 0 } }
) {

  fun copy(updates: Map<Int, Map<Int, Int>>?): Board {
    val newMatrix = Array(boardHeight) { row ->
      IntArray(boardWidth) { column ->
        updates?.get(row)?.get(column) ?: matrix[row][column]
      }
    }
    return Board(newMatrix)
  }

  fun with(newTetrimino: Tetrimino, position: Tetrimino.Position): Board {
    println("Board.with")
    val newMatrix = matrix.deepCopy()
    
    for (row in newTetrimino.shape.indices) {
      for (col in newTetrimino.shape[row].indices) {
        if (newTetrimino.shape[row][col] != 0) {
          val boardRow = position.row + row
          val boardCol = position.col + col

          if (boardRow >= 0 && boardRow < newMatrix.size &&
            boardCol >= 0 && boardCol < newMatrix[0].size
          ) {
            newMatrix[boardRow][boardCol] = newTetrimino.type
          }
        }
      }
    }
    return Board(newMatrix)
  }
}
