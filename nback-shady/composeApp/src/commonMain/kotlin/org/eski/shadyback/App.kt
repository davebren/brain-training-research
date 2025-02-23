package org.eski.shadyback

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.eski.shadyback.ui.GameScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        GameScreen()
    }
}