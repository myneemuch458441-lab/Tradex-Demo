package com.example.data.engine

import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MarketEngine {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _assetsFlow = MutableStateFlow<List<MarketAsset>>(emptyList())
    val assetsFlow: StateFlow<List<MarketAsset>> = _assetsFlow.asStateFlow()

    private var tickJob: Job? = null

    init {
        initializeAssets()
        startLiveTicks()
    }

    private fun initializeAssets() {
        val initialList = listOf(
            // --- INDICES ---
            createInitialAsset("idx_1", "NIFTY 50", "Nifty 50 Index", AssetCategory.INDEX, 24320.0, 24150.0),
            createInitialAsset("idx_2", "BANK NIFTY", "Nifty Bank Index", AssetCategory.INDEX, 51480.0, 51100.0),
            createInitialAsset("idx_3", "SENSEX", "BSE Sensex Index", AssetCategory.INDEX, 80240.0, 79800.0),
            createInitialAsset("idx_4", "NIFTY IT", "Nifty IT Sector", AssetCategory.INDEX, 41850.0, 42100.0),

            // --- EQUITIES ---
            createInitialAsset("eq_1", "RELIANCE", "Reliance Industries Ltd.", AssetCategory.EQUITY, 2980.50, 2945.0),
            createInitialAsset("eq_2", "TCS", "Tata Consultancy Services", AssetCategory.EQUITY, 4125.00, 4150.0),
            createInitialAsset("eq_3", "HDFCBANK", "HDFC Bank Ltd.", AssetCategory.EQUITY, 1684.20, 1665.0),
            createInitialAsset("eq_4", "TATAMOTORS", "Tata Motors Passenger & EV", AssetCategory.EQUITY, 982.40, 960.0),
            createInitialAsset("eq_5", "INFY", "Infosys Technologies", AssetCategory.EQUITY, 1855.10, 1840.0),
            createInitialAsset("eq_6", "SBIN", "State Bank of India", AssetCategory.EQUITY, 824.75, 815.0),
            createInitialAsset("eq_7", "ICICIBANK", "ICICI Bank Ltd.", AssetCategory.EQUITY, 1245.60, 1230.0),
            createInitialAsset("eq_8", "ITC", "ITC Ltd. FMCG & Hotels", AssetCategory.EQUITY, 492.30, 496.0),
            createInitialAsset("eq_9", "BHARTIARTL", "Bharti Airtel Telecom", AssetCategory.EQUITY, 1632.00, 1610.0),
            createInitialAsset("eq_10", "LT", "Larsen & Toubro Infrastructure", AssetCategory.EQUITY, 3560.00, 3520.0),
            createInitialAsset("eq_11", "BAJFINANCE", "Bajaj Finance Ltd.", AssetCategory.EQUITY, 7150.00, 7220.0),
            createInitialAsset("eq_12", "MARUTI", "Maruti Suzuki India", AssetCategory.EQUITY, 12400.00, 12250.0),

            // --- COMMODITIES ---
            createInitialAsset("com_1", "GOLD 24K", "Gold MCX (per 10g)", AssetCategory.COMMODITY, 78650.0, 78200.0),
            createInitialAsset("com_2", "SILVER", "Silver MCX (per 1kg)", AssetCategory.COMMODITY, 94200.0, 93500.0),
            createInitialAsset("com_3", "CRUDE OIL", "Crude Oil WTI MCX", AssetCategory.COMMODITY, 5940.0, 6010.0),
            createInitialAsset("com_4", "NATURAL GAS", "Natural Gas MCX", AssetCategory.COMMODITY, 218.40, 212.0),

            // --- CRYPTO ---
            createInitialAsset("cry_1", "BTC/USDT", "Bitcoin USD", AssetCategory.CRYPTO, 67450.0, 66800.0),
            createInitialAsset("cry_2", "ETH/USDT", "Ethereum USD", AssetCategory.CRYPTO, 2680.0, 2610.0),
            createInitialAsset("cry_3", "SOL/USDT", "Solana USD", AssetCategory.CRYPTO, 178.50, 172.0)
        )
        _assetsFlow.value = initialList
    }

    private fun createInitialAsset(
        id: String,
        symbol: String,
        name: String,
        category: AssetCategory,
        basePrice: Double,
        prevClose: Double
    ): MarketAsset {
        val candles = generateHistoricalCandles(basePrice, 25)
        val high = candles.maxOfOrNull { it.high } ?: (basePrice * 1.02)
        val low = candles.minOfOrNull { it.low } ?: (basePrice * 0.98)
        val open = candles.firstOrNull()?.open ?: prevClose

        return MarketAsset(
            id = id,
            symbol = symbol,
            name = name,
            category = category,
            currentPrice = roundTwo(basePrice),
            previousClose = roundTwo(prevClose),
            openPrice = roundTwo(open),
            dayHigh = roundTwo(max(high, basePrice)),
            dayLow = roundTwo(min(low, basePrice)),
            volume = (Random.nextLong(250_000, 8_500_000)),
            history = candles,
            depth = generateMarketDepth(basePrice),
            isFavorite = symbol in listOf("NIFTY 50", "RELIANCE", "TATAMOTORS", "GOLD 24K")
        )
    }

    private fun generateHistoricalCandles(currentPrice: Double, count: Int): List<CandleData> {
        val candles = mutableListOf<CandleData>()
        val now = System.currentTimeMillis()
        val intervalMs = 5 * 60 * 1000L // 5 minute candles
        var current = currentPrice * (1.0 - (Random.nextDouble(-0.015, 0.015)))

        for (i in count downTo 1) {
            val open = current
            val delta = open * (Random.nextDouble(-0.006, 0.007))
            val close = open + delta
            val high = max(open, close) + (open * Random.nextDouble(0.0005, 0.003))
            val low = min(open, close) - (open * Random.nextDouble(0.0005, 0.003))
            val volume = Random.nextLong(1500, 45000)
            val time = now - (i * intervalMs)

            candles.add(
                CandleData(
                    timestamp = time,
                    open = roundTwo(open),
                    high = roundTwo(high),
                    low = roundTwo(low),
                    close = roundTwo(close),
                    volume = volume
                )
            )
            current = close
        }
        return candles
    }

    private fun generateMarketDepth(price: Double): MarketDepth {
        val bids = mutableListOf<MarketDepthEntry>()
        val asks = mutableListOf<MarketDepthEntry>()
        val tick = if (price > 10000) 5.0 else if (price > 1000) 0.5 else 0.05

        for (i in 1..5) {
            bids.add(
                MarketDepthEntry(
                    price = roundTwo(price - (i * tick)),
                    quantity = Random.nextInt(50, 1500),
                    orders = Random.nextInt(3, 40)
                )
            )
            asks.add(
                MarketDepthEntry(
                    price = roundTwo(price + (i * tick)),
                    quantity = Random.nextInt(50, 1500),
                    orders = Random.nextInt(3, 40)
                )
            )
        }
        return MarketDepth(bids = bids, asks = asks)
    }

    private fun startLiveTicks() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                delay(1000L) // Real-time tick every 1 sec
                tickStep()
            }
        }
    }

    private fun tickStep() {
        val currentList = _assetsFlow.value
        if (currentList.isEmpty()) return

        // Pick 4-8 assets to fluctuate per second for natural staggered market feel
        val updated = currentList.map { asset ->
            if (Random.nextDouble() < 0.65) {
                // Fluctuates
                val volatility = when (asset.category) {
                    AssetCategory.CRYPTO -> 0.0035
                    AssetCategory.COMMODITY -> 0.0018
                    AssetCategory.INDEX -> 0.0009
                    else -> 0.0015
                }
                val pctChange = Random.nextDouble(-volatility, volatility)
                val newPrice = roundTwo(asset.currentPrice * (1.0 + pctChange))
                val flash = if (newPrice > asset.currentPrice) PriceFlash.UP else if (newPrice < asset.currentPrice) PriceFlash.DOWN else PriceFlash.NONE

                val newHigh = max(asset.dayHigh, newPrice)
                val newLow = min(asset.dayLow, newPrice)
                val newVol = asset.volume + Random.nextLong(20, 800)

                // Update latest candle
                val updatedCandles = if (asset.history.isNotEmpty()) {
                    val last = asset.history.last()
                    val modifiedLast = last.copy(
                        high = max(last.high, newPrice),
                        low = min(last.low, newPrice),
                        close = newPrice,
                        volume = last.volume + Random.nextLong(5, 50)
                    )
                    asset.history.dropLast(1) + modifiedLast
                } else {
                    asset.history
                }

                asset.copy(
                    currentPrice = newPrice,
                    dayHigh = roundTwo(newHigh),
                    dayLow = roundTwo(newLow),
                    volume = newVol,
                    history = updatedCandles,
                    depth = generateMarketDepth(newPrice),
                    priceFlash = flash
                )
            } else {
                asset.copy(priceFlash = PriceFlash.NONE)
            }
        }

        _assetsFlow.value = updated
    }

    private fun roundTwo(value: Double): Double {
        return kotlin.math.round(value * 100.0) / 100.0
    }

    fun toggleFavorite(symbol: String) {
        _assetsFlow.value = _assetsFlow.value.map {
            if (it.symbol == symbol) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun getAssetBySymbol(symbol: String): MarketAsset? {
        return _assetsFlow.value.find { it.symbol == symbol }
    }
}
