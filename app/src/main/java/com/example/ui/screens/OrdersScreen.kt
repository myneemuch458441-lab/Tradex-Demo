package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.TradeOrder
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class OrderFilter(val label: String) {
    ALL("सभी (All)"),
    EXECUTED("निष्पादित (Executed)"),
    PENDING("पेंडिंग लिमिट (Pending)"),
    CLOSED("क्लोज्ड P&L (Closed)")
}

@Composable
fun OrdersScreen(
    orders: List<TradeOrder>,
    onCancelOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(OrderFilter.ALL) }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            OrderFilter.ALL -> orders
            OrderFilter.EXECUTED -> orders.filter { it.status == OrderStatus.EXECUTED }
            OrderFilter.PENDING -> orders.filter { it.status == OrderStatus.PENDING }
            OrderFilter.CLOSED -> orders.filter { it.status == OrderStatus.CLOSED }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_screen")
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AccentCyan,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AccentCyan else DarkBorder
                    )
                )
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "कोई आर्डर नहीं मिला",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "आपके द्वारा किए गए सभी ट्रेड्स और लिमिट ऑर्डर्स यहाँ दिखेंगे।",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onCancel = { onCancelOrder(order.id) }
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
fun OrderHistoryCard(
    order: TradeOrder,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBuy = order.orderType == OrderType.BUY
    val typeColor = if (isBuy) BullGreen else BearRed
    val timeFormatted = SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()).format(Date(order.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Symbol, BUY/SELL, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = order.orderType.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.symbol,
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
                            text = order.productType.name,
                            fontSize = 9.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Status Badge
                val (statusBg, statusFg) = when (order.status) {
                    OrderStatus.EXECUTED -> Pair(BullGreen.copy(alpha = 0.15f), BullGreen)
                    OrderStatus.PENDING -> Pair(AccentGold.copy(alpha = 0.15f), AccentGold)
                    OrderStatus.CLOSED -> Pair(AccentPurple.copy(alpha = 0.15f), AccentPurple)
                    OrderStatus.CANCELLED -> Pair(DarkBorder, TextMuted)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = order.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("मात्रा (Qty)", fontSize = 10.sp, color = TextMuted)
                    Text("${order.quantity} Qty", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (order.status == OrderStatus.PENDING) "लिमिट टारगेट" else "मूल्य (Price)",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    val priceDisplay = if (order.status == OrderStatus.PENDING && order.targetLimitPrice != null) {
                        order.targetLimitPrice
                    } else {
                        order.executedPrice
                    }
                    Text("₹${String.format("%,.2f", priceDisplay)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("समय (Time)", fontSize = 10.sp, color = TextMuted)
                    Text(timeFormatted, fontSize = 11.sp, color = TextSecondary)
                }
            }

            // If Closed with Realized P&L
            if (order.status == OrderStatus.CLOSED && order.realizedPnl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isPnlProfit = order.realizedPnl >= 0
                val pnlColor = if (isPnlProfit) BullGreen else BearRed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(pnlColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("वास्तविक लाभ/हानि (Realized P&L):", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${if (isPnlProfit) "+" else ""}₹${String.format("%,.2f", order.realizedPnl)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }

            // Strategy / Journal Notes
            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 ट्रेड नोट: ${order.notes}",
                    fontSize = 11.sp,
                    color = AccentCyan
                )
            }

            // Cancel button for pending orders
            if (order.status == OrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BearRed),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("रद्द करें (Cancel Order)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
