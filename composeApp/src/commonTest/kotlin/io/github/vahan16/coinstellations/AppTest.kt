package io.github.vahan16.coinstellations

import io.github.vahan16.coinstellations.data.DemoData
import io.github.vahan16.coinstellations.ui.formatCompactUsd
import io.github.vahan16.coinstellations.ui.formatPercent
import io.github.vahan16.coinstellations.ui.formatPrice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppTest {

    @Test
    fun prices_format_with_adaptive_precision() {
        assertEquals("$68,120.00", formatPrice(68120.0))
        assertEquals("$1.00", formatPrice(1.0))
        assertEquals("—", formatPrice(null))
        // sub-cent coins keep significant digits
        assertTrue(formatPrice(0.0000242).startsWith("$0.0000"))
    }

    @Test
    fun percent_and_compact_have_expected_shape() {
        assertEquals("+1.40%", formatPercent(1.4))
        assertEquals("-2.10%", formatPercent(-2.1))
        val cap = formatCompactUsd(1_345_000_000_000.0)
        assertTrue(cap.startsWith("$1.3") && cap.endsWith("T"), "got $cap")
        assertTrue(formatCompactUsd(812_500_000.0).endsWith("M"))
    }

    @Test
    fun demo_data_is_sane() {
        assertTrue(DemoData.coins.size >= 20)
        assertTrue(DemoData.coins.all { it.id.isNotBlank() && it.symbol.isNotBlank() })
        assertTrue(DemoData.coins.all { (it.marketCap ?: 0.0) > 0.0 })
    }

    @Test
    fun demo_chart_ends_at_current_price() {
        val btc = DemoData.coins.first { it.id == "bitcoin" }
        val chart = DemoData.chart(btc, "1w")
        assertEquals(56, chart.size)
        assertEquals(btc.price, chart.last().price)
        assertTrue(chart.all { it.price >= 0.0 })
    }

    @Test
    fun visible_coins_filter_and_limit() {
        val state = SkyState(coins = DemoData.coins, starCount = 5)
        assertEquals(5, state.visibleCoins.size)
        // sorted by market cap desc -> Bitcoin first
        assertEquals("bitcoin", state.visibleCoins.first().id)

        val searched = state.copy(query = "eth").visibleCoins
        assertTrue(searched.isNotEmpty() && searched.all {
            it.name.contains("eth", true) || it.symbol.contains("eth", true)
        })
    }

    @Test
    fun timeframe_maps_to_change_fields() {
        val c = DemoData.coins.first { it.id == "solana" }
        assertEquals(c.priceChange1h, c.changeFor(Timeframe.HOUR))
        assertEquals(c.priceChange1d, c.changeFor(Timeframe.DAY))
        assertEquals(c.weeklyChange, c.changeFor(Timeframe.WEEK))
    }
}
