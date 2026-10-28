package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.MarketEngine
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val marketEngine = MarketEngine()
    val repository = TradingRepository(database.tradingDao(), marketEngine)

    // UI Navigation & Filters
    val selectedTab = MutableStateFlow(MainTab.WATCHLIST)
    val selectedCategory = MutableStateFlow(AssetCategory.ALL)
    val searchQuery = MutableStateFlow("")
    val showFavoritesOnly = MutableStateFlow(false)

    // Modals & Sheets
    val selectedAssetDetail = MutableStateFlow<MarketAsset?>(null)
    val orderSheetAsset = MutableStateFlow<MarketAsset?>(null)
    val orderSheetType = MutableStateFlow(OrderType.BUY)
    val showResetDialog = MutableStateFlow(false)
    val showAddFundsDialog = MutableStateFlow(false)
    val snackbarMessage = MutableStateFlow<String?>(null)

    // Data streams
    val assets: StateFlow<List<MarketAsset>> = repository.assetsFlow
    val positions: StateFlow<List<Position>> = repository.positionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val holdings: StateFlow<List<Holding>> = repository.holdingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val orders: StateFlow<List<TradeOrder>> = repository.ordersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val wallet: StateFlow<UserWallet> = repository.walletFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserWallet())

    // Keep detail view asset in sync with real-time ticks
    init {
        viewModelScope.launch {
            assets.collect { assetList ->
                val currentDetail = selectedAssetDetail.value
                if (currentDetail != null) {
                    val updated = assetList.find { it.symbol == currentDetail.symbol }
                    if (updated != null) {
                        selectedAssetDetail.value = updated
                    }
                }
            }
        }
    }

    // Filtered Assets for Watchlist
    val filteredAssets: StateFlow<List<MarketAsset>> = combine(
        assets,
        selectedCategory,
        searchQuery,
        showFavoritesOnly
    ) { assetList, category, query, favOnly ->
        assetList.filter { asset ->
            val matchesCategory = (category == AssetCategory.ALL || asset.category == category)
            val matchesQuery = query.isBlank() ||
                    asset.symbol.contains(query, ignoreCase = true) ||
                    asset.name.contains(query, ignoreCase = true)
            val matchesFav = !favOnly || asset.isFavorite
            matchesCategory && matchesQuery && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculations
    val totalUnrealizedPnl: StateFlow<Double> = positions.map { list ->
        list.sumOf { it.unrealizedPnl }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalHoldingsInvested: StateFlow<Double> = holdings.map { list ->
        list.sumOf { it.investedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalHoldingsCurrentValue: StateFlow<Double> = holdings.map { list ->
        list.sumOf { it.currentValue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPortfolioValue: StateFlow<Double> = combine(
        wallet,
        totalHoldingsCurrentValue,
        totalUnrealizedPnl
    ) { w, holdVal, unpnl ->
        w.availableCash + holdVal + unpnl
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000000.0)

    // Actions
    fun selectTab(tab: MainTab) {
        selectedTab.value = tab
    }

    fun setCategory(category: AssetCategory) {
        selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        showFavoritesOnly.value = !showFavoritesOnly.value
    }

    fun toggleAssetFavorite(symbol: String) {
        viewModelScope.launch {
            repository.toggleFavorite(symbol)
        }
    }

    fun openAssetDetail(asset: MarketAsset) {
        selectedAssetDetail.value = asset
    }

    fun closeAssetDetail() {
        selectedAssetDetail.value = null
    }

    fun openOrderSheet(asset: MarketAsset, orderType: OrderType) {
        orderSheetAsset.value = asset
        orderSheetType.value = orderType
    }

    fun closeOrderSheet() {
        orderSheetAsset.value = null
    }

    fun placeOrder(
        asset: MarketAsset,
        orderType: OrderType,
        productType: ProductType,
        executionType: OrderExecutionType,
        targetLimitPrice: Double?,
        quantity: Int,
        stopLoss: Double?,
        targetPrice: Double?,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val result = repository.placeOrder(
                asset = asset,
                orderType = orderType,
                productType = productType,
                executionType = executionType,
                targetLimitPrice = targetLimitPrice,
                quantity = quantity,
                stopLoss = stopLoss,
                targetPrice = targetPrice,
                notes = notes
            )
            result.onSuccess { msg ->
                snackbarMessage.value = "✓ $msg"
                closeOrderSheet()
            }.onFailure { err ->
                snackbarMessage.value = "⚠ ${err.message}"
            }
        }
    }

    fun squareOffPosition(positionId: String) {
        viewModelScope.launch {
            val result = repository.squareOffPosition(positionId)
            result.onSuccess { pnl ->
                val sign = if (pnl >= 0) "+" else ""
                snackbarMessage.value = "पोजीशन सफलतापूर्वक काटी गई! P&L: ₹$sign${String.format("%,.2f", pnl)}"
            }.onFailure {
                snackbarMessage.value = "त्रुटि: ${it.message}"
            }
        }
    }

    fun squareOffAllPositions() {
        viewModelScope.launch {
            val count = repository.squareOffAllPositions()
            snackbarMessage.value = "सभी $count पोजीशन काट दी गईं!"
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.cancelOrder(orderId)
            result.onSuccess {
                snackbarMessage.value = it
            }.onFailure {
                snackbarMessage.value = it.message
            }
        }
    }

    fun resetAccount(amount: Double = 1000000.0) {
        viewModelScope.launch {
            repository.resetVirtualCapital(amount)
            showResetDialog.value = false
            snackbarMessage.value = "✓ वर्चुअल फंड्स रीसेट हुए! नया बैलेंस: ₹${String.format("%,.0f", amount)}"
        }
    }

    fun addFunds(amount: Double) {
        viewModelScope.launch {
            repository.addFunds(amount)
            showAddFundsDialog.value = false
            snackbarMessage.value = "✓ +₹${String.format("%,.0f", amount)} वर्चुअल कैश सफलतापूर्वक जोड़ा गया!"
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }
}
