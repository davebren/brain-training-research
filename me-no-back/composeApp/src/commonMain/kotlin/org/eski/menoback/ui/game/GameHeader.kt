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
import org.eski.menoback.ui.game.vm.GameScreenViewModel

@Composable
fun GameHeader(
  vm: GameScreenViewModel
) {
  val nbackLevel by vm.nback.level.collectAsState()
  val nbackStreak by vm.nback.streak.collectAsState()
  val nbackMultiplierText by vm.nback.multiplierText.collectAsState()
  val score by vm.score.collectAsState()
  val timeLeft by vm.timeRemaining.collectAsState()
  val timerColor by vm.timerColor.collectAsState()

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
      InfoItem(label = "Score", value = score.toString())
      InfoItem(label = "Multiplier", value = nbackMultiplierText)
      InfoItem(label = "$nbackLevel-Back", value = "Streak: $nbackStreak")
      InfoItem(label = "Time", value = timeLeft.toString(), timerColor)
    }
  }
}

@Composable
private fun InfoItem(label: String, value: String, valueTextColor: Color = Color.LightGray) {
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
      color = valueTextColor,
    )
  }
}