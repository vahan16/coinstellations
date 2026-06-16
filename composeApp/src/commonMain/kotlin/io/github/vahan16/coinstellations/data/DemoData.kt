package io.github.vahan16.coinstellations.data

import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

/**
 * Bundled snapshot so the app animates instantly with no API key (great for the
 * web demo and screenshots). Numbers are illustrative. Add a CoinStats key in
 * Settings for live data — https://coinstats.app/api/
 */
object DemoData {

    val global = MarketGlobal(
        marketCap = 2_410_000_000_000.0,
        volume = 96_500_000_000.0,
        btcDominance = 54.2,
        marketCapChange = 1.8,
    )

    private fun c(
        id: String, name: String, symbol: String, rank: Int,
        price: Double, marketCap: Double, volume: Double,
        avail: Double, total: Double,
        h: Double, d: Double, w: Double,
        web: String? = null, twitter: String? = null,
    ) = Coin(
        id = id, name = name, symbol = symbol, rank = rank,
        price = price, marketCap = marketCap, volume = volume,
        availableSupply = avail, totalSupply = total,
        priceChange1h = h, priceChange1d = d, priceChange1w = w,
        websiteUrl = web, twitterUrl = twitter,
    )

    private val topCoins: List<Coin> = listOf(
        c("bitcoin", "Bitcoin", "BTC", 1, 68120.0, 1_345_000_000_000.0, 32_400_000_000.0, 19.74e6, 21e6, 0.12, 1.42, -2.10, "https://bitcoin.org", "https://twitter.com/bitcoin"),
        c("ethereum", "Ethereum", "ETH", 2, 3552.0, 427_000_000_000.0, 16_800_000_000.0, 120.2e6, 120.2e6, 0.20, 2.05, 3.40, "https://ethereum.org", "https://twitter.com/ethereum"),
        c("tether", "Tether", "USDT", 3, 1.0, 112_000_000_000.0, 48_000_000_000.0, 112e9, 112e9, 0.00, 0.01, -0.02),
        c("binancecoin", "BNB", "BNB", 4, 596.0, 86_000_000_000.0, 1_900_000_000.0, 145e6, 145e6, 0.05, -0.80, 4.10),
        c("solana", "Solana", "SOL", 5, 158.4, 73_000_000_000.0, 3_300_000_000.0, 461e6, 580e6, 0.42, 3.21, 8.40),
        c("ripple", "XRP", "XRP", 6, 0.523, 29_000_000_000.0, 1_100_000_000.0, 55.5e9, 100e9, -0.10, -1.20, 1.60),
        c("usd-coin", "USD Coin", "USDC", 7, 1.0, 33_000_000_000.0, 6_400_000_000.0, 33e9, 33e9, 0.00, 0.00, 0.01),
        c("dogecoin", "Dogecoin", "DOGE", 8, 0.131, 19_000_000_000.0, 1_200_000_000.0, 145e9, 145e9, 0.90, 5.10, -3.20),
        c("cardano", "Cardano", "ADA", 9, 0.452, 16_000_000_000.0, 420_000_000.0, 35.4e9, 45e9, -0.22, 1.10, -1.40),
        c("tron", "TRON", "TRX", 10, 0.122, 10_600_000_000.0, 360_000_000.0, 87e9, 87e9, 0.08, 0.60, 2.20),
        c("avalanche-2", "Avalanche", "AVAX", 11, 36.2, 14_200_000_000.0, 520_000_000.0, 393e6, 720e6, 0.30, 2.80, -5.10),
        c("chainlink", "Chainlink", "LINK", 12, 17.8, 10_500_000_000.0, 480_000_000.0, 587e6, 1e9, 0.18, -2.10, 6.30),
        c("polkadot", "Polkadot", "DOT", 13, 7.1, 9_800_000_000.0, 280_000_000.0, 1.38e9, 1.45e9, -0.40, 1.70, -2.90),
        c("the-open-network", "Toncoin", "TON", 14, 7.6, 18_900_000_000.0, 420_000_000.0, 2.49e9, 5.1e9, 0.55, 4.30, 11.20),
        c("matic-network", "Polygon", "MATIC", 15, 0.72, 7_100_000_000.0, 360_000_000.0, 9.9e9, 10e9, -0.30, -1.90, -4.40),
        c("shiba-inu", "Shiba Inu", "SHIB", 16, 0.0000242, 14_300_000_000.0, 640_000_000.0, 589e12, 589e12, 1.20, 6.80, -2.10),
        c("litecoin", "Litecoin", "LTC", 17, 84.5, 6_300_000_000.0, 410_000_000.0, 74.6e6, 84e6, 0.10, 0.90, 1.30),
        c("bitcoin-cash", "Bitcoin Cash", "BCH", 18, 487.0, 9_600_000_000.0, 320_000_000.0, 19.7e6, 21e6, 0.25, 2.40, 5.70),
        c("uniswap", "Uniswap", "UNI", 19, 9.8, 5_900_000_000.0, 180_000_000.0, 600e6, 1e9, -0.15, 3.60, 9.10),
        c("near", "NEAR Protocol", "NEAR", 20, 6.2, 6_700_000_000.0, 240_000_000.0, 1.08e9, 1.2e9, 0.40, 2.10, 7.40),
        c("aptos", "Aptos", "APT", 21, 8.9, 4_300_000_000.0, 160_000_000.0, 483e6, 1.1e9, 0.30, -3.30, -6.80),
        c("stellar", "Stellar", "XLM", 22, 0.108, 3_200_000_000.0, 90_000_000.0, 29.6e9, 50e9, -0.10, 0.50, 2.90),
        c("cosmos", "Cosmos Hub", "ATOM", 23, 8.1, 3_100_000_000.0, 120_000_000.0, 390e6, 390e6, 0.20, 1.30, -1.10),
        c("monero", "Monero", "XMR", 24, 168.0, 3_000_000_000.0, 70_000_000.0, 18.4e6, 18.4e6, 0.05, 0.70, 3.10),
        c("filecoin", "Filecoin", "FIL", 25, 4.6, 2_700_000_000.0, 150_000_000.0, 587e6, 1.9e9, -0.25, -2.60, -7.20),
        c("hedera-hashgraph", "Hedera", "HBAR", 26, 0.082, 2_900_000_000.0, 80_000_000.0, 35.4e9, 50e9, 0.60, 4.90, 12.60),
        c("arbitrum", "Arbitrum", "ARB", 27, 0.78, 2_400_000_000.0, 210_000_000.0, 3.1e9, 10e9, 0.35, 3.10, -3.40),
        c("injective-protocol", "Injective", "INJ", 28, 24.3, 2_300_000_000.0, 130_000_000.0, 95e6, 100e6, 0.80, 5.60, 14.10),
        c("pepe", "Pepe", "PEPE", 29, 0.0000118, 4_900_000_000.0, 1_500_000_000.0, 415e12, 421e12, 2.10, 18.30, 27.50),
        c("render-token", "Render", "RNDR", 30, 7.4, 2_900_000_000.0, 170_000_000.0, 388e6, 531e6, 0.65, -4.10, 9.80),
    )

