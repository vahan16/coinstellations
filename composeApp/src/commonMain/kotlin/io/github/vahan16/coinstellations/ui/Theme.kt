package io.github.vahan16.coinstellations.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// --- Cosmic palette ---
val SpaceTop = Color(0xFF0C1230)
val SpaceMid = Color(0xFF080A1C)
val SpaceBottom = Color(0xFF04050B)
val Surface = Color(0xFF121734)
val OnSurface = Color(0xFFE7ECFF)
val Muted = Color(0xFF8A93B8)
val Accent = Color(0xFF8AB4FF)

val UpColor = Color(0xFF21D07A)
val DownColor = Color(0xFFFF5C6C)
val StarWarm = Color(0xFFFFE3A3) // gainers glow warm gold
val StarCool = Color(0xFF8FB8FF) // losers glow cool blue
val StarNeutral = Color(0xFFE9EEFF)

/** Map a price-change % to a star tint: warm/green up, cool/red down. */
fun changeColor(pct: Double?): Color {
    val p = (pct ?: 0.0).toFloat()
    val t = (p / 8f).coerceIn(-1f, 1f)
    return if (t >= 0f) lerp(StarNeutral, UpColor, t * 0.85f)
    else lerp(StarNeutral, DownColor, -t * 0.85f)
}

private val Scheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF06122B),
    secondary = StarWarm,
    background = SpaceBottom,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFF1B2147),
    onSurfaceVariant = Muted,
    outline = Color(0xFF2A3360),
    error = DownColor,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = Typography(),
        content = content,
    )
}
