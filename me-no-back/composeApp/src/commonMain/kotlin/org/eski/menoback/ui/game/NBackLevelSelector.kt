package org.eski.menoback.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NBackLevelSelector(
  vm: GameScreenViewModel,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "${vm.nBackLevel}-Back Level",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = Color.LightGray
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Button(
        onClick = { vm.decreaseNBackLevel() },
        modifier = Modifier.size(40.dp)
      ) {
        Icon(Icons.Filled.Close, contentDescription = "Decrease N-Back Level")
//        Icon(Icons.Default.Remove, contentDescription = "Decrease N-Back Level")
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = vm.nBackLevel.toString(),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = { vm.increaseNBackLevel() },
        modifier = Modifier.size(40.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = "Increase N-Back Level")
      }
    }
  }
}