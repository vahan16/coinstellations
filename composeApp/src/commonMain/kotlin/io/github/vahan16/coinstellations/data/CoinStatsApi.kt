package io.github.vahan16.coinstellations.data

import io.github.vahan16.coinstellations.platformEngine
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Thin client for the CoinStats Open API — https://coinstats.app/api/
 *
 * Auth is a single `X-API-KEY` header. The key is read lazily on every call so
 * the user can paste one into Settings at runtime without rebuilding the client.
 */
class CoinStatsApi(
    private val apiKeyProvider: () -> String?,
    private val client: HttpClient = defaultClient(),
) {
    suspend fun getCoins(limit: Int = 100, currency: String = "USD"): List<Coin> =
        client.get("$BASE/coins") {
            auth()
            parameter("limit", limit)
            parameter("currency", currency)
        }.body<CoinsResponse>().result

    suspend fun getGlobalMarket(): MarketGlobal =
        client.get("$BASE/markets") { auth() }.body()

    /** Chart data is a JSON array of `[timestampSec, price, ...]` tuples. */
    suspend fun getChart(coinId: String, period: String): List<ChartPoint> {
        val rows: List<List<Double>> = client.get("$BASE/coins/$coinId/charts") {
            auth()
            parameter("period", period)
        }.body()
        return rows.mapNotNull { row ->
            if (row.size >= 2) ChartPoint(row[0].toLong(), row[1]) else null
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.auth() {
        header("X-API-KEY", apiKeyProvider().orEmpty())
    }

    fun close() = client.close()

    companion object {
        const val BASE = "https://openapiv1.coinstats.app"

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            explicitNulls = false
        }

        fun defaultClient(): HttpClient = HttpClient(platformEngine()) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 15_000
            }
        }
    }
}
