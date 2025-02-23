package org.eski.shadyback.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random


class GameScreenViewModel: ViewModel() {
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

    private fun generateColorShade(baseColor: Color): Color {
        val variation = 0.5f
        return Color(
            red = (baseColor.red + Random.nextFloat() * variation - variation/2).coerceIn(0f, 1f),
            green = (baseColor.green + Random.nextFloat() * variation - variation/2).coerceIn(0f, 1f),
            blue = (baseColor.blue + Random.nextFloat() * variation - variation/2).coerceIn(0f, 1f)
        )
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
        if (!gameStarted.value) {
            colorSequence.value = List(nBack.value + 1) {
                generateColorShade(baseColors[selectedBaseColor.value]!!)
            }
            gameStarted.value = true
        } else {
            colorSequence.value = colorSequence.value.drop(1) +
                    generateColorShade(baseColors[selectedBaseColor.value]!!)
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