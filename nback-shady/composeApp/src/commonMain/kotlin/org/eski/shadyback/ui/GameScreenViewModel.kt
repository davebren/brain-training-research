package org.eski.shadyback.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.eski.ui.util.hsl
import kotlin.math.abs
import kotlin.math.ceil

class GameScreenViewModel : ViewModel() {
  val nBack = MutableStateFlow(2)
  val selectedBaseColor = MutableStateFlow("Red")

  val colorSequence = MutableStateFlow<List<Color>>(emptyList())
  val currentPosition = MutableStateFlow(0)
  val score = MutableStateFlow(0)
  val gameStarted = MutableStateFlow(false)

  private val baseColors = mapOf(
    "Red" to Color.Red,
    "Orange" to Color(0xFFFFA500),
    "Yellow" to Color.Yellow,
    "Green" to Color.Green,
    "Blue" to Color.Blue,
    "Purple" to Color(0xFF800080),
    "Pink" to Color(0xFFFFC1CC),
    "White" to Color.White,
    "Black" to Color.Black
  )

  private fun getShades(variance: Float): List<Color> {
    val baseColor = baseColors[selectedBaseColor.value]!!
    val hsl = baseColor.hsl()
    val shadeIndex = listOf(-2, -1, 0, 1, 2)
    return shadeIndex.map { index ->
      var lightness = hsl.lightness + (index * variance)
      if (lightness < 0) {
        val stepsOutOfBounds = ceil(abs(lightness / variance))
        lightness = hsl.lightness + (shadeIndex.last() * variance) + (stepsOutOfBounds * variance)
      } else if (lightness > 1) {
        val stepsOutOfBounds = ceil(abs((lightness - 1) / variance))
        lightness = hsl.lightness - (shadeIndex.first() * variance) - (stepsOutOfBounds * variance)
      }

      Color.hsl(hsl.hue, hsl.saturation, lightness)
    }
  }

  fun increaseNBack() {
    if (nBack.value < 5) nBack.value++
  }

  fun decreaseNBack() {
    if (nBack.value > 1) nBack.value--
  }

  fun changeBaseColor() {
    val colors = baseColors.keys.toList()
    val currentIndex = colors.indexOf(selectedBaseColor.value)
    selectedBaseColor.value = colors[(currentIndex + 1) % colors.size]
  }

  fun advanceGame() {
    val shades = getShades(.1f)
    if (!gameStarted.value) {
      colorSequence.value = List(nBack.value + 1) { shades.random() }
      gameStarted.value = true
    } else {
      colorSequence.value = colorSequence.value.drop(1) + shades.random()
    }
    currentPosition.value++
  }

  fun checkAnswer(isMatch: Boolean) {
    if (currentPosition.value >= nBack.value) {
      val currentColor = colorSequence.value.last()
      val nBackColor = colorSequence.value[colorSequence.value.size - nBack.value - 1]
      val isCorrectMatch = currentColor == nBackColor
      if (isMatch == isCorrectMatch) {
        score.value++
      }
    }
    advanceGame()
  }
}