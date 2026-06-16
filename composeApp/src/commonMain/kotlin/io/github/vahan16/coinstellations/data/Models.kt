package io.github.vahan16.coinstellations.data

import kotlinx.serialization.Serializable

/**
 * A coin as returned by the CoinStats `/coins` endpoint. Every field is optional
 * so a missing/renamed key in the upstream payload never crashes deserialization.
 */
@Serializable
data class Coin(
    val id: String = "",
    val icon: String? = null,
    val name: String = "",
    val symbol: String = "",
    val rank: Int? = null,
    val price: Double? = null,
    val priceBtc: Double? = null,
    val volume: Double? = null,
    val marketCap: Double? = null,
    val availableSupply: Double? = null,
    val totalSupply: Double? = null,
    val fullyDilutedValuation: Double? = null,
    val priceChange1h: Double? = null,
    val priceChange1d: Double? = null,
    // CoinStats returns the weekly change as `priceChange1w`; some payloads use `priceChange7d`.
    val priceChange1w: Double? = null,
    val priceChange7d: Double? = null,
    val websiteUrl: String? = null,
    val twitterUrl: String? = null,
    val redditUrl: String? = null,
    val explorers: List<String>? = null,
) {
    val weeklyChange: Double? get() = priceChange1w ?: priceChange7d
}

@Serializable
data class CoinsResponse(
    val result: List<Coin> = emptyList(),
    val meta: Meta? = null,
)

@Serializable
data class Meta(
    val page: Int? = null,
    val limit: Int? = null,
    val itemCount: Int? = null,
    val pageCount: Int? = null,
)

/** Global market snapshot from `/markets`. */
@Serializable
data class MarketGlobal(
    val marketCap: Double? = null,
    val volume: Double? = null,
    val btcDominance: Double? = null,
    val marketCapChange: Double? = null,
)

/** One point of a price chart: epoch-seconds timestamp + price. */
data class ChartPoint(val timeSec: Long, val price: Double)
