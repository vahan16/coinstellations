package io.github.vahan16.coinstellations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vahan16.coinstellations.ui.Accent
import io.github.vahan16.coinstellations.ui.AppTheme
import io.github.vahan16.coinstellations.ui.CoinDetailSheet
import io.github.vahan16.coinstellations.ui.DownColor
import io.github.vahan16.coinstellations.ui.GearIcon
import io.github.vahan16.coinstellations.ui.Muted
import io.github.vahan16.coinstellations.ui.OnSurface
import io.github.vahan16.coinstellations.ui.PillBar
import io.github.vahan16.coinstellations.ui.RefreshIcon
import io.github.vahan16.coinstellations.ui.SearchIcon
import io.github.vahan16.coinstellations.ui.SettingsSheet
import io.github.vahan16.coinstellations.ui.SkyView
import io.github.vahan16.coinstellations.ui.SpaceBottom
import io.github.vahan16.coinstellations.ui.UpColor
import io.github.vahan16.coinstellations.ui.formatCompactUsd
import io.github.vahan16.coinstellations.ui.formatPercent
import kotlin.math.roundToInt

@Composable
fun App() {
    AppTheme {
        val store = remember { MarketStore() }
        DisposableEffect(Unit) { onDispose { store.close() } }
        val state by store.state.collectAsState()
        var showSettings by remember { mutableStateOf(false) }
        var searchOpen by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize().background(SpaceBottom)) {
            SkyView(
                coins = state.visibleCoins,
                timeframe = state.timeframe,
                modifier = Modifier.fillMaxSize(),
                onSelect = store::select,
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SpaceBottom.copy(alpha = 0.9f), Color.Transparent)))
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Coinstellations", color = OnSurface, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            LiveBadge(state.live)
                        }
                        Text(moodText(state), color = Muted, fontSize = 13.sp)
                    }
                    IconButtonBox(onClick = { searchOpen = !searchOpen }) { SearchIcon(Modifier.size(19.dp), OnSurface) }
                    Spacer(Modifier.width(8.dp))
                    IconButtonBox(onClick = { store.refresh() }) { RefreshIcon(Modifier.size(19.dp), OnSurface) }
                    Spacer(Modifier.width(8.dp))
                    IconButtonBox(onClick = { showSettings = true }) { GearIcon(Modifier.size(19.dp), OnSurface) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.global?.let { g ->
                        Chip("Cap ${formatCompactUsd(g.marketCap)}")
                        g.btcDominance?.let { Chip("BTC ${(it * 10).roundToInt() / 10.0}%") }
                        g.marketCapChange?.let { Chip("24h ${formatPercent(it)}") }
                    }
                }

                if (searchOpen) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = store::setQuery,
                        singleLine = true,
                        placeholder = { Text("Search coins…", color = Muted) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                PillBar(
                    options = Timeframe.entries,
                    selected = state.timeframe,
                    label = { it.label },
                    onSelect = store::setTimeframe,
                )
            }

            state.error?.let { err ->
                Box(
                    Modifier.align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DownColor.copy(alpha = 0.16f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(err, color = OnSurface, fontSize = 12.sp)
                }
            }

            state.selected?.let { coin ->
                CoinDetailSheet(
                    coin = coin,
                    chart = state.chart,
                    timeframe = state.timeframe,
                    onPeriod = store::setChartPeriod,
                    onDismiss = store::dismissDetail,
                )
            }

            if (showSettings) {
                SettingsSheet(
                    hasApiKey = state.hasApiKey,
                    live = state.live,
                    currency = state.currency,
                    starCount = state.starCount,
                    onSaveKey = { store.saveApiKey(it) },
                    onCurrency = store::setCurrency,
                    onStarCount = store::setStarCount,
                    onDismiss = { showSettings = false },
                )
            }
        }
    }
}

@Composable
private fun IconButtonBox(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
private fun LiveBadge(live: Boolean) {
    val txt = if (live) "LIVE" else "DEMO"
    val col = if (live) UpColor else Muted
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(col.copy(alpha = 0.18f)).padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(txt, color = col, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.06f)).padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, color = OnSurface.copy(alpha = 0.9f), fontSize = 12.sp)
    }
}

private fun moodText(state: SkyState): String {
    val m = state.moodChange
    val phrase = when {
        m > 3 -> "Clear skies — the market is glowing"
        m > 0.5 -> "Calm, bright skies tonight"
        m >= -0.5 -> "Still skies"
        m > -3 -> "Clouds rolling in"
        else -> "Stormy — stars are dimming"
    }
    return "$phrase · ${formatPercent(m)} (${state.timeframe.label})"
}