    /** A recognisable long tail so the demo sky is dense even without an API key. */
    private val extraDefs: List<Pair<String, String>> = listOf(
        "Sui" to "SUI", "Sei" to "SEI", "Aave" to "AAVE", "Maker" to "MKR", "Lido DAO" to "LDO",
        "THORChain" to "RUNE", "Algorand" to "ALGO", "Fantom" to "FTM", "Flow" to "FLOW", "MultiversX" to "EGLD",
        "Tezos" to "XTZ", "Theta" to "THETA", "Axie Infinity" to "AXS", "The Sandbox" to "SAND", "Decentraland" to "MANA",
        "Chiliz" to "CHZ", "Gala" to "GALA", "Enjin" to "ENJ", "Curve DAO" to "CRV", "Synthetix" to "SNX",
        "Compound" to "COMP", "dYdX" to "DYDX", "Kava" to "KAVA", "Zcash" to "ZEC", "Dash" to "DASH",
        "EOS" to "EOS", "Neo" to "NEO", "IOTA" to "IOTA", "Kusama" to "KSM", "PancakeSwap" to "CAKE",
        "1inch" to "1INCH", "Basic Attention" to "BAT", "Zilliqa" to "ZIL", "Quant" to "QNT", "Fetch.ai" to "FET",
        "The Graph" to "GRT", "Immutable" to "IMX", "Worldcoin" to "WLD", "Jupiter" to "JUP", "Pyth Network" to "PYTH",
        "Celestia" to "TIA", "Stacks" to "STX", "Ordinals" to "ORDI", "Bonk" to "BONK", "dogwifhat" to "WIF",
        "Floki" to "FLOKI", "STEPN" to "GMT", "ApeCoin" to "APE", "Loopring" to "LRC", "Oasis" to "ROSE",
        "Mina" to "MINA", "Arweave" to "AR", "Helium" to "HNT", "Conflux" to "CFX", "Kaspa" to "KAS",
        "Nexo" to "NEXO", "Gnosis" to "GNO", "Pendle" to "PENDLE", "Jito" to "JTO", "Blur" to "BLUR",
        "Ethereum Name Service" to "ENS", "Mask Network" to "MASK", "WOO" to "WOO", "Moonbeam" to "GLMR", "Astar" to "ASTR",
        "Osmosis" to "OSMO", "Celo" to "CELO", "Ankr" to "ANKR", "Audius" to "AUDIO", "Band Protocol" to "BAND",
        "Ocean Protocol" to "OCEAN", "Frax Share" to "FXS", "Rocket Pool" to "RPL", "Ondo" to "ONDO", "ether.fi" to "ETHFI",
        "Starknet" to "STRK", "Manta Network" to "MANTA", "Dymension" to "DYM", "Wormhole" to "W", "Ethena" to "ENA",
        "Omni Network" to "OMNI", "Saga" to "SAGA", "Bittensor" to "TAO", "Akash" to "AKT", "SafePal" to "SFP",
    )

