package io.github.vahan16.coinstellations

import io.ktor.client.engine.HttpClientEngine

/** Each target supplies its own Ktor engine (CIO on JVM/Android, Darwin on iOS, JS on Web). */
expect fun platformEngine(): HttpClientEngine

/** Human-readable platform name, shown in the About section. */
expect val platformLabel: String
