package com.example.data.model

enum class AssetCategory(val displayName: String, val hindiName: String) {
    ALL("All", "सभी"),
    EQUITY("Stocks", "शेयर"),
    INDEX("Indices", "इंडेक्स"),
    COMMODITY("Commodities", "कमोडिटी"),
    CRYPTO("Crypto", "क्रिप्टो")
}

enum class PriceFlash {
    NONE, UP, DOWN
}

data class CandleData(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

data class MarketDepthEntry(
    val price: Double,
    val quantity: Int,
    val orders: Int
)

data class MarketDepth(
    val bids: List<MarketDepthEntry>,
    val asks: List<MarketDepthEntry>
)

data class MarketAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val category: AssetCategory,
    val currentPrice: Double,
    val previousClose: Double,
    val openPrice: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val volume: Long,
    val history: List<CandleData> = emptyList(),
    val depth: MarketDepth? = null,
    val isFavorite: Boolean = false,
    val priceFlash: PriceFlash = PriceFlash.NONE
) {
    val change: Double get() = currentPrice - previousClose
    val changePercent: Double get() = if (previousClose != 0.0) ((currentPrice - previousClose) / previousClose) * 100.0 else 0.0
    val isBullish: Boolean get() = change >= 0
}

enum class OrderType(val displayName: String) {
    BUY("BUY"),
    SELL("SELL")
}

enum class ProductType(val displayName: String, val subtitle: String, val leverage: Int) {
    INTRADAY("Intraday (MIS)", "Day trading • 5x virtual margin", 5),
    DELIVERY("Delivery (CNC)", "Long term • 1x margin", 1)
}

enum class OrderExecutionType(val displayName: String) {
    MARKET("Market"),
    LIMIT("Limit")
}

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    EXECUTED("Executed"),
    CLOSED("Closed / Squared Off"),
    CANCELLED("Cancelled")
}

data class TradeOrder(
    val id: String,
    val symbol: String,
    val assetName: String,
    val orderType: OrderType,
    val productType: ProductType,
    val executionType: OrderExecutionType,
    val targetLimitPrice: Double? = null,
    val executedPrice: Double,
    val quantity: Int,
    val status: OrderStatus,
    val timestamp: Long,
    val stopLoss: Double? = null,
    val targetPrice: Double? = null,
    val exitPrice: Double? = null,
    val realizedPnl: Double? = null,
    val notes: String = ""
)

data class Position(
    val id: String,
    val orderId: String,
    val symbol: String,
    val assetName: String,
    val orderType: OrderType,
    val productType: ProductType,
    val quantity: Int,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double? = null,
    val targetPrice: Double? = null,
    val timestamp: Long
) {
    // For BUY: (current - entry) * qty; For SELL (short): (entry - current) * qty
    val unrealizedPnl: Double
        get() = if (orderType == OrderType.BUY) {
            (currentPrice - entryPrice) * quantity
        } else {
            (entryPrice - currentPrice) * quantity
        }

    val unrealizedPnlPercent: Double
        get() = if (entryPrice > 0.0) {
            if (orderType == OrderType.BUY) {
                ((currentPrice - entryPrice) / entryPrice) * 100.0 * productType.leverage
            } else {
                ((entryPrice - currentPrice) / entryPrice) * 100.0 * productType.leverage
            }
        } else 0.0
}

data class Holding(
    val symbol: String,
    val assetName: String,
    val quantity: Int,
    val avgBuyPrice: Double,
    val currentPrice: Double
) {
    val investedAmount: Double get() = quantity * avgBuyPrice
    val currentValue: Double get() = quantity * currentPrice
    val pnl: Double get() = currentValue - investedAmount
    val pnlPercent: Double get() = if (investedAmount > 0.0) (pnl / investedAmount) * 100.0 else 0.0
}

data class UserWallet(
    val availableCash: Double = 1000000.0, // Default ₹10,00,000 virtual balance
    val totalCapitalAdded: Double = 1000000.0,
    val realizedPnl: Double = 0.0,
    val dailyPnl: Double = 0.0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val resetCount: Int = 0
) {
    val totalTrades: Int get() = winningTrades + losingTrades
    val winRate: Double get() = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100.0 else 0.0
}

enum class MainTab(val title: String, val hindiTitle: String) {
    WATCHLIST("Watchlist", "वॉचलिस्ट"),
    POSITIONS("Positions", "पोजीशन"),
    HOLDINGS("Holdings", "होल्डिंग्स"),
    ORDERS("Orders", "ऑर्डर्स"),
    ANALYTICS("Analytics", "एनालिटिक्स")
}
