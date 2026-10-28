package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
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
import com.example.data.model.UserWallet
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    wallet: UserWallet,
    portfolioValue: Double,
    onResetClicked: () -> Unit,
    onAddFundsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Performance Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF162138),
                                Color(0xFF0F1524)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Text(
                    text = "ट्रेडिंग परफॉर्मेंस व मेट्रिक्स (Trading Stats)",
                    fontSize = 13.sp,
                    color = AccentCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${String.format("%.1f", wallet.winRate)}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (wallet.winRate >= 50.0) BullGreen else AccentGold
                        )
                        Text(
                            text = "कुल जीत दर (Win Rate)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${wallet.totalTrades} ट्रेड्स",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${wallet.winningTrades} जीत • ${wallet.losingTrades} हार",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Win rate progress bar
                val winRatio = if (wallet.totalTrades > 0) (wallet.winningTrades.toFloat() / wallet.totalTrades) else 0f
                LinearProgressIndicator(
                    progress = { winRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BullGreen,
                    trackColor = BearRed.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C101A), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("कुल रियल-P&L", fontSize = 10.sp, color = TextMuted)
                        val pColor = if (wallet.realizedPnl >= 0) BullGreen else BearRed
                        Text("${if (wallet.realizedPnl >= 0) "+" else ""}₹${String.format("%,.2f", wallet.realizedPnl)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = pColor)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("दैनिक P&L", fontSize = 10.sp, color = TextMuted)
                        val dpColor = if (wallet.dailyPnl >= 0) BullGreen else BearRed
                        Text("${if (wallet.dailyPnl >= 0) "+" else ""}₹${String.format("%,.2f", wallet.dailyPnl)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dpColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("रीसेट संख्या", fontSize = 10.sp, color = TextMuted)
                        Text("${wallet.resetCount} बार", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                }
            }
        }

        // Unlimited Virtual Trading Capital Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "असीमित वर्चुअल फंड्स (Unlimited Virtual Capital)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "यह एक 100% रिस्क-फ्री डेमो ट्रेडिंग सिम्युलेटर है। आप रोज जितनी चाहें उतनी बार ट्रेडिंग कर सकते हैं और कभी भी अपना बैलेंस दोबारा रीसेट कर सकते हैं।",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onResetClicked,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = AccentCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analytics_reset_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("फंड्स रीसेट (₹10L)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAddFundsClicked,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BullGreen, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analytics_add_funds_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ कैश जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Trading Wisdom & Rules Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "सफल ट्रेडिंग के 4 सुनहरे नियम (Golden Rules)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TradingRuleItem(
                    number = "1",
                    title = "1% रिस्क मैनेजमेंट रूल",
                    description = "किसी भी एक ट्रेड में अपने कुल कैपिटल का 1% से 2% से अधिक रिस्क कभी न लें।"
                )
                TradingRuleItem(
                    number = "2",
                    title = "स्टॉप-लॉस (Stop Loss) हमेशा लगाएं",
                    description = "ट्रेड लेते ही एग्जिट प्लान तय करें। मार्केट के खिलाफ कभी अड़ियल न बनें।"
                )
                TradingRuleItem(
                    number = "3",
                    title = "रिवेंज ट्रेडिंग से बचें (No Revenge Trading)",
                    description = "घाटा होने पर तुरंत बदला लेने के लिए गलत ट्रेड न लें। थोड़ा ब्रेक लें।"
                )
                TradingRuleItem(
                    number = "4",
                    title = "डेमो में कम से कम 30 दिन अभ्यास करें",
                    description = "अपनी स्ट्रेटेजी को इस ऐप में रोज टेस्ट करें जब तक लगातार प्रॉफिट न मिलने लगे।"
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun TradingRuleItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AccentCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}
