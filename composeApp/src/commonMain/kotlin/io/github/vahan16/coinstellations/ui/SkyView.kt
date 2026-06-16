package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vahan16.coinstellations.Timeframe
import io.github.vahan16.coinstellations.changeFor
import io.github.vahan16.coinstellations.data.Coin
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val GOLDEN_ANGLE = 2.399963f // radians, ~137.5°

private class Star(
    val coin: Coin,
    val nx: Float,            // normalized 0..1 position
    val ny: Float,
    val radiusPx: Float,
    val color: Color,
    val twinkleSpeed: Float,
    val twinklePhase: Float,
    val major: Boolean,       // draw a symbol label
)

private class BgStar(val nx: Float, val ny: Float, val r: Float, val baseAlpha: Float, val speed: Float, val phase: Float)

private class Shooting(var x: Float, var y: Float, val vx: Float, val vy: Float, var life: Float, val maxLife: Float, val len: Float, val color: Color)

/**
 * The market as a night sky. Each coin is a star (size = market cap, color = move,
 * twinkle = volatility); nearby stars are linked into constellations and big movers
 * streak across as shooting stars. Tap a star to inspect it.
 */
@Composable
fun SkyView(
    coins: List<Coin>,
    timeframe: Timeframe,
    modifier: Modifier = Modifier,
    onSelect: (Coin) -> Unit,
) {
    var sizePx by remember { mutableStateOf(Size.Zero) }
    var nowSec by remember { mutableStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()

    val bgStars = remember { buildBackgroundStars(count = 170) }

    val stars = remember(coins, timeframe, sizePx) {
        if (sizePx.minDimension <= 0f) emptyList() else buildStars(coins, timeframe, sizePx)
    }
    val edges = remember(stars) { buildEdges(stars) }
    val shooting = remember { mutableListOf<Shooting>() }

    val topMover = remember(coins, timeframe) {
        coins.maxByOrNull { abs(it.changeFor(timeframe) ?: 0.0) }
    }

    LaunchedEffect(sizePx) {
        if (sizePx.minDimension <= 0f) return@LaunchedEffect
        var last = 0L
        var spawnAcc = 1.0f
        val rnd = Random(7)
        while (true) {
            withFrameNanos { t ->
                if (last == 0L) last = t
                val dt = ((t - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                last = t
                nowSec += dt

                val iter = shooting.iterator()
                while (iter.hasNext()) {
                    val s = iter.next()
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.life -= dt
                    if (s.life <= 0f) iter.remove()
                }
                spawnAcc += dt
                if (spawnAcc > 2.4f && shooting.size < 3) {
                    spawnAcc = 0f
                    shooting += spawnShooting(sizePx, rnd, (topMover?.changeFor(timeframe) ?: 0.0) >= 0)
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { sizePx = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(stars, sizePx) {
                detectTapGestures { pos -> pickStar(stars, pos, sizePx)?.let(onSelect) }
            }
    ) {
        val t = nowSec // subscribe to the animation clock
        val w = size.width
        val h = size.height
        val minDim = size.minDimension

        // Deep-space gradient
        drawRect(Brush.verticalGradient(listOf(SpaceTop, SpaceMid, SpaceBottom)))
        // Faint nebula glow
        val nebula = Offset(w * 0.72f, h * 0.22f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Accent.copy(alpha = 0.12f), Color.Transparent),
                center = nebula, radius = minDim * 0.7f,
            ),
            radius = minDim * 0.7f, center = nebula,
        )

        // Background starfield
        bgStars.forEach { b ->
            val a = (b.baseAlpha * (0.55f + 0.45f * sin(t * b.speed + b.phase))).coerceIn(0f, 1f)
            drawCircle(Color.White.copy(alpha = a), b.r, Offset(b.nx * w, b.ny * h))
        }

        // Constellation lines
        edges.forEach { (a, b) ->
            drawLine(
                color = Accent.copy(alpha = 0.10f),
                start = Offset(a.nx * w, a.ny * h),
                end = Offset(b.nx * w, b.ny * h),
                strokeWidth = 1f,
            )
        }

        // Shooting stars (tail -> head)
        shooting.forEach { s ->
            val k = (s.life / s.maxLife).coerceIn(0f, 1f)
            val dir = hypot(s.vx, s.vy).coerceAtLeast(1f)
            val tail = Offset(s.x - s.vx / dir * s.len, s.y - s.vy / dir * s.len)
            drawLine(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, s.color.copy(alpha = 0.9f * k)),
                    start = tail, end = Offset(s.x, s.y),
                ),
                start = tail, end = Offset(s.x, s.y), strokeWidth = 2f,
            )
            drawCircle(s.color.copy(alpha = k), 2.2f, Offset(s.x, s.y))
        }

        // Coin stars
        stars.forEach { star ->
            val pos = Offset(star.nx * w, star.ny * h)
            val tw = 0.72f + 0.28f * sin(t * star.twinkleSpeed + star.twinklePhase)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(star.color.copy(alpha = 0.5f * tw), Color.Transparent),
                    center = pos, radius = star.radiusPx * 3.4f,
                ),
                radius = star.radiusPx * 3.4f, center = pos,
            )
            drawCircle(star.color.copy(alpha = (0.85f * tw + 0.15f).coerceIn(0f, 1f)), star.radiusPx, pos)
            drawCircle(Color.White.copy(alpha = 0.45f * tw), star.radiusPx * 0.42f, pos)

            if (star.major) {
                val layout = textMeasurer.measure(
                    star.coin.symbol,
                    style = TextStyle(color = OnSurface.copy(alpha = 0.82f), fontSize = 11.sp, fontWeight = FontWeight.Medium),
                )
                drawText(
                    layout,
                    topLeft = Offset(pos.x - layout.size.width / 2f, pos.y + star.radiusPx + 3.dp.toPx()),
                )
            }
        }
    }
}

// --- layout & helpers (pure) ---

private fun buildStars(coins: List<Coin>, tf: Timeframe, size: Size): List<Star> {
    if (coins.isEmpty()) return emptyList()
    val minDim = size.minDimension
    val maxMc = coins.maxOf { it.marketCap ?: 1.0 }.coerceAtLeast(1.0)
    val maxR = minDim * 0.052f
    val minR = minDim * 0.009f
    val n = coins.size
    val spread = 0.46f
    return coins.mapIndexed { i, coin ->
        val angle = i * GOLDEN_ANGLE
        val rad = sqrt(i.toFloat() / n)
        val nx = 0.5f + rad * cos(angle) * spread
        val ny = 0.5f + rad * sin(angle) * spread
        val mcNorm = sqrt(((coin.marketCap ?: 1.0) / maxMc).toFloat()).coerceIn(0f, 1f)
        val change = coin.changeFor(tf) ?: 0.0
        val rnd = Random(coin.id.hashCode().toLong())
        Star(
            coin = coin,
            nx = nx.coerceIn(0.04f, 0.96f),
            ny = ny.coerceIn(0.04f, 0.96f),
            radiusPx = (minR + (maxR - minR) * mcNorm),
            color = changeColor(change),
            twinkleSpeed = 0.6f + min(3.0, abs(change) / 3.0).toFloat(),
            twinklePhase = rnd.nextFloat() * 6.2832f,
            major = i < 12,
        )
    }
}

private fun buildEdges(stars: List<Star>): List<Pair<Star, Star>> {
    if (stars.size < 2) return emptyList()
    val seen = HashSet<Long>()
    val edges = ArrayList<Pair<Star, Star>>()
    for (i in stars.indices) {
        var best = -1
        var bestD = Float.MAX_VALUE
        for (j in stars.indices) {
            if (i == j) continue
            val d = hypot(stars[i].nx - stars[j].nx, stars[i].ny - stars[j].ny)
            if (d < bestD) { bestD = d; best = j }
        }
        if (best >= 0) {
            val key = (minOf(i, best).toLong() shl 20) or maxOf(i, best).toLong()
            if (seen.add(key)) edges += stars[i] to stars[best]
        }
    }
    return edges
}

private fun buildBackgroundStars(count: Int): List<BgStar> {
    val rnd = Random(42)
    return List(count) {
        BgStar(
            nx = rnd.nextFloat(), ny = rnd.nextFloat(),
            r = 0.5f + rnd.nextFloat() * 1.4f,
            baseAlpha = 0.18f + rnd.nextFloat() * 0.5f,
            speed = 0.3f + rnd.nextFloat() * 1.3f,
            phase = rnd.nextFloat() * 6.2832f,
        )
    }
}

private fun spawnShooting(size: Size, rnd: Random, warm: Boolean): Shooting {
    val fromLeft = rnd.nextBoolean()
    val startX = if (fromLeft) -20f else size.width * rnd.nextFloat()
    val startY = size.height * rnd.nextFloat() * 0.5f
    val speed = size.minDimension * (0.5f + rnd.nextFloat() * 0.4f)
    val angle = 0.35f + rnd.nextFloat() * 0.3f // gently downward-right
    val life = 0.9f + rnd.nextFloat() * 0.5f
    return Shooting(
        x = startX, y = startY,
        vx = cos(angle) * speed, vy = sin(angle) * speed,
        life = life, maxLife = life,
        len = size.minDimension * 0.13f,
        color = if (warm) StarWarm else StarCool,
    )
}

private fun pickStar(stars: List<Star>, pos: Offset, size: Size): Coin? {
    if (stars.isEmpty() || size.minDimension <= 0f) return null
    var best: Star? = null
    var bestD = Float.MAX_VALUE
    for (s in stars) {
        val sx = s.nx * size.width
        val sy = s.ny * size.height
        val d = hypot(pos.x - sx, pos.y - sy)
        val hit = s.radiusPx + 16f
        if (d <= hit && d < bestD) { bestD = d; best = s }
    }
    return best?.coin
}
