package com.bscharbau.currencymobile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * The zigzag "signal" waveform used as a section divider on the web app (see .signal-divider /
 * <svg class="signal-divider"> in frontend/src/styles.css and app.html) — a mostly-flat line with
 * periodic sharp spikes, evoking a frequency/heartbeat signal. Redrawn here with Canvas since
 * Compose has no direct SVG-polyline equivalent.
 */
@Composable
fun SignalDivider(modifier: Modifier = Modifier, blips: Int = 4) {
    Canvas(modifier = modifier.fillMaxWidth().height(28.dp)) {
        val midY = size.height / 2f
        val spikeUpY = size.height * 0.05f
        val spikeDownY = size.height

        val segmentWidth = size.width / blips
        // Within each segment: a flat run, then a small up/down/up zigzag "blip", matching the
        // rhythm of the web version's polyline points.
        val flatFraction = 0.55f
        val blipFraction = (1f - flatFraction) / 3f

        val path = Path().apply {
            moveTo(0f, midY)
            for (i in 0 until blips) {
                val segmentStart = i * segmentWidth
                val flatEnd = segmentStart + segmentWidth * flatFraction
                val upX = flatEnd + segmentWidth * blipFraction
                val downX = upX + segmentWidth * blipFraction
                val backX = downX + segmentWidth * blipFraction

                lineTo(flatEnd, midY)
                lineTo(upX, spikeUpY)
                lineTo(downX, spikeDownY)
                lineTo(backX, midY)
            }
            lineTo(size.width, midY)
        }

        drawPath(
            path = path,
            color = BrandColors.signal,
            alpha = 0.6f,
            style = Stroke(width = 1.4.dp.toPx()),
        )
    }
}
