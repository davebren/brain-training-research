package org.eski.menoback.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GameControls(
  onMoveLeftClicked: () -> Unit,
  onMoveRightClicked: () -> Unit,
  onRotateClicked: () -> Unit,
  onDropClicked: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Controls",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Rotate button
    Button(
      onClick = onRotateClicked,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(Icons.Filled.Refresh, contentDescription = "Rotate")
//            Icon(Icons.Filled.RotateRight, contentDescription = "Rotate")
      Spacer(modifier = Modifier.width(8.dp))
      Text("Rotate")
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Left/Right buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      Button(
        onClick = onMoveLeftClicked,
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Move Left")
      }

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = onMoveRightClicked,
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Filled.ArrowForward, contentDescription = "Move Right")
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Drop button
    Button(
      onClick = onDropClicked,
      modifier = Modifier.fillMaxWidth(),
      colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
    ) {
      Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Drop")
//            Icon(Icons.Filled.ArrowDownward, contentDescription = "Drop")
      Spacer(modifier = Modifier.width(8.dp))
      Text("Drop")
    }
  }
}