package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MainTab
import com.example.data.model.OrderType
import com.example.ui.TradingViewModel
import com.example.ui.components.TradingHeader
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                TradingApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TradingApp(viewModel: TradingViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()

    val assets by viewModel.filteredAssets.collectAsStateWithLifecycle()
    val allAssets by viewModel.assets.collectAsStateWithLifecycle()
    val positions by viewModel.positions.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()

    val totalUnrealizedPnl by viewModel.totalUnrealizedPnl.collectAsStateWithLifecycle()
    val totalHoldingsInvested by viewModel.totalHoldingsInvested.collectAsStateWithLifecycle()
    val totalHoldingsCurrentValue by viewModel.totalHoldingsCurrentValue.collectAsStateWithLifecycle()
    val portfolioValue by viewModel.totalPortfolioValue.collectAsStateWithLifecycle()

    val detailAsset by viewModel.selectedAssetDetail.collectAsStateWithLifecycle()
    val orderSheetAsset by viewModel.orderSheetAsset.collectAsStateWithLifecycle()
    val orderSheetType by viewModel.orderSheetType.collectAsStateWithLifecycle()
    val showResetDialog by viewModel.showResetDialog.collectAsStateWithLifecycle()
    val showAddFundsDialog by viewModel.showAddFundsDialog.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(data.visuals.message, fontSize = 13.sp)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val (icon, badgeCount) = when (tab) {
                        MainTab.WATCHLIST -> Pair(Icons.Default.ShowChart, 0)
                        MainTab.POSITIONS -> Pair(Icons.Default.Layers, positions.size)
                        MainTab.HOLDINGS -> Pair(Icons.Default.AccountBalance, holdings.size)
                        MainTab.ORDERS -> Pair(Icons.Default.ReceiptLong, orders.count { it.status.name == "PENDING" })
                        MainTab.ANALYTICS -> Pair(Icons.Default.Analytics, 0)
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (badgeCount > 0) {
                                        Badge(
                                            containerColor = if (tab == MainTab.POSITIONS) BullGreen else AccentGold,
                                            contentColor = Color.Black
                                        ) {
                                            Text(badgeCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) AccentCyan else TextMuted
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.hindiTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentCyan else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AccentCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Persistent Trading Header with Portfolio Net Worth, Live Ticks & Unlimited Reset
            TradingHeader(
                wallet = wallet,
                portfolioValue = portfolioValue,
                unrealizedPnl = totalUnrealizedPnl,
                onResetClicked = { viewModel.showResetDialog.value = true },
                onAddFundsClicked = { viewModel.showAddFundsDialog.value = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Main Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    MainTab.WATCHLIST -> {
                        WatchlistScreen(
                            assets = assets,
                            allAssets = allAssets,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.setCategory(it) },
                            searchQuery = searchQuery,
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            showFavoritesOnly = showFavoritesOnly,
                            onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                            onAssetClick = { viewModel.openAssetDetail(it) },
                            onBuyClick = { viewModel.openOrderSheet(it, OrderType.BUY) },
                            onSellClick = { viewModel.openOrderSheet(it, OrderType.SELL) },
                            onFavoriteToggle = { viewModel.toggleAssetFavorite(it) }
                        )
                    }

                    MainTab.POSITIONS -> {
                        PositionsScreen(
                            positions = positions,
                            totalUnrealizedPnl = totalUnrealizedPnl,
                            onSquareOffPosition = { viewModel.squareOffPosition(it) },
                            onSquareOffAll = { viewModel.squareOffAllPositions() },
                            onGoToWatchlist = { viewModel.selectTab(MainTab.WATCHLIST) }
                        )
                    }

                    MainTab.HOLDINGS -> {
                        HoldingsScreen(
                            holdings = holdings,
                            totalInvested = totalHoldingsInvested,
                            totalCurrentValue = totalHoldingsCurrentValue,
                            onBuyMore = { symbol ->
                                val a = allAssets.find { it.symbol == symbol }
                                if (a != null) viewModel.openOrderSheet(a, OrderType.BUY)
                            },
                            onSellHolding = { symbol ->
                                val a = allAssets.find { it.symbol == symbol }
                                if (a != null) viewModel.openOrderSheet(a, OrderType.SELL)
                            },
                            onGoToWatchlist = { viewModel.selectTab(MainTab.WATCHLIST) }
                        )
                    }

                    MainTab.ORDERS -> {
                        OrdersScreen(
                            orders = orders,
                            onCancelOrder = { viewModel.cancelOrder(it) }
                        )
                    }

                    MainTab.ANALYTICS -> {
                        AnalyticsScreen(
                            wallet = wallet,
                            portfolioValue = portfolioValue,
                            onResetClicked = { viewModel.showResetDialog.value = true },
                            onAddFundsClicked = { viewModel.showAddFundsDialog.value = true }
                        )
                    }
                }
            }
        }
    }

    // Asset Detail Modal
    detailAsset?.let { asset ->
        AssetDetailModal(
            asset = asset,
            onClose = { viewModel.closeAssetDetail() },
            onBuyClicked = {
                viewModel.openOrderSheet(asset, OrderType.BUY)
            },
            onSellClicked = {
                viewModel.openOrderSheet(asset, OrderType.SELL)
            },
            onFavoriteToggle = {
                viewModel.toggleAssetFavorite(asset.symbol)
            }
        )
    }

    // Order Execution Sheet
    orderSheetAsset?.let { asset ->
        OrderSheet(
            asset = asset,
            initialOrderType = orderSheetType,
            availableCash = wallet.availableCash,
            onClose = { viewModel.closeOrderSheet() },
            onSubmitOrder = { oType, pType, execType, limPrice, qty, sl, tp, notes ->
                viewModel.placeOrder(
                    asset = asset,
                    orderType = oType,
                    productType = pType,
                    executionType = execType,
                    targetLimitPrice = limPrice,
                    quantity = qty,
                    stopLoss = sl,
                    targetPrice = tp,
                    notes = notes
                )
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetDialog.value = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "वर्चुअल फंड्स रीसेट करें?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "क्या आप अपने वर्चुअल बैलेंस को दोबारा ₹10,00,000 पर रीसेट करना चाहते हैं? इससे आपकी सभी खुली पोजीशन और होल्डिंग्स साफ़ हो जाएंगी ताकि आप नए सिरे से प्रैक्टिस कर सकें।",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetAccount(1000000.0) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                ) {
                    Text("हाँ, रीसेट करें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetDialog.value = false }) {
                    Text("रद्द करें", color = TextMuted)
                }
            }
        )
    }

    // Add Funds Dialog
    if (showAddFundsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showAddFundsDialog.value = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "वर्चुअल कैश जोड़ें (+ Add Funds)",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "जितना चाहें उतना डेमो कैश चुनें:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    listOf(100000.0, 500000.0, 1000000.0).forEach { amount ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addFunds(amount) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+ ₹${String.format("%,.0f", amount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BullGreen
                                )
                                Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.showAddFundsDialog.value = false }) {
                    Text("बंद करें", color = TextMuted)
                }
            }
        )
    }
}
