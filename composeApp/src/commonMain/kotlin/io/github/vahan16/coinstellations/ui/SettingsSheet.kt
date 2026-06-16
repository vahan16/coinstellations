package io.github.vahan16.coinstellations.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vahan16.coinstellations.platformLabel

private const val REPO_URL = "https://github.com/vahan16/coinstellations"
private const val API_URL = "https://coinstats.app/api/"
private const val GET_KEY_URL = "https://openapi.coinstats.app"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    hasApiKey: Boolean,
    live: Boolean,
    currency: String,
    starCount: Int,
    onSaveKey: (String?) -> Unit,
    onCurrency: (String) -> Unit,
    onStarCount: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uri = LocalUriHandler.current
    var keyText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Surface) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Settings", color = OnSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // --- API key ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CoinStats API key", color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (live) "● Live" else "● Demo data",
                        color = if (live) UpColor else Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    )
                }
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    singleLine = true,
                    placeholder = { Text(if (hasApiKey) "Saved — paste a new key to replace" else "Paste your X-API-KEY", color = Muted) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { onSaveKey(keyText.ifBlank { null }); keyText = "" }) {
                        Text("Save key")
                    }
                    if (hasApiKey) {
                        TextButton(onClick = { onSaveKey(null); keyText = "" }) { Text("Remove", color = DownColor) }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Get a free key ↗",
                        color = Accent, fontSize = 13.sp,
                        modifier = Modifier.clickable { runCatching { uri.openUri(GET_KEY_URL) } }.padding(vertical = 10.dp),
                    )
                }
            }

            // --- Preferences ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Currency", color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                PillBar(listOf("USD", "EUR", "GBP", "BTC"), currency, { it }, onCurrency)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Stars in the sky", color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                PillBar(listOf(25, 50, 100, 150), starCount, { it.toString() }, onStarCount)
            }

            // --- About ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("About", color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Coinstellations renders the live crypto market as a night sky — every coin is a star.",
                    color = Muted, fontSize = 13.sp,
                )
                Text("Compose Multiplatform · running on $platformLabel · v1.0.0", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        "CoinStats API ↗", color = Accent, fontSize = 13.sp,
                        modifier = Modifier.clickable { runCatching { uri.openUri(API_URL) } },
                    )
                    Text(
                        "Source on GitHub ↗", color = Accent, fontSize = 13.sp,
                        modifier = Modifier.clickable { runCatching { uri.openUri(REPO_URL) } },
                    )
                }
            }
        }
    }
}
