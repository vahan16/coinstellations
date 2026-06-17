package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val LAYOUT_R = 0.60f          // scatter radius as fraction of min dimension
private const val AUTO_SPIN = 0.05f         // idle rotation, radians/sec
private const val MIN_SCALE = 0.35f
private const val MAX_SCALE = 4f
private const val DEFAULT_SCALE = 0.7f
private const val ROT_EASE = 5f   // how quickly rotation catches up to input (lower = more lag)
private const val ZOOM_EASE = 6f  // how quickly zoom catches up to input

private class Star(
    val coin: Coin,
    val ang: Float,          // layout angle (rad) — rotated by the user
    val radFrac: Float,      // 0..1 distance from centre
    val radiusPx: Float,
    val color: Color,
    val twinkleSpeed: Float,
    val twinklePhase: Float,
    val depth: Float,        // 0 (far) .. 1 (near) — parallax weight
)

private class BgStar(val nx: Float, val ny: Float, val r: Float, val baseAlpha: Float, val speed: Float, val phase: Float, val depth: Float)

private class Shooting(var x: Float, var y: Float, val vx: Float, val vy: Float, var life: Float, val maxLife: Float, val len: Float, val color: Color)

/** Shared interaction state: pointer parallax + drag rotation. */
private class Sky {
    var targetX = 0f; var targetY = 0f // parallax target (-1..1)
    var x = 0f; var y = 0f             // smoothed parallax
    var rot = 0f; var rotTarget = 0f                       // galaxy rotation (radians), eased
    var scale = DEFAULT_SCALE; var scaleTarget = DEFAULT_SCALE // zoom, eased
    var lastInputSec = -10f
}

