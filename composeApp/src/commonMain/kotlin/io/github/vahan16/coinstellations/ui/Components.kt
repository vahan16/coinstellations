package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A coin's "star" rendered as a glowing monogram disc. */
@Composable
fun Monogram(symbol: String, color: Color, diameter: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(diameter)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.30f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (diameter.value * 0.42f).sp,
        )
    }
}

/** Compact rounded segmented selector. */
@Composable
fun <T> PillBar(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { opt ->
            val sel = opt == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (sel) Accent else Color.Transparent)
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 13.dp, vertical = 6.dp),
            ) {
                Text(
                    text = label(opt),
                    color = if (sel) Color(0xFF06122B) else Muted,
                    fontSize = 13.sp,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ChangeChip(label: String, value: Double?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(formatPercent(value), color = changeColor(value), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LinkChip(label: String, url: String, modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { runCatching { uri.openUri(url) } }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, color = OnSurface, fontSize = 13.sp)
        ExternalLinkIcon(Modifier.size(13.dp))
    }
}
