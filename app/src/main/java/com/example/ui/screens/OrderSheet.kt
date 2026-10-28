package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSheet(
    asset: MarketAsset,
    initialOrderType: OrderType,
    availableCash: Double,
    onClose: () -> Unit,
    onSubmitOrder: (
        orderType: OrderType,
        productType: ProductType,
        executionType: OrderExecutionType,
        limitPrice: Double?,
        quantity: Int,
        stopLoss: Double?,
        targetPrice: Double?,
        notes: String
    ) -> Unit
) {
    var orderType by remember { mutableStateOf(initialOrderType) }
    var productType by remember { mutableStateOf(ProductType.INTRADAY) }
    var executionType by remember { mutableStateOf(OrderExecutionType.MARKET) }

    var quantityStr by remember { mutableStateOf("10") }
    var limitPriceStr by remember { mutableStateOf(String.format("%.2f", asset.currentPrice)) }
    var showAdvanced by remember { mutableStateOf(false) }
    var stopLossStr by remember { mutableStateOf("") }
    var targetPriceStr by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }

    val qty = quantityStr.toIntOrNull() ?: 1
    val limitPrice = limitPriceStr.toDoubleOrNull() ?: asset.currentPrice
    val effectivePrice = if (executionType == OrderExecutionType.LIMIT) limitPrice else asset.currentPrice

    val totalValue = effectivePrice * qty
    val requiredMargin = totalValue / productType.leverage
    val isMarginSufficient = availableCash >= requiredMargin

    val actionColor = if (orderType == OrderType.BUY) BullGreen else BearRed

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${asset.symbol} • ₹${String.format("%,.2f", asset.currentPrice)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "वर्चुअल आर्डर निष्पादन (Virtual Paper Trade)",
                        fontSize = 11.sp,
                        color = AccentCyan
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            // BUY / SELL Segmented Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101A), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { orderType = OrderType.BUY },
                    modifier = Modifier.weight(1f).testTag("select_buy_tab"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (orderType == OrderType.BUY) BullGreen else Color.Transparent,
                        contentColor = if (orderType == OrderType.BUY) Color.Black else TextSecondary
                    ),
                    elevation = null
                ) {
                    Text("BUY (खरीदें)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { orderType = OrderType.SELL },
                    modifier = Modifier.weight(1f).testTag("select_sell_tab"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (orderType == OrderType.SELL) BearRed else Color.Transparent,
                        contentColor = if (orderType == OrderType.SELL) Color.White else TextSecondary
                    ),
                    elevation = null
                ) {
                    Text("SELL (बेचें/शॉर्ट)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Product Type (Intraday vs Delivery)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProductTypeCard(
                    title = "Intraday (MIS)",
                    subtitle = "डे ट्रेडिंग • 5x मार्जिन",
                    isSelected = productType == ProductType.INTRADAY,
                    onClick = { productType = ProductType.INTRADAY },
                    modifier = Modifier.weight(1f)
                )
                ProductTypeCard(
                    title = "Delivery (CNC)",
                    subtitle = "लॉन्ग टर्म • 1x मार्जिन",
                    isSelected = productType == ProductType.DELIVERY,
                    onClick = { productType = ProductType.DELIVERY },
                    modifier = Modifier.weight(1f)
                )
            }

            // Order Type (Market vs Limit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OrderTypePill(
                    label = "Market (मार्केट)",
                    isSelected = executionType == OrderExecutionType.MARKET,
                    onClick = { executionType = OrderExecutionType.MARKET },
                    modifier = Modifier.weight(1f)
                )
                OrderTypePill(
                    label = "Limit (लिमिट)",
                    isSelected = executionType == OrderExecutionType.LIMIT,
                    onClick = { executionType = OrderExecutionType.LIMIT },
                    modifier = Modifier.weight(1f)
                )
            }

            // If Limit Order -> Limit Price Input
            if (executionType == OrderExecutionType.LIMIT) {
                OutlinedTextField(
                    value = limitPriceStr,
                    onValueChange = { limitPriceStr = it },
                    label = { Text("लिमिट मूल्य (Limit Price ₹)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Quantity selector with quick chips
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("मात्रा (Quantity / Shares)", fontSize = 12.sp, color = TextSecondary)
                    Text("कुल मूल्य: ₹${String.format("%,.2f", totalValue)}", fontSize = 12.sp, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val newQty = max(1, qty - 5)
                            quantityStr = newQty.toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("-5", fontSize = 12.sp, color = TextPrimary)
                    }

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.weight(1f).testTag("quantity_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Button(
                        onClick = {
                            val newQty = qty + 5
                            quantityStr = newQty.toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("+5", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                // Quick Quantity presets
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 25, 50, 100).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { quantityStr = preset.toString() }
                        ) {
                            Text(
                                text = "+$preset",
                                fontSize = 11.sp,
                                color = AccentCyan,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Max Affordable
                    val maxAffordable = max(1, ((availableCash * productType.leverage) / effectivePrice).toInt())
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceVariant,
                        modifier = Modifier
                            .weight(1.2f)
                            .clickable { quantityStr = maxAffordable.toString() }
                    ) {
                        Text(
                            text = "MAX ($maxAffordable)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            modifier = Modifier.padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Stop Loss & Target Accordion
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvanced = !showAdvanced }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("स्टॉप लॉस & टारगेट (Stop Loss / TP) [वैकल्पिक]", fontSize = 12.sp, color = AccentCyan)
                    Text(if (showAdvanced) "▲ छुपाएं" else "▼ जोड़ें", fontSize = 11.sp, color = TextMuted)
                }

                if (showAdvanced) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stopLossStr,
                            onValueChange = { stopLossStr = it },
                            label = { Text("Stop Loss (₹)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground,
                                focusedBorderColor = BearRed,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = targetPriceStr,
                            onValueChange = { targetPriceStr = it },
                            label = { Text("Target (₹)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground,
                                focusedBorderColor = BullGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }

            // Notes / Journal
            OutlinedTextField(
                value = notesStr,
                onValueChange = { notesStr = it },
                label = { Text("ट्रेड जर्नल नोट्स (Trade Reason / Strategy)", fontSize = 11.sp) },
                placeholder = { Text("e.g. Breakout pullback, 20 EMA bounce...", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Margin & Funds Info
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0C101A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("आवश्यक मार्जिन (Margin Req):", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "₹${String.format("%,.2f", requiredMargin)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMarginSufficient) TextPrimary else BearRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("उपलब्ध वर्चुअल कैश (Available):", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "₹${String.format("%,.2f", availableCash)}",
                            fontSize = 12.sp,
                            color = BullGreen
                        )
                    }
                    if (!isMarginSufficient) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠ फंड्स कम हैं! कृपया मात्रा घटाएं या फंड्स रीसेट करें।",
                            fontSize = 11.sp,
                            color = BearRed
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val sl = stopLossStr.toDoubleOrNull()
                    val tp = targetPriceStr.toDoubleOrNull()
                    val lim = if (executionType == OrderExecutionType.LIMIT) limitPrice else null
                    onSubmitOrder(
                        orderType,
                        productType,
                        executionType,
                        lim,
                        qty,
                        sl,
                        tp,
                        notesStr
                    )
                },
                enabled = isMarginSufficient && qty > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_order_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionColor,
                    contentColor = if (orderType == OrderType.BUY) Color.Black else Color.White
                )
            ) {
                Text(
                    text = "${if (orderType == OrderType.BUY) "खरीदें (BUY)" else "बेचें (SELL)"} • $qty ${asset.symbol}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ProductTypeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isSelected) AccentCyan else DarkBorder,
                shape = RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) AccentCyan.copy(alpha = 0.12f) else DarkBackground
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AccentCyan else TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun OrderTypePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isSelected) AccentCyan else DarkBorder,
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) AccentCyan.copy(alpha = 0.12f) else DarkBackground
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) AccentCyan else TextSecondary,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
