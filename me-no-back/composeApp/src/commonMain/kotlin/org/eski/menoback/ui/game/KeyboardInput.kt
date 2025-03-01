package org.eski.menoback.ui.game

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.delay
import org.eski.menoback.ui.game.vm.GameScreenViewModel

const val repeatDelayMillis = 250L
const val repeatTickMillis = 50L

@Composable
fun KeyboardInput(vm: GameScreenViewModel) {
  val focusRequester = remember { FocusRequester() }
  var hasFocus by remember { mutableStateOf(false) }
  var leftPressed by remember { mutableStateOf(false) }
  var rightPressed by remember { mutableStateOf(false) }
  var downPressed by remember { mutableStateOf(false) }

  Box(modifier = Modifier
    .focusRequester(focusRequester)
    .onFocusChanged { hasFocus = it.isFocused }
    .focusable()
    .onKeyEvent { event ->
      if (event.isCtrlPressed || event.isShiftPressed || event.isMetaPressed || event.isAltPressed) {
        return@onKeyEvent false
      }

      if (event.type == KeyEventType.KeyDown) {
        when (event.key) {
          Key.DirectionLeft -> {
            if (!leftPressed) {
              vm.leftClicked()
              leftPressed = true
            }
          }

          Key.DirectionRight -> {
            if (!rightPressed) {
              vm.rightClicked()
              rightPressed = true
            }
          }

          Key.V -> vm.rotatePiece(Rotation.clockwise)
          Key.Q -> vm.rotatePiece(Rotation.counterClockwise)
          Key.DirectionUp -> {
            vm.rotatePiece(Rotation.clockwise)
            vm.rotatePiece(Rotation.clockwise)
          }
          Key.DirectionDown -> {
            if (!downPressed) {
              vm.downClicked()
              downPressed = true
            }
          }

          Key.Spacebar -> vm.dropPiece()
        }
      }

      if (event.type == KeyEventType.KeyUp) {
        when(event.key) {
          Key.DirectionLeft -> leftPressed = false
          Key.DirectionRight -> rightPressed = false
          Key.DirectionDown -> downPressed = false
          Key.Z -> vm.nbackMatchChoice()
        }
      }

      return@onKeyEvent false
    }
  )

  LaunchedEffect(leftPressed) {
    delay(repeatDelayMillis)
    while (leftPressed) {
      vm.leftClicked()
      delay(repeatTickMillis)
    }
  }

  LaunchedEffect(rightPressed) {
    delay(repeatDelayMillis)
    while (rightPressed) {
      vm.rightClicked()
      delay(repeatTickMillis)
    }
  }

  LaunchedEffect(downPressed) {
    delay(repeatDelayMillis)
    while (downPressed) {
      val valid = vm.downClicked()
      if (valid) {
        delay(repeatTickMillis)
      } else {
        downPressed = false
        delay(repeatTickMillis)
      }
    }
  }

  if (!hasFocus) {
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }
  }
}