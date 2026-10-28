package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Holding
import com.example.ui.theme.*

@Composable
fun HoldingsScreen(
    holdings: List<Holding>,
    totalInvested: Double,
    totalCurrentValue: Double,
    onBuyMore: (String) -> Unit,
    onSellHolding: (String) -> Unit,
    onGoToWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPnl = totalCurrentValue - totalInvested
    val totalPnlPct = if (totalInvested > 0.0) (totalPnl / totalInvested) * 100.0 else 0.0
    val isProfit = totalPnl >= 0
    val pnlColor = if (isProfit) BullGreen else BearRed

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("holdings_screen")
    ) {
        // Portfolio Summary Card
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
                    .padding(16.dp)
            ) {
                Text(
                    text = "डिलीवरी पोर्टफोलियो (Holdings)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${String.format("%,.2f", totalCurrentValue)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "वर्तमान मूल्य (Current Value)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (isProfit) "+" else ""}₹${String.format("%,.2f", totalPnl)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = pnlColor
                        )
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format("%.2f", totalPnlPct)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = pnlColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = DarkBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("कुल निवेश (Invested)", fontSize = 11.sp, color = TextMuted)
                        Text("₹${String.format("%,.2f", totalInvested)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("कुल स्टॉक्स (Total Assets)", fontSize = 11.sp, color = TextMuted)
                        Text("${holdings.size} होल्डिंग्स", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                    }
                }
            }
        }

        // Holdings List
        if (holdings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "कोई होल्डिंग्स नहीं हैं",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "डिलीवरी (CNC) में स्टॉक्स खरीदकर अपना पोर्टफोलियो बनाएं।",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onGoToWatchlist,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                    ) {
                        Text("स्टॉक्स देखें (Explore Stocks)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(holdings, key = { it.symbol }) { holding ->
                    HoldingCard(
                        holding = holding,
                        onBuyMore = { onBuyMore(holding.symbol) },
                        onSell = { onSellHolding(holding.symbol) }
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
fun HoldingCard(
    holding: Holding,
    onBuyMore: () -> Unit,
    onSell: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProfit = holding.pnl >= 0
    val pnlColor = if (isProfit) BullGreen else BearRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("holding_card_${holding.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Symbol & P&L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = holding.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${holding.quantity} शेयर • Avg ₹${String.format("%,.2f", holding.avgBuyPrice)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isProfit) "+" else ""}₹${String.format("%,.2f", holding.pnl)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                    Text(
                        text = "${if (isProfit) "+" else ""}${String.format("%.2f", holding.pnlPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pnlColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("निवेश (Invested)", fontSize = 10.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", holding.investedAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("वर्तमान मूल्य (LTP)", fontSize = 10.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", holding.currentPrice)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = pnlColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("वैल्यू (Current Value)", fontSize = 10.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", holding.currentValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBuyMore,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BullGreen),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("+ और खरीदें (Buy More)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSell,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BearRed, contentColor = Color.White),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Text("बेचें (SELL)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