/**
 * The market as a night sky. Each coin is a star (size = market cap, colour = move,
 * twinkle = volatility), laid out as a golden-angle galaxy.
 *
 * Interaction:
 *  • **Drag** anywhere to spin the whole galaxy around its centre.
 *  • **Move/hover** to tilt it in 3D — near (large-cap) stars parallax more than far ones; the hovered star flares.
 *  • **Tap** a star to inspect it. Idle, it slowly spins and drifts on its own.
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
    val sky = remember { Sky() }

    val bgStars = remember { buildBackgroundStars(count = 170) }
    val sessionSeed = remember { Random.nextInt() } // fresh scatter on every launch
    val stars = remember(coins, timeframe, sizePx, sessionSeed) {
        if (sizePx.minDimension <= 0f) emptyList() else buildStars(coins, timeframe, sizePx, sessionSeed)
    }
    val edges = remember(stars) { buildEdges(stars) }
    val shooting = remember { mutableListOf<Shooting>() }
    val topMover = remember(coins, timeframe) { coins.maxByOrNull { abs(it.changeFor(timeframe) ?: 0.0) } }

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

                // Eased rotation + zoom — input nudges the target, the value glides to it.
                sky.rotTarget += AUTO_SPIN * dt // gentle idle spin (drag adds on top)
                sky.rot += (sky.rotTarget - sky.rot) * min(1f, dt * ROT_EASE)
                sky.scale += (sky.scaleTarget - sky.scale) * min(1f, dt * ZOOM_EASE)

                val idle = nowSec - sky.lastInputSec > 2.5f
                val desiredX = if (idle) sin(nowSec * 0.18f) * 0.5f else sky.targetX
                val desiredY = if (idle) cos(nowSec * 0.13f) * 0.32f else sky.targetY
                val k = min(1f, dt * 3.5f)
                sky.x += (desiredX - sky.x) * k
                sky.y += (desiredY - sky.y) * k

                val iter = shooting.iterator()
                while (iter.hasNext()) {
                    val s = iter.next()
                    s.x += s.vx * dt; s.y += s.vy * dt; s.life -= dt
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

    Box(modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize()
            .onSizeChanged { sizePx = Size(it.width.toFloat(), it.height.toFloat()) }
            // hover → parallax (ignore while a finger/button is pressed; that's a rotate)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val c = event.changes.lastOrNull() ?: continue
                        if (c.scrollDelta != Offset.Zero) { // mouse wheel / trackpad → zoom (eased)
                            sky.scaleTarget = (sky.scaleTarget * (1f - c.scrollDelta.y * 0.05f)).coerceIn(MIN_SCALE, MAX_SCALE)
                            sky.lastInputSec = nowSec
                            c.consume()
                        }
                        if (!c.pressed && size.width > 0 && size.height > 0) {
                            sky.targetX = ((c.position.x / size.width) * 2f - 1f).coerceIn(-1f, 1f)
                            sky.targetY = ((c.position.y / size.height) * 2f - 1f).coerceIn(-1f, 1f)
                            sky.lastInputSec = nowSec
                        }
                    }
                }
            }
            // drag → rotate (one finger) · pinch → zoom · twist → rotate (two fingers)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoomChange, rotationDeg ->
                    val c = Offset(size.width / 2f, size.height / 2f)
                    if (zoomChange != 1f) sky.scaleTarget = (sky.scaleTarget * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
                    if (rotationDeg != 0f) sky.rotTarget += rotationDeg * (PI.toFloat() / 180f)
                    val cur = centroid - c
                    val prev = (centroid - pan) - c
                    if (cur.getDistance() > 8f && prev.getDistance() > 8f) {
                        var da = atan2(cur.y, cur.x) - atan2(prev.y, prev.x)
                        if (da > PI) da -= (2 * PI).toFloat()
                        if (da < -PI) da += (2 * PI).toFloat()
                        sky.rotTarget += da
                    }
                    sky.lastInputSec = nowSec
                }
            }
            .pointerInput(stars, sizePx) {
                detectTapGestures { pos -> pickStar(stars, pos, sizePx, sky)?.let(onSelect) }
            }
    ) {
        nowSec // subscribe to the animation clock
        val w = size.width
        val h = size.height
        val minDim = size.minDimension
        val cx = w / 2f
        val cy = h / 2f
        val layoutR = minDim * LAYOUT_R * sky.scale
        val shift = minDim * 0.05f
        val active = nowSec - sky.lastInputSec <= 2.5f
        val pointer = Offset((sky.targetX + 1f) / 2f * w, (sky.targetY + 1f) / 2f * h)

        fun starPos(s: Star): Offset {
            val a = s.ang + sky.rot
            return Offset(
                cx + cos(a) * s.radFrac * layoutR + sky.x * s.depth * shift,
                cy + sin(a) * s.radFrac * layoutR + sky.y * s.depth * shift,
            )
        }

        // Deep-space gradient + nebula
        drawRect(Brush.verticalGradient(listOf(SpaceTop, SpaceMid, SpaceBottom)))
        val nebula = Offset(w * 0.72f + sky.x * shift, h * 0.22f + sky.y * shift)
        drawCircle(Brush.radialGradient(listOf(Accent.copy(alpha = 0.12f), Color.Transparent), center = nebula, radius = minDim * 0.7f), radius = minDim * 0.7f, center = nebula)

        // Background starfield (does not rotate; far parallax only)
        bgStars.forEach { b ->
            val a = (b.baseAlpha * (0.55f + 0.45f * sin(nowSec * b.speed + b.phase))).coerceIn(0f, 1f)
            drawCircle(Color.White.copy(alpha = a), b.r, Offset(b.nx * w + sky.x * b.depth * shift, b.ny * h + sky.y * b.depth * shift))
        }

        // Positions (rotated) for the coin stars
        val pos = stars.map { starPos(it) }

        // Constellation lines
        edges.forEach { (i, j) ->
            drawLine(Accent.copy(alpha = 0.10f), pos[i], pos[j], strokeWidth = 1f)
        }

        // Shooting stars
        shooting.forEach { s ->
            val kf = (s.life / s.maxLife).coerceIn(0f, 1f)
            val dir = hypot(s.vx, s.vy).coerceAtLeast(1f)
            val tail = Offset(s.x - s.vx / dir * s.len, s.y - s.vy / dir * s.len)
            drawLine(Brush.linearGradient(listOf(Color.Transparent, s.color.copy(alpha = 0.9f * kf)), start = tail, end = Offset(s.x, s.y)), start = tail, end = Offset(s.x, s.y), strokeWidth = 2f)
            drawCircle(s.color.copy(alpha = kf), 2.2f, Offset(s.x, s.y))
        }

        // Coin stars
        stars.forEachIndexed { idx, star ->
            val p = pos[idx]
            var tw = 0.72f + 0.28f * sin(nowSec * star.twinkleSpeed + star.twinklePhase)
            val base = star.radiusPx * sky.scale
            var rad = base
            if (active) {
                val d = hypot(pointer.x - p.x, pointer.y - p.y)
                val reach = base * 4.5f + 28f
                if (d < reach) {
                    val boost = 1f - d / reach
                    tw = (tw + boost * 0.6f).coerceAtMost(1.25f)
                    rad = base * (1f + boost * 0.35f)
                }
            }
            drawCircle(Brush.radialGradient(listOf(star.color.copy(alpha = 0.5f * tw), Color.Transparent), center = p, radius = rad * 3.4f), radius = rad * 3.4f, center = p)
            drawCircle(star.color.copy(alpha = (0.85f * tw + 0.15f).coerceIn(0f, 1f)), rad, p)
            drawCircle(Color.White.copy(alpha = 0.45f * tw), rad * 0.42f, p)

            // Reveal the coin's symbol once the star is big enough on screen — so
            // zooming in fades in more names (BTC/ETH show even at the default zoom).
            val labelThreshold = minDim * 0.016f
            if (rad >= labelThreshold) {
                val labelAlpha = (((rad / labelThreshold) - 1f) * 2.5f).coerceIn(0f, 1f) * 0.85f
                val layout = textMeasurer.measure(
                    star.coin.symbol,
                    style = TextStyle(color = OnSurface.copy(alpha = labelAlpha), fontSize = 11.sp, fontWeight = FontWeight.Medium),
                )
                drawText(layout, topLeft = Offset(p.x - layout.size.width / 2f, p.y + rad + 3.dp.toPx()))
            }
        }
    }

        ZoomControls(
            onIn = { sky.scaleTarget = (sky.scaleTarget * 1.4f).coerceIn(MIN_SCALE, MAX_SCALE); sky.lastInputSec = nowSec },
            onOut = { sky.scaleTarget = (sky.scaleTarget / 1.4f).coerceIn(MIN_SCALE, MAX_SCALE); sky.lastInputSec = nowSec },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@Composable
private fun ZoomControls(onIn: () -> Unit, onOut: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ZoomButton("+", onIn)
        ZoomButton("−", onOut)
    }
}

@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = OnSurface, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

// --- layout & helpers (pure) ---

private fun buildStars(coins: List<Coin>, tf: Timeframe, size: Size, seed: Int): List<Star> {
    if (coins.isEmpty()) return emptyList()
    val minDim = size.minDimension
    val maxMc = coins.maxOf { it.marketCap ?: 1.0 }.coerceAtLeast(1.0)
    val maxR = minDim * 0.052f
    val minR = minDim * 0.009f
    return coins.mapIndexed { i, coin ->
        val mcNorm = sqrt(((coin.marketCap ?: 1.0) / maxMc).toFloat()).coerceIn(0f, 1f)
        val change = coin.changeFor(tf) ?: 0.0
        val rnd = Random((coin.id.hashCode().toLong() + i * 31L) xor (seed.toLong() shl 21))
        Star(
            coin = coin,
            ang = rnd.nextFloat() * 6.2832f,                  // random angle
            radFrac = sqrt(rnd.nextFloat()),                  // area-uniform random radius
            radiusPx = (minR + (maxR - minR) * mcNorm),
            color = changeColor(change),
            twinkleSpeed = 0.6f + min(3.0, abs(change) / 3.0).toFloat(),
            twinklePhase = rnd.nextFloat() * 6.2832f,
            depth = 0.3f + 0.7f * mcNorm,
        )
    }
}

private fun buildEdges(stars: List<Star>): List<Pair<Int, Int>> {
    if (stars.size < 2) return emptyList()
    fun xy(s: Star) = Offset(cos(s.ang) * s.radFrac, sin(s.ang) * s.radFrac)
    val seen = HashSet<Long>()
    val edges = ArrayList<Pair<Int, Int>>()
    for (i in stars.indices) {
        val a = xy(stars[i])
        var best = -1
        var bestD = Float.MAX_VALUE
        for (j in stars.indices) {
            if (i == j) continue
            val b = xy(stars[j])
            val d = hypot(a.x - b.x, a.y - b.y)
            if (d < bestD) { bestD = d; best = j }
        }
        if (best >= 0) {
            val key = (minOf(i, best).toLong() shl 20) or maxOf(i, best).toLong()
            if (seen.add(key)) edges += i to best
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
            depth = 0.04f + rnd.nextFloat() * 0.18f,
        )
    }
}

private fun spawnShooting(size: Size, rnd: Random, warm: Boolean): Shooting {
    val fromLeft = rnd.nextBoolean()
    val startX = if (fromLeft) -20f else size.width * rnd.nextFloat()
    val startY = size.height * rnd.nextFloat() * 0.5f
    val speed = size.minDimension * (0.5f + rnd.nextFloat() * 0.4f)
    val angle = 0.35f + rnd.nextFloat() * 0.3f
    val life = 0.9f + rnd.nextFloat() * 0.5f
    return Shooting(
        x = startX, y = startY,
        vx = cos(angle) * speed, vy = sin(angle) * speed,
        life = life, maxLife = life,
        len = size.minDimension * 0.13f,
        color = if (warm) StarWarm else StarCool,
    )
}

private fun pickStar(stars: List<Star>, tap: Offset, size: Size, sky: Sky): Coin? {
    if (stars.isEmpty() || size.minDimension <= 0f) return null
    val minDim = size.minDimension
    val cx = size.width / 2f
    val cy = size.height / 2f
    val layoutR = minDim * LAYOUT_R * sky.scale
    val shift = minDim * 0.05f
    var best: Star? = null
    var bestD = Float.MAX_VALUE
    for (s in stars) {
        val a = s.ang + sky.rot
        val sx = cx + cos(a) * s.radFrac * layoutR + sky.x * s.depth * shift
        val sy = cy + sin(a) * s.radFrac * layoutR + sky.y * s.depth * shift
        val d = hypot(tap.x - sx, tap.y - sy)
        if (d <= s.radiusPx * sky.scale + 16f && d < bestD) { bestD = d; best = s }
    }
    return best?.coin
}
