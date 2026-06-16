package io.github.vahan16.coinstellations

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Coinstellations",
        state = rememberWindowState(width = 1100.dp, height = 760.dp),
    ) {
        App()
    }
}
