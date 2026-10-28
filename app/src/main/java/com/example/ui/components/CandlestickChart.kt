package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleData
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

enum class ChartType {
    CANDLESTICK, LINE
}

@Composable
fun CandlestickChart(
    candles: List<CandleData>,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("चार्ट लोड हो रहा है...", color = TextMuted)
        }
        return
    }

    var chartType by remember { mutableStateOf(ChartType.CANDLESTICK) }
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    val maxPrice = max(candles.maxOf { it.high }, currentPrice) * 1.002
    val minPrice = min(candles.minOf { it.low }, currentPrice) * 0.998
    val priceRange = max(maxPrice - minPrice, 0.01)

    val maxVolume = candles.maxOfOrNull { it.volume } ?: 1000L

    val activeCandle = selectedCandleIndex?.let { idx ->
        if (idx in candles.indices) candles[idx] else null
    } ?: candles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1420), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Chart Controls & Selected Candle Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live / Scrubber OHLC info
            if (activeCandle != null) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "O: ₹${formatP(activeCandle.open)}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "H: ₹${formatP(activeCandle.high)}",
                            fontSize = 11.sp,
                            color = BullGreen
                        )
                        Text(
                            text = "L: ₹${formatP(activeCandle.low)}",
                            fontSize = 11.sp,
                            color = BearRed
                        )
                        Text(
                            text = "C: ₹${formatP(activeCandle.close)}",
                            fontSize = 11.sp,
                            color = if (activeCandle.close >= activeCandle.open) BullGreen else BearRed
                        )
                    }
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(activeCandle.timestamp))
                    Text(
                        text = "Time: $timeStr • Vol: ${formatVol(activeCandle.volume)}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Chart Type Toggle
            Row(
                modifier = Modifier
                    .background(Color(0xFF1B2338), RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = { chartType = ChartType.CANDLESTICK },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Candlestick Chart",
                        tint = if (chartType == ChartType.CANDLESTICK) BullGreen else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { chartType = ChartType.LINE },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Line Chart",
                        tint = if (chartType == ChartType.LINE) BullGreen else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Canvas Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(candles.size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val slotWidth = size.width / candles.size
                            val idx = (offset.x / slotWidth).toInt().coerceIn(0, candles.lastIndex)
                            selectedCandleIndex = idx
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val slotWidth = size.width / candles.size
                            val idx = (change.position.x / slotWidth).toInt().coerceIn(0, candles.lastIndex)
                            selectedCandleIndex = idx
                        },
                        onDragEnd = {
                            selectedCandleIndex = null
                        },
                        onDragCancel = {
                            selectedCandleIndex = null
                        }
                    )
                }
                .pointerInput(candles.size) {
                    detectTapGestures(
                        onPress = { offset ->
                            val slotWidth = size.width / candles.size
                            val idx = (offset.x / slotWidth).toInt().coerceIn(0, candles.lastIndex)
                            selectedCandleIndex = idx
                            tryAwaitRelease()
                            selectedCandleIndex = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val chartAreaHeight = height * 0.78f
            val volumeAreaHeight = height * 0.20f
            val volumeAreaTop = height * 0.80f

            // Price grid lines
            val gridSteps = 3
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            for (i in 0..gridSteps) {
                val ratio = i.toFloat() / gridSteps
                val y = chartAreaHeight * ratio
                val priceAtY = maxPrice - (ratio * priceRange)

                drawLine(
                    color = DarkBorder,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )

                // Price label
                drawText(
                    textMeasurer = textMeasurer,
                    text = "₹${formatP(priceAtY)}",
                    topLeft = Offset(width - 70.dp.toPx(), y - 14.sp.toPx()),
                    style = TextStyle(color = TextMuted, fontSize = 9.sp)
                )
            }

            val count = candles.size
            val slotWidth = width / count
            val candleWidth = max(slotWidth * 0.65f, 2f)

            if (chartType == ChartType.CANDLESTICK) {
                candles.forEachIndexed { index, candle ->
                    val x = (index * slotWidth) + (slotWidth / 2)
                    val isGreen = candle.close >= candle.open
                    val candleColor = if (isGreen) BullGreen else BearRed

                    // High / Low Wick
                    val highY = (chartAreaHeight * (1f - ((candle.high - minPrice) / priceRange).toFloat()))
                    val lowY = (chartAreaHeight * (1f - ((candle.low - minPrice) / priceRange).toFloat()))
                    drawLine(
                        color = candleColor,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.5f
                    )

                    // Open / Close Body
                    val openY = (chartAreaHeight * (1f - ((candle.open - minPrice) / priceRange).toFloat()))
                    val closeY = (chartAreaHeight * (1f - ((candle.close - minPrice) / priceRange).toFloat()))
                    val bodyTop = min(openY, closeY)
                    val bodyHeight = max(kotlin.math.abs(closeY - openY), 2f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - (candleWidth / 2), bodyTop),
                        size = Size(candleWidth, bodyHeight)
                    )

                    // Volume bar at bottom
                    val volRatio = if (maxVolume > 0) (candle.volume.toFloat() / maxVolume) else 0f
                    val volBarHeight = volumeAreaHeight * volRatio
                    drawRect(
                        color = candleColor.copy(alpha = 0.35f),
                        topLeft = Offset(x - (candleWidth / 2), height - volBarHeight),
                        size = Size(candleWidth, volBarHeight)
                    )
                }
            } else {
                // Line chart mode
                val path = Path()
                candles.forEachIndexed { index, candle ->
                    val x = (index * slotWidth) + (slotWidth / 2)
                    val y = (chartAreaHeight * (1f - ((candle.close - minPrice) / priceRange).toFloat()))
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = BullGreen,
                    style = Stroke(width = 3f)
                )

                // Fill area gradient
                val fillPath = Path().apply {
                    addPath(path)
                    val lastX = ((candles.size - 1) * slotWidth) + (slotWidth / 2)
                    lineTo(lastX, chartAreaHeight)
                    lineTo(slotWidth / 2, chartAreaHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(BullGreen.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = chartAreaHeight
                    )
                )
            }

            // Crosshair if user touches
            if (selectedCandleIndex != null && selectedCandleIndex in candles.indices) {
                val activeX = (selectedCandleIndex!! * slotWidth) + (slotWidth / 2)
                val c = candles[selectedCandleIndex!!]
                val activeY = (chartAreaHeight * (1f - ((c.close - minPrice) / priceRange).toFloat()))

                // Vertical cursor line
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(activeX, 0f),
                    end = Offset(activeX, height),
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect
                )

                // Horizontal cursor line
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(0f, activeY),
                    end = Offset(width, activeY),
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect
                )

                // Glowing dot
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(activeX, activeY)
                )
            }
        }
    }
}

private fun formatP(value: Double): String {
    return if (value >= 1000) String.format("%,.1f", value) else String.format("%.2f", value)
}

private fun formatVol(vol: Long): String {
    return if (vol >= 1_000_000) String.format("%.1fM", vol / 1_000_000.0)
    else if (vol >= 1_000) String.format("%.1fK", vol / 1_000.0)
    else vol.toString()
}
