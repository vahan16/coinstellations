package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.vahan16.coinstellations.data.ChartPoint

/** Minimal area + line chart drawn straight onto a Canvas. */
@Composable
fun Sparkline(
    points: List<ChartPoint>,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    val line = if (positive) UpColor else DownColor
    Canvas(modifier) {
        if (points.size < 2) return@Canvas
        val prices = points.map { it.price.toFloat() }
        val mn = prices.minOrNull() ?: return@Canvas
        val mx = prices.maxOrNull() ?: return@Canvas
        val range = (mx - mn).let { if (it <= 0f) 1f else it }
        val w = size.width
        val h = size.height
        val padTop = h * 0.10f
        val usable = h * 0.82f
        val n = points.size

        fun px(i: Int) = w * i / (n - 1)
        fun py(p: Float) = padTop + usable * (1f - (p - mn) / range)

        val path = Path().apply {
            moveTo(0f, py(prices[0]))
            for (i in 1 until n) lineTo(px(i), py(prices[i]))
        }
        val fill = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(line.copy(alpha = 0.30f), Color.Transparent)))
        drawPath(path, color = line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(line, 3.dp.toPx(), Offset(px(n - 1), py(prices.last())))
        drawCircle(Color.White, 1.4f, Offset(px(n - 1), py(prices.last())))
    }
}
