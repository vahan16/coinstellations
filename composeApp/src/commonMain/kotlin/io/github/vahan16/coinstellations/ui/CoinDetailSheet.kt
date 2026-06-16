package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vahan16.coinstellations.ChartState
import io.github.vahan16.coinstellations.Timeframe
import io.github.vahan16.coinstellations.changeFor
import io.github.vahan16.coinstellations.data.Coin

private val PERIODS = listOf("24h", "1w", "1m", "1y", "all")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CoinDetailSheet(
    coin: Coin,
    chart: ChartState,
    timeframe: Timeframe,
    onPeriod: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uri = LocalUriHandler.current
    val tint = changeColor(coin.changeFor(timeframe))

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Surface) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Monogram(coin.symbol, tint, 44.dp)
                Column(Modifier.weight(1f)) {
                    Text(coin.name, color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        coin.symbol.uppercase() + (coin.rank?.let { "  ·  Rank #$it" } ?: ""),
                        color = Muted, fontSize = 13.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatPrice(coin.price), color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(formatPercent(coin.changeFor(timeframe)), color = tint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ChangeChip("1h", coin.priceChange1h)
                ChangeChip("24h", coin.priceChange1d)
                ChangeChip("7d", coin.weeklyChange)
            }

            val positive = chart.points.let { it.size >= 2 && it.last().price >= it.first().price } ||
                (chart.points.isEmpty() && (coin.changeFor(timeframe) ?: 0.0) >= 0)
            Box(
                Modifier.fillMaxWidth().height(168.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center,
            ) {
                if (chart.points.isEmpty()) {
                    Text(if (chart.loading) "Charting the stars…" else "No chart data", color = Muted, fontSize = 13.sp)
                } else {
                    Sparkline(chart.points, positive, Modifier.fillMaxSize().padding(8.dp))
                }
            }

            PillBar(PERIODS, chart.period, { it }, onPeriod, Modifier.align(Alignment.CenterHorizontally))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    StatCell("Market cap", formatCompactUsd(coin.marketCap), Modifier.weight(1f))
                    StatCell("Volume 24h", formatCompactUsd(coin.volume), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth()) {
                    StatCell("Circulating", "${formatCompact(coin.availableSupply)} ${coin.symbol.uppercase()}", Modifier.weight(1f))
                    StatCell("Fully diluted", formatCompactUsd(coin.fullyDilutedValuation), Modifier.weight(1f))
                }
            }

            val links = buildList {
                coin.websiteUrl?.let { add("Website" to it) }
                coin.twitterUrl?.let { add("X / Twitter" to it) }
                coin.explorers?.firstOrNull()?.let { add("Explorer" to it) }
            }
            if (links.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    links.forEach { (label, url) -> LinkChip(label, url) }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Data from ", color = Muted, fontSize = 12.sp)
                Text(
                    "CoinStats API ↗",
                    color = Accent, fontSize = 12.sp,
                    modifier = Modifier.clickable { runCatching { uri.openUri("https://coinstats.app/api/") } },
                )
            }
        }
    }
}
