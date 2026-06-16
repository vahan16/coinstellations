package io.github.vahan16.coinstellations

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun platformEngine(): HttpClientEngine = Js.create()

actual val platformLabel: String = "Web"
