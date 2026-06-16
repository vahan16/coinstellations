package io.github.vahan16.coinstellations

import io.github.vahan16.coinstellations.data.AppSettings
import io.github.vahan16.coinstellations.data.ChartPoint
import io.github.vahan16.coinstellations.data.Coin
import io.github.vahan16.coinstellations.data.CoinStatsApi
import io.github.vahan16.coinstellations.data.DemoData
import io.github.vahan16.coinstellations.data.MarketGlobal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Timeframe(val label: String) { HOUR("1h"), DAY("24h"), WEEK("7d") }

fun Coin.changeFor(tf: Timeframe): Double? = when (tf) {
    Timeframe.HOUR -> priceChange1h
    Timeframe.DAY -> priceChange1d
    Timeframe.WEEK -> weeklyChange
}

data class ChartState(
    val period: String = "1w",
    val points: List<ChartPoint> = emptyList(),
    val loading: Boolean = false,
)

data class SkyState(
    val coins: List<Coin> = emptyList(),
    val global: MarketGlobal? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val live: Boolean = false,
    val timeframe: Timeframe = Timeframe.DAY,
    val query: String = "",
    val currency: String = "USD",
    val starCount: Int = 100,
    val selected: Coin? = null,
    val chart: ChartState = ChartState(),
    val hasApiKey: Boolean = false,
) {
    /** Coins actually drawn as stars: search-filtered, then top-N by market cap. */
    val visibleCoins: List<Coin>
        get() = coins
            .filter { c ->
                query.isBlank() ||
                    c.name.contains(query, true) ||
                    c.symbol.contains(query, true)
            }
            .sortedByDescending { it.marketCap ?: 0.0 }
            .take(starCount)

    /** Average move across visible coins for the current timeframe — drives the "mood". */
    val moodChange: Double
        get() = visibleCoins.mapNotNull { it.changeFor(timeframe) }.let {
            if (it.isEmpty()) 0.0 else it.average()
        }
}

/**
 * Holds all app state. Created once per app session and disposed with the UI.
 * Falls back to bundled demo data when there's no API key or a request fails.
 */
class MarketStore(private val settings: AppSettings = AppSettings()) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val api = CoinStatsApi(apiKeyProvider = { settings.apiKey })

    private val _state = MutableStateFlow(
        SkyState(
            currency = settings.currency,
            starCount = settings.bubbleCount,
            hasApiKey = settings.hasApiKey,
        )
    )
    val state: StateFlow<SkyState> = _state.asStateFlow()

    private var chartJob: Job? = null

    init { refresh() }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val key = settings.apiKey
            if (key.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        coins = DemoData.coins, global = DemoData.global,
                        live = false, loading = false, hasApiKey = false,
                    )
                }
                return@launch
            }
            try {
                val coins = api.getCoins(limit = 200, currency = settings.currency)
                val global = runCatching { api.getGlobalMarket() }.getOrNull()
                _state.update {
                    it.copy(
                        coins = coins.ifEmpty { DemoData.coins },
                        global = global ?: DemoData.global,
                        live = coins.isNotEmpty(), loading = false,
                        hasApiKey = true, error = null,
                    )
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        coins = DemoData.coins, global = DemoData.global,
                        live = false, loading = false, hasApiKey = true,
                        error = e.message ?: "Couldn't reach CoinStats — showing demo sky.",
                    )
                }
            }
        }
    }

    fun setTimeframe(tf: Timeframe) = _state.update { it.copy(timeframe = tf) }
    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun setCurrency(c: String) {
        settings.currency = c
        _state.update { it.copy(currency = c) }
        refresh()
    }

    fun setStarCount(n: Int) {
        settings.bubbleCount = n
        _state.update { it.copy(starCount = n) }
    }

    fun saveApiKey(key: String?) {
        settings.apiKey = key
        _state.update { it.copy(hasApiKey = settings.hasApiKey) }
        refresh()
    }

    fun select(coin: Coin) {
        val period = _state.value.chart.period
        _state.update { it.copy(selected = coin, chart = ChartState(period = period, loading = true)) }
        loadChart(coin, period)
    }

    fun setChartPeriod(period: String) {
        val coin = _state.value.selected ?: return
        _state.update { it.copy(chart = it.chart.copy(period = period, loading = true)) }
        loadChart(coin, period)
    }

    fun dismissDetail() {
        chartJob?.cancel()
        _state.update { it.copy(selected = null) }
    }

    private fun loadChart(coin: Coin, period: String) {
        chartJob?.cancel()
        chartJob = scope.launch {
            val live = _state.value.live
            val pts = if (live) {
                runCatching { api.getChart(coin.id, period) }
                    .getOrElse { DemoData.chart(coin, period) }
            } else {
                DemoData.chart(coin, period)
            }
            _state.update {
                if (it.selected?.id == coin.id) {
                    it.copy(chart = it.chart.copy(period = period, points = pts, loading = false))
                } else it
            }
        }
    }

    fun close() {
        scope.cancel()
        api.close()
    }
}