    private fun extras(): List<Coin> {
        var mc = 2_050_000_000.0
        return extraDefs.mapIndexed { i, (name, sym) ->
            val rnd = Random(sym.hashCode().toLong() xor 0x5DEECE66DL)
            mc *= 0.90 + rnd.nextDouble() * 0.075 // gently decreasing market cap
            val price = 10.0.pow(rnd.nextInt(-4, 3)) * (1.0 + rnd.nextDouble() * 8.0)
            val avail = (mc / price).coerceAtLeast(1.0)
            Coin(
                id = sym.lowercase(),
                name = name,
                symbol = sym,
                rank = 30 + i + 1,
                price = price,
                marketCap = mc,
                volume = mc * (0.02 + rnd.nextDouble() * 0.18),
                availableSupply = avail,
                totalSupply = avail * (1.0 + rnd.nextDouble() * 1.4),
                priceChange1h = (rnd.nextDouble() - 0.5) * 3.0,
                priceChange1d = (rnd.nextDouble() - 0.46) * 15.0,
                priceChange1w = (rnd.nextDouble() - 0.46) * 30.0,
            )
        }
    }

    /** Curated majors + a generated long tail — ~110 coins so the sky is full. */
    val coins: List<Coin> = topCoins + extras()

    /** Deterministic synthetic price history for the detail chart in demo mode. */
    fun chart(coin: Coin, period: String): List<ChartPoint> {
        val n = when (period) {
            "24h" -> 48; "1w" -> 56; "1m" -> 60; "3m" -> 90; "6m" -> 120; "1y" -> 180; "all" -> 220; else -> 60
        }
        val end = coin.price ?: 1.0
        val changeFrac = ((coin.weeklyChange ?: coin.priceChange1d ?: 0.0) / 100.0)
        val start = end / max(0.05, 1.0 + changeFrac)
        val rnd = Random(coin.id.hashCode().toLong() xor period.hashCode().toLong())
        val pts = ArrayList<ChartPoint>(n)
        for (i in 0 until n) {
            val f = if (n == 1) 1.0 else i.toDouble() / (n - 1)
            val trend = start + (end - start) * f
            val noise = (rnd.nextDouble() - 0.5) * 0.06 * (1.0 - f * 0.5)
            pts.add(ChartPoint(i.toLong(), max(0.0, trend * (1.0 + noise))))
        }
        if (pts.isNotEmpty()) pts[pts.lastIndex] = ChartPoint((n - 1).toLong(), end)
        return pts
    }
}
