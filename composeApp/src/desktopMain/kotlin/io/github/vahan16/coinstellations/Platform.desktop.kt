package io.github.vahan16.coinstellations

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun platformEngine(): HttpClientEngine = CIO.create()

actual val platformLabel: String = "Desktop"
