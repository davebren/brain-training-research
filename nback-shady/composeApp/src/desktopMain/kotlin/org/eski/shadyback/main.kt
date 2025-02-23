package org.eski.shadyback

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Shady N-Back",
    ) {
        App()
    }
}