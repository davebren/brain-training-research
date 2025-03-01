package org.eski.menoback.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameHeader(
  vm: GameScreenViewModel
) {
  val nbackLevel by vm.nbackLevel.collectAsState()
  val nbackStreak by vm.nbackStreak.collectAsState()
  val nbackMultiplier by vm.nbackMultiplier.collectAsState()

  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "MeNoBack",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = Color.LightGray,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      InfoItem(label = "Score", value = vm.score.toString())
      InfoItem(label = "Multiplier", value = "${nbackMultiplier}x")
      InfoItem(label = "$nbackLevel-Back", value = "Streak: $nbackStreak")

      GameTimer(timeRemaining = vm.timeRemaining)
    }
  }
}

@Composable
private fun InfoItem(label: String, value: String) {
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
      fontWeight = FontWeight.Bold,
      color = Color.LightGray,
    )
  }
}