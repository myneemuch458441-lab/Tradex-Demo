package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserWallet
import com.example.ui.theme.*

@Composable
fun TradingHeader(
    wallet: UserWallet,
    portfolioValue: Double,
    unrealizedPnl: Double,
    onResetClicked: () -> Unit,
    onAddFundsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val totalPnl = wallet.realizedPnl + unrealizedPnl
    val isProfit = totalPnl >= 0
    val pnlColor = if (isProfit) BullGreen else BearRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trading_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161F33),
                            Color(0xFF0F1524)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Top Row: App Brand & Live Simulator badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BullGreen, AccentCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TX",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "TradeX Demo",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "वर्चुअल ट्रेडिंग • असीमित अभ्यास",
                            fontSize = 11.sp,
                            color = AccentCyan
                        )
                    }
                }

                // Live pulse pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1B263B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BullGreen.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE TICK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BullGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Middle: Virtual Net Worth & P&L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "वर्चुअल पोर्टफोलियो मूल्य (Virtual Net Worth)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${formatRupees(portfolioValue)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                // Total P&L badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isProfit) BullGreen.copy(alpha = 0.15f) else BearRed.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = pnlColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isProfit) "+" else ""}₹${formatRupees(totalPnl)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = pnlColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub Stats: Available Cash & Realized P&L
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101A), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("उपलब्ध कैश (Cash)", fontSize = 10.sp, color = TextMuted)
                    Text("₹${formatRupees(wallet.availableCash)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("दैनिक P&L (Daily)", fontSize = 10.sp, color = TextMuted)
                    val dPnl = wallet.dailyPnl
                    val dColor = if (dPnl >= 0) BullGreen else BearRed
                    Text("${if (dPnl >= 0) "+" else ""}₹${formatRupees(dPnl)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("जीत दर (Win Rate)", fontSize = 10.sp, color = TextMuted)
                    Text("${String.format("%.1f", wallet.winRate)}% (${wallet.winningTrades}W/${wallet.losingTrades}L)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions: Unlimited Reset & Add Funds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onResetClicked,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_balance_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(AccentCyan.copy(alpha = 0.5f), AccentPurple.copy(alpha = 0.5f)))
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Capital",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "अनलिमिटेड रीसेट (₹10L)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onAddFundsClicked,
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("add_funds_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BullGreen,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Funds",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ फंड्स",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatRupees(amount: Double): String {
    return String.format("%,.2f", amount)
}
