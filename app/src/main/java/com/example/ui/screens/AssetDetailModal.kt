package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketAsset
import com.example.data.model.OrderType
import com.example.ui.components.CandlestickChart
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun AssetDetailModal(
    asset: MarketAsset,
    onClose: () -> Unit,
    onBuyClicked: () -> Unit,
    onSellClicked: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val isBullish = asset.isBullish
    val changeColor = if (isBullish) BullGreen else BearRed

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            Surface(
                color = DarkSurface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = asset.symbol,
                                    fontSize = 18.sp,
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
                                        fontSize = 10.sp,
                                        color = AccentCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = asset.name,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (asset.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (asset.isFavorite) AccentGold else TextMuted
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onBuyClicked,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BullGreen, contentColor = Color.Black)
                    ) {
                        Text("BUY (खरीदें)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSellClicked,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_sell_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BearRed, contentColor = Color.White)
                    ) {
                        Text("SELL (बेचें)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Price Hero Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "₹${formatPrice(asset.currentPrice)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (isBullish) "+" else ""}₹${formatPrice(asset.change)} (${String.format("%+.2f", asset.changePercent)}%)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = changeColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("• आज", fontSize = 12.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1B2338)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(BullGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("REAL-TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Day Range slider indicator
                    val rangeSpan = max(asset.dayHigh - asset.dayLow, 0.01)
                    val progress = ((asset.currentPrice - asset.dayLow) / rangeSpan).coerceIn(0.0, 1.0).toFloat()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Low: ₹${formatPrice(asset.dayLow)}", fontSize = 11.sp, color = BearRed)
                            Text("दिन का रेंज (Day Range)", fontSize = 11.sp, color = TextMuted)
                            Text("High: ₹${formatPrice(asset.dayHigh)}", fontSize = 11.sp, color = BullGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = AccentCyan,
                            trackColor = DarkSurfaceVariant
                        )
                    }
                }
            }

            // Real-time Candlestick Chart
            CandlestickChart(
                candles = asset.history,
                currentPrice = asset.currentPrice
            )

            // Market Statistics Grid
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "मार्केट आंकड़े (Market Overview)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(label = "ओपन (Open)", value = "₹${formatPrice(asset.openPrice)}")
                        StatItem(label = "पिछला क्लोज (Prev Close)", value = "₹${formatPrice(asset.previousClose)}")
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(label = "दिन का हाई (High)", value = "₹${formatPrice(asset.dayHigh)}", valueColor = BullGreen)
                        StatItem(label = "दिन का लो (Low)", value = "₹${formatPrice(asset.dayLow)}", valueColor = BearRed)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(label = "वॉल्यूम (Volume)", value = formatVol(asset.volume))
                        StatItem(label = "52-हफ्ते हाई/लो", value = "₹${formatPrice(asset.dayHigh * 1.15)} / ₹${formatPrice(asset.dayLow * 0.85)}")
                    }
                }
            }

            // Market Depth (Order Book)
            asset.depth?.let { depth ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "मार्केट डेप्थ / आर्डर बुक (Market Depth)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Bids column (Green)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BIDS (खरीदार)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                                Spacer(modifier = Modifier.height(4.dp))
                                depth.bids.forEach { bid ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(bid.quantity.toString(), fontSize = 11.sp, color = TextSecondary)
                                        Text("₹${formatPrice(bid.price)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BullGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Asks column (Red)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ASKS (विक्रेता)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BearRed)
                                Spacer(modifier = Modifier.height(4.dp))
                                depth.asks.forEach { ask ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("₹${formatPrice(ask.price)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BearRed)
                                        Text(ask.quantity.toString(), fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1000) String.format("%,.2f", price) else String.format("%.2f", price)
}

private fun formatVol(vol: Long): String {
    return if (vol >= 1_000_000) String.format("%.2fM", vol / 1_000_000.0)
    else if (vol >= 1_000) String.format("%.1fK", vol / 1_000.0)
    else vol.toString()
}
