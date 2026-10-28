package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderType
import com.example.data.model.Position
import com.example.ui.theme.*

@Composable
fun PositionsScreen(
    positions: List<Position>,
    totalUnrealizedPnl: Double,
    onSquareOffPosition: (String) -> Unit,
    onSquareOffAll: () -> Unit,
    onGoToWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("positions_screen")
    ) {
        // Summary Banner
        val isTotalProfit = totalUnrealizedPnl >= 0
        val totalColor = if (isTotalProfit) BullGreen else BearRed

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (isTotalProfit) Color(0xFF00381B) else Color(0xFF380A14),
                                Color(0xFF101624)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "कुल ओपन P&L (Unrealized)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isTotalProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = totalColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isTotalProfit) "+" else ""}₹${String.format("%,.2f", totalUnrealizedPnl)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = totalColor
                            )
                        }
                    }

                    if (positions.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onSquareOffAll,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BearRed),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(BearRed, AccentGold))),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("square_off_all_button")
                        ) {
                            Text("सभी काटें (Exit All)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "सक्रिय पोजीशन: ${positions.size} • रियल-टाइम में अपडेट हो रहा है",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Positions List
        if (positions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "कोई खुली पोजीशन नहीं है",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "वॉचलिस्ट से कोई भी स्टॉक या इंडेक्स ट्रेड करें।",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onGoToWatchlist,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                    ) {
                        Text("वॉचलिस्ट देखें (Go to Watchlist)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(positions, key = { it.id }) { pos ->
                    PositionCard(
                        position = pos,
                        onSquareOff = { onSquareOffPosition(pos.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun PositionCard(
    position: Position,
    onSquareOff: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProfit = position.unrealizedPnl >= 0
    val pnlColor = if (isProfit) BullGreen else BearRed
    val isBuy = position.orderType == OrderType.BUY

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("position_card_${position.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Symbol, Buy/Sell Chip, Product Type, P&L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isBuy) BullGreen.copy(alpha = 0.2f) else BearRed.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isBuy) "BUY" else "SELL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBuy) BullGreen else BearRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = position.symbol,
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
                            text = position.productType.displayName,
                            fontSize = 9.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // P&L badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isProfit) "+" else ""}₹${String.format("%,.2f", position.unrealizedPnl)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                    Text(
                        text = "${if (isProfit) "+" else ""}${String.format("%.2f", position.unrealizedPnlPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pnlColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details: Qty, Entry Price, Live Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("मात्रा (Qty)", fontSize = 10.sp, color = TextMuted)
                    Text("${position.quantity} शेयर", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("खरीद मूल्य (Entry)", fontSize = 10.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", position.entryPrice)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("लाइव मूल्य (LTP)", fontSize = 10.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", position.currentPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = pnlColor)
                }
            }

            // Stop loss / Target info if present
            if (position.stopLoss != null || position.targetPrice != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    position.stopLoss?.let { sl ->
                        Text("SL: ₹${String.format("%,.2f", sl)}", fontSize = 10.sp, color = BearRed)
                    }
                    position.targetPrice?.let { tp ->
                        Text("Target: ₹${String.format("%,.2f", tp)}", fontSize = 10.sp, color = BullGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Square Off Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onSquareOff,
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("square_off_${position.symbol}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("स्क्वेयर ऑफ (Exit)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
