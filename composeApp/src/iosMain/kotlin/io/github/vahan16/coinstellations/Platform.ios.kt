package io.github.vahan16.coinstellations

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun platformEngine(): HttpClientEngine = Darwin.create()

actual val platformLabel: String = "iOS"
