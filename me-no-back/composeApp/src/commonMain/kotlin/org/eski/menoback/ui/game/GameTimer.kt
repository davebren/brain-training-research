package org.eski.menoback.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameTimer(
  timeRemaining: Int,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    Text(
      text = "Time",
      fontSize = 12.sp,
      color = Color.Gray
    )

    // Change color based on time remaining (red when below 10 seconds)
    val timerColor = if (timeRemaining <= 10) Color.Red else Color.White

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .padding(4.dp)
        .background(
          color = Color.DarkGray.copy(alpha = 0.7f),
          shape = RoundedCornerShape(4.dp)
        )
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = "${timeRemaining}s",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = timerColor
      )
    }
  }
}