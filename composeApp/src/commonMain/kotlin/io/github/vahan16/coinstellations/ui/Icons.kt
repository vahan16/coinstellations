package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/** Tiny hand-drawn icon set so we depend on no icon font/library (keeps the Wasm bundle lean). */

@Composable
fun GearIcon(modifier: Modifier = Modifier, tint: Color = OnSurface) = Canvas(modifier) {
    val c = Offset(size.width / 2, size.height / 2)
    val r = size.minDimension * 0.30f
    val sw = size.minDimension * 0.11f
    val stroke = Stroke(width = sw, cap = StrokeCap.Round)
    drawCircle(tint, r, c, style = stroke)
    drawCircle(tint, size.minDimension * 0.07f, c)
    repeat(8) { i ->
        val a = i * (6.2832f / 8)
        val inner = Offset(c.x + cos(a) * r * 1.05f, c.y + sin(a) * r * 1.05f)
        val outer = Offset(c.x + cos(a) * r * 1.55f, c.y + sin(a) * r * 1.55f)
        drawLine(tint, inner, outer, strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun SearchIcon(modifier: Modifier = Modifier, tint: Color = OnSurface) = Canvas(modifier) {
    val sw = size.minDimension * 0.10f
    val r = size.minDimension * 0.27f
    val c = Offset(size.width * 0.42f, size.height * 0.42f)
    drawCircle(tint, r, c, style = Stroke(width = sw, cap = StrokeCap.Round))
    drawLine(
        tint,
        Offset(c.x + r * 0.72f, c.y + r * 0.72f),
        Offset(size.width * 0.84f, size.height * 0.84f),
        strokeWidth = sw, cap = StrokeCap.Round,
    )
}

@Composable
fun CloseIcon(modifier: Modifier = Modifier, tint: Color = OnSurface) = Canvas(modifier) {
    val sw = size.minDimension * 0.11f
    val a = size.minDimension * 0.28f
    val c = Offset(size.width / 2, size.height / 2)
    drawLine(tint, Offset(c.x - a, c.y - a), Offset(c.x + a, c.y + a), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(tint, Offset(c.x + a, c.y - a), Offset(c.x - a, c.y + a), strokeWidth = sw, cap = StrokeCap.Round)
}

@Composable
fun RefreshIcon(modifier: Modifier = Modifier, tint: Color = OnSurface) = Canvas(modifier) {
    val sw = size.minDimension * 0.10f
    val r = size.minDimension * 0.30f
    val c = Offset(size.width / 2, size.height / 2)
    drawArc(
        color = tint,
        startAngle = -50f, sweepAngle = 280f, useCenter = false,
        topLeft = Offset(c.x - r, c.y - r),
        size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        style = Stroke(width = sw, cap = StrokeCap.Round),
    )
    // arrowhead at the arc opening (top-right)
    val a = -50f * 0.0174533f
    val head = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
    val s = size.minDimension * 0.16f
    drawLine(tint, head, Offset(head.x - s, head.y), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(tint, head, Offset(head.x, head.y - s), strokeWidth = sw, cap = StrokeCap.Round)
}

@Composable
fun ExternalLinkIcon(modifier: Modifier = Modifier, tint: Color = Accent) = Canvas(modifier) {
    val sw = size.minDimension * 0.10f
    val start = Offset(size.width * 0.3f, size.height * 0.7f)
    val end = Offset(size.width * 0.72f, size.height * 0.28f)
    drawLine(tint, start, end, strokeWidth = sw, cap = StrokeCap.Round)
    val s = size.minDimension * 0.22f
    drawLine(tint, end, Offset(end.x - s, end.y), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(tint, end, Offset(end.x, end.y + s), strokeWidth = sw, cap = StrokeCap.Round)
}
