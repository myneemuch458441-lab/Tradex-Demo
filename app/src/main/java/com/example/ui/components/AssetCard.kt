package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleData
import com.example.data.model.MarketAsset
import com.example.data.model.PriceFlash
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun AssetCard(
    asset: MarketAsset,
    onClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic price flash highlight
    val flashColor by animateColorAsState(
        targetValue = when (asset.priceFlash) {
            PriceFlash.UP -> BullGreen.copy(alpha = 0.35f)
            PriceFlash.DOWN -> BearRed.copy(alpha = 0.35f)
            PriceFlash.NONE -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 350),
        label = "flash"
    )

    val isBullish = asset.isBullish
    val changeColor = if (isBullish) BullGreen else BearRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("asset_card_${asset.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(flashColor)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Main Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Symbol & Name
                Row(
                    modifier = Modifier.weight(1.3f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (asset.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (asset.isFavorite) AccentGold else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.symbol,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = asset.category.displayName,
                                    fontSize = 9.sp,
                                    color = AccentCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = asset.name,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Middle: Sparkline preview
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(34.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MiniSparkline(candles = asset.history, isBullish = isBullish)
                }

                // Right: Price & Change
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${formatPrice(asset.currentPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isBullish) BullGreen.copy(alpha = 0.12f) else BearRed.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${if (isBullish) "+" else ""}₹${formatPrice(asset.change)} (${String.format("%+.2f", asset.changePercent)}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = changeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Buy/Sell buttons on card
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "High: ₹${formatPrice(asset.dayHigh)} • Low: ₹${formatPrice(asset.dayLow)}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onBuyClick,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("buy_${asset.symbol}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BullGreen, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("BUY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSellClick,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("sell_${asset.symbol}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BearRed, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("SELL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MiniSparkline(
    candles: List<CandleData>,
    isBullish: Boolean,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val strokeColor = if (isBullish) BullGreen else BearRed
    val min = candles.minOfOrNull { it.close } ?: 0.0
    val max = candles.maxOfOrNull { it.close } ?: 1.0
    val range = max(max - min, 0.01)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val step = width / (candles.size - 1).coerceAtLeast(1)

        val path = Path()
        candles.forEachIndexed { i, candle ->
            val x = i * step
            val y = (height * (1f - ((candle.close - min) / range).toFloat())).coerceIn(2f, height - 2f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1000) String.format("%,.2f", price) else String.format("%.2f", price)
}
