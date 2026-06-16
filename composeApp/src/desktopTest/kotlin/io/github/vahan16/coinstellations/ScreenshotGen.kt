package io.github.vahan16.coinstellations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vahan16.coinstellations.data.DemoData
import io.github.vahan16.coinstellations.ui.AppTheme
import io.github.vahan16.coinstellations.ui.Muted
import io.github.vahan16.coinstellations.ui.OnSurface
import io.github.vahan16.coinstellations.ui.PillBar
import io.github.vahan16.coinstellations.ui.SkyView
import io.github.vahan16.coinstellations.ui.SpaceBottom
import io.github.vahan16.coinstellations.ui.UpColor
import org.jetbrains.skia.EncodedImageFormat
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream
import kotlin.test.Test

/**
 * Headless generator for the README visuals — renders the real Compose UI off-screen
 * via [ImageComposeScene] (software Skia; no window). Output dir comes from the
 * SCREENSHOT_DIR env var (defaults to build/screenshots).
 *
 * Run:  SCREENSHOT_DIR=$PWD/docs ./gradlew :composeApp:desktopTest --tests "*ScreenshotGen*"
 */
class ScreenshotGen {

    private val outDir: File =
        File(System.getenv("SCREENSHOT_DIR")?.takeIf { it.isNotBlank() } ?: "build/screenshots")
            .also { it.mkdirs() }

    @Composable
    private fun Scene(timeframe: Timeframe, withHeader: Boolean, count: Int) {
        AppTheme {
            Box(Modifier.fillMaxSize().background(SpaceBottom)) {
                SkyView(DemoData.coins.take(count), timeframe, Modifier.fillMaxSize()) {}
                if (withHeader) {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(SpaceBottom.copy(alpha = 0.9f), Color.Transparent)))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Coinstellations", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Box(Modifier.clip(RoundedCornerShape(50)).background(Muted.copy(alpha = 0.18f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text("DEMO", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Calm, bright skies tonight · +1.42% (${timeframe.label})", color = Muted, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip("Cap \$2.41T"); Chip("BTC 54.2%"); Chip("24h +1.80%")
                        }
                        PillBar(Timeframe.entries, timeframe, { it.label }, {})
                    }
                }
            }
        }
    }

    @Composable
    private fun Chip(text: String) {
        Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.06f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
            Text(text, color = OnSurface.copy(alpha = 0.9f), fontSize = 12.sp)
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private fun frames(wPx: Int, hPx: Int, density: Float, count: Int, stepMs: Long, content: @Composable () -> Unit): List<ByteArray> {
        val scene = ImageComposeScene(width = wPx, height = hPx, density = Density(density), content = content)
        val result = ArrayList<ByteArray>(count)
        try {
            var t = 0L
            repeat(count) {
                val img = scene.render(t)
                result += img.encodeToData(EncodedImageFormat.PNG)!!.bytes
                t += stepMs * 1_000_000L
            }
        } finally {
            scene.close()
        }
        return result
    }

    @Test
    fun generate() {
        // Only run when explicitly asked (so a normal `desktopTest` stays side-effect free).
        if (System.getenv("SCREENSHOT_DIR").isNullOrBlank()) return

        // --- Static stills: render a short sequence, keep a settled frame ---
        val hero = frames(1600, 1000, 2f, 26, 50) { Scene(Timeframe.DAY, withHeader = true, count = 100) }
        File(outDir, "hero.png").writeBytes(hero.last())
        println("wrote ${outDir.resolve("hero.png")} (${hero.last().size / 1024} KB)")

        val mobile = frames(760, 1640, 2f, 26, 50) { Scene(Timeframe.WEEK, withHeader = true, count = 85) }
        File(outDir, "mobile.png").writeBytes(mobile.last())
        println("wrote ${outDir.resolve("mobile.png")} (${mobile.last().size / 1024} KB)")

        // --- Animated GIF of the living sky (twinkle + a shooting star) ---
        val gifFrames = frames(640, 380, 1.5f, 44, 60) { Scene(Timeframe.DAY, withHeader = false, count = 75) }
        writeGif(File(outDir, "demo.gif"), gifFrames, delayMs = 60)
        println("wrote ${outDir.resolve("demo.gif")} (${File(outDir, "demo.gif").length() / 1024} KB)")
    }

    private fun writeGif(file: File, pngFrames: List<ByteArray>, delayMs: Int) {
        val writer = ImageIO.getImageWritersByFormatName("gif").next()
        FileImageOutputStream(file).use { out ->
            writer.output = out
            val param = writer.defaultWriteParam
            writer.prepareWriteSequence(null)
            pngFrames.forEachIndexed { i, bytes ->
                val src = ImageIO.read(ByteArrayInputStream(bytes))
                val rgb = java.awt.image.BufferedImage(src.width, src.height, java.awt.image.BufferedImage.TYPE_INT_RGB)
                rgb.createGraphics().apply { drawImage(src, 0, 0, null); dispose() }
                val meta = writer.getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier.createFromRenderedImage(rgb), param)
                configureGifMetadata(meta, delayMs, loop = i == 0)
                writer.writeToSequence(javax.imageio.IIOImage(rgb, null, meta), param)
            }
            writer.endWriteSequence()
        }
        writer.dispose()
    }

    private fun configureGifMetadata(meta: javax.imageio.metadata.IIOMetadata, delayMs: Int, loop: Boolean) {
        val format = meta.nativeMetadataFormatName
        val root = meta.getAsTree(format) as org.w3c.dom.Node

        fun child(parent: org.w3c.dom.Node, name: String): org.w3c.dom.Element {
            var n = parent.firstChild
            while (n != null) {
                if (n.nodeName.equals(name, ignoreCase = true)) return n as org.w3c.dom.Element
                n = n.nextSibling
            }
            val e = (meta as javax.imageio.metadata.IIOMetadata).let { javax.imageio.metadata.IIOMetadataNode(name) }
            parent.appendChild(e)
            return e
        }

        val gce = child(root, "GraphicControlExtension")
        gce.setAttribute("disposalMethod", "none")
        gce.setAttribute("userInputFlag", "FALSE")
        gce.setAttribute("transparentColorFlag", "FALSE")
        gce.setAttribute("delayTime", (delayMs / 10).toString())
        gce.setAttribute("transparentColorIndex", "0")

        if (loop) {
            val appExts = child(root, "ApplicationExtensions")
            val appNode = javax.imageio.metadata.IIOMetadataNode("ApplicationExtension")
            appNode.setAttribute("applicationID", "NETSCAPE")
            appNode.setAttribute("authenticationCode", "2.0")
            appNode.userObject = byteArrayOf(0x1, 0x0, 0x0) // loop forever
            appExts.appendChild(appNode)
        }
        meta.setFromTree(format, root)
    }
}
