package org.eski.menoback.model

const val boardWidth = 10
const val boardHeight = 20

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

  data class Position(val row: Int, val colum: Int)
}
