package com.example.data.repository

import com.example.data.engine.MarketEngine
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class TradingRepository(
    private val dao: TradingDao,
    val marketEngine: MarketEngine
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val assetsFlow: StateFlow<List<MarketAsset>> = marketEngine.assetsFlow

    // Orders from DB
    val ordersFlow: Flow<List<TradeOrder>> = dao.getAllOrders().map { list ->
        list.map { it.toDomain() }
    }

    // Positions from DB merged with live market prices
    val positionsFlow: Flow<List<Position>> = combine(
        dao.getAllPositions(),
        assetsFlow
    ) { posEntities, assets ->
        val assetMap = assets.associateBy { it.symbol }
        posEntities.map { entity ->
            val liveAsset = assetMap[entity.symbol]
            val livePrice = liveAsset?.currentPrice ?: entity.entryPrice
            entity.toDomain(livePrice)
        }
    }

    // Holdings from DB merged with live market prices
    val holdingsFlow: Flow<List<Holding>> = combine(
        dao.getAllHoldings(),
        assetsFlow
    ) { holdingEntities, assets ->
        val assetMap = assets.associateBy { it.symbol }
        holdingEntities.map { entity ->
            val liveAsset = assetMap[entity.symbol]
            val livePrice = liveAsset?.currentPrice ?: entity.avgBuyPrice
            Holding(
                symbol = entity.symbol,
                assetName = entity.assetName,
                quantity = entity.quantity,
                avgBuyPrice = entity.avgBuyPrice,
                currentPrice = livePrice
            )
        }
    }

    // Wallet from DB with fallback
    val walletFlow: Flow<UserWallet> = dao.getWalletFlow().map { entity ->
        entity?.toDomain() ?: UserWallet()
    }

    init {
        // Observe market ticks to check pending limit orders & stop-loss / target executions
        scope.launch {
            assetsFlow.collect { assets ->
                checkPendingOrdersAndTriggers(assets)
            }
        }
    }

    private suspend fun checkPendingOrdersAndTriggers(assets: List<MarketAsset>) {
        val assetMap = assets.associateBy { it.symbol }
        val pendingOrders = dao.getPendingOrdersList()

        for (order in pendingOrders) {
            val asset = assetMap[order.symbol] ?: continue
            val limit = order.targetLimitPrice ?: continue

            val shouldExecute = if (order.orderType == OrderType.BUY.name) {
                asset.currentPrice <= limit
            } else {
                asset.currentPrice >= limit
            }

            if (shouldExecute) {
                executeLimitOrder(order, asset.currentPrice)
            }
        }

        // Check active positions for Stop Loss & Target
        val positions = dao.getPositionsList()
        for (pos in positions) {
            val asset = assetMap[pos.symbol] ?: continue
            val currentPrice = asset.currentPrice

            var triggerExit = false
            var exitReason = ""

            if (pos.orderType == OrderType.BUY.name) {
                if (pos.stopLoss != null && currentPrice <= pos.stopLoss) {
                    triggerExit = true
                    exitReason = "Stop-loss hit at ₹$currentPrice"
                } else if (pos.targetPrice != null && currentPrice >= pos.targetPrice) {
                    triggerExit = true
                    exitReason = "Target reached at ₹$currentPrice"
                }
            } else { // SELL (Short)
                if (pos.stopLoss != null && currentPrice >= pos.stopLoss) {
                    triggerExit = true
                    exitReason = "Stop-loss hit at ₹$currentPrice"
                } else if (pos.targetPrice != null && currentPrice <= pos.targetPrice) {
                    triggerExit = true
                    exitReason = "Target reached at ₹$currentPrice"
                }
            }

            if (triggerExit) {
                squareOffPosition(pos.id, currentPrice, exitReason)
            }
        }
    }

    private suspend fun executeLimitOrder(order: OrderEntity, executionPrice: Double) {
        val updatedOrder = order.copy(
            status = OrderStatus.EXECUTED.name,
            executedPrice = executionPrice,
            timestamp = System.currentTimeMillis()
        )
        dao.updateOrder(updatedOrder)

        if (order.productType == ProductType.INTRADAY.name) {
            val position = PositionEntity(
                id = UUID.randomUUID().toString(),
                orderId = order.id,
                symbol = order.symbol,
                assetName = order.assetName,
                orderType = order.orderType,
                productType = order.productType,
                quantity = order.quantity,
                entryPrice = executionPrice,
                stopLoss = order.stopLoss,
                targetPrice = order.targetPrice,
                timestamp = System.currentTimeMillis()
            )
            dao.insertPosition(position)
        } else {
            // Delivery holding
            addOrUpdateHolding(order.symbol, order.assetName, order.quantity, executionPrice)
        }
    }

    suspend fun placeOrder(
        asset: MarketAsset,
        orderType: OrderType,
        productType: ProductType,
        executionType: OrderExecutionType,
        targetLimitPrice: Double?,
        quantity: Int,
        stopLoss: Double?,
        targetPrice: Double?,
        notes: String = ""
    ): Result<String> {
        val wallet = dao.getWalletDirect()?.toDomain() ?: UserWallet()
        val effectivePrice = if (executionType == OrderExecutionType.LIMIT && targetLimitPrice != null) {
            targetLimitPrice
        } else {
            asset.currentPrice
        }

        val requiredMargin = (effectivePrice * quantity) / productType.leverage

        if (orderType == OrderType.BUY) {
            if (wallet.availableCash < requiredMargin) {
                return Result.failure(Exception("पर्याप्त बैलेंस नहीं है! आवश्यक: ₹${formatNumber(requiredMargin)}, उपलब्ध: ₹${formatNumber(wallet.availableCash)}"))
            }
        } else { // SELL
            if (productType == ProductType.DELIVERY) {
                val existingHolding = dao.getHoldingBySymbol(asset.symbol)
                if (existingHolding == null || existingHolding.quantity < quantity) {
                    return Result.failure(Exception("सेल करने के लिए होल्डिंग्स में पर्याप्त शेयर नहीं हैं!"))
                }
            } else {
                // Intraday short selling requires margin
                if (wallet.availableCash < requiredMargin) {
                    return Result.failure(Exception("शॉर्ट सेलिंग के लिए पर्याप्त मार्जिन नहीं है!"))
                }
            }
        }

        val orderId = UUID.randomUUID().toString()
        val isPending = executionType == OrderExecutionType.LIMIT && (
                (orderType == OrderType.BUY && targetLimitPrice != null && targetLimitPrice < asset.currentPrice) ||
                (orderType == OrderType.SELL && targetLimitPrice != null && targetLimitPrice > asset.currentPrice)
        )

        val status = if (isPending) OrderStatus.PENDING else OrderStatus.EXECUTED
        val executedPrice = if (isPending) 0.0 else asset.currentPrice

        val orderEntity = OrderEntity(
            id = orderId,
            symbol = asset.symbol,
            assetName = asset.name,
            orderType = orderType.name,
            productType = productType.name,
            executionType = executionType.name,
            targetLimitPrice = targetLimitPrice,
            executedPrice = executedPrice,
            quantity = quantity,
            status = status.name,
            timestamp = System.currentTimeMillis(),
            stopLoss = stopLoss,
            targetPrice = targetPrice,
            exitPrice = null,
            realizedPnl = null,
            notes = notes
        )
        dao.insertOrder(orderEntity)

        if (!isPending) {
            // Deduct margin / update holdings
            if (productType == ProductType.INTRADAY) {
                val newCash = wallet.availableCash - requiredMargin
                dao.saveWallet(wallet.copy(availableCash = newCash).toEntity())

                val pos = PositionEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    symbol = asset.symbol,
                    assetName = asset.name,
                    orderType = orderType.name,
                    productType = productType.name,
                    quantity = quantity,
                    entryPrice = executedPrice,
                    stopLoss = stopLoss,
                    targetPrice = targetPrice,
                    timestamp = System.currentTimeMillis()
                )
                dao.insertPosition(pos)
            } else {
                // Delivery
                if (orderType == OrderType.BUY) {
                    val newCash = wallet.availableCash - (executedPrice * quantity)
                    dao.saveWallet(wallet.copy(availableCash = newCash).toEntity())
                    addOrUpdateHolding(asset.symbol, asset.name, quantity, executedPrice)
                } else {
                    // Delivery Sell
                    sellHolding(asset.symbol, quantity, executedPrice)
                }
            }
        }

        return Result.success(if (isPending) "लिमिट आर्डर सफलतापूर्वक रखा गया!" else "आर्डर तुरंत निष्पादित हुआ!")
    }

    private suspend fun addOrUpdateHolding(symbol: String, name: String, qty: Int, price: Double) {
        val existing = dao.getHoldingBySymbol(symbol)
        if (existing != null) {
            val totalQty = existing.quantity + qty
            val totalInvested = (existing.quantity * existing.avgBuyPrice) + (qty * price)
            val newAvg = totalInvested / totalQty
            dao.updateHolding(existing.copy(quantity = totalQty, avgBuyPrice = newAvg))
        } else {
            dao.insertHolding(HoldingEntity(symbol = symbol, assetName = name, quantity = qty, avgBuyPrice = price))
        }
    }

    private suspend fun sellHolding(symbol: String, sellQty: Int, sellPrice: Double) {
        val existing = dao.getHoldingBySymbol(symbol) ?: return
        val realized = (sellPrice - existing.avgBuyPrice) * sellQty
        val wallet = dao.getWalletDirect()?.toDomain() ?: UserWallet()

        val proceeds = sellQty * sellPrice
        val newCash = wallet.availableCash + proceeds
        val newRealizedPnl = wallet.realizedPnl + realized
        val newDailyPnl = wallet.dailyPnl + realized
        val isWin = realized > 0

        dao.saveWallet(
            wallet.copy(
                availableCash = newCash,
                realizedPnl = newRealizedPnl,
                dailyPnl = newDailyPnl,
                winningTrades = wallet.winningTrades + if (isWin) 1 else 0,
                losingTrades = wallet.losingTrades + if (!isWin) 1 else 0
            ).toEntity()
        )

        if (existing.quantity <= sellQty) {
            dao.deleteHolding(symbol)
        } else {
            dao.updateHolding(existing.copy(quantity = existing.quantity - sellQty))
        }
    }

    suspend fun squareOffPosition(positionId: String, currentPrice: Double? = null, reason: String = ""): Result<Double> {
        val pos = dao.getPositionById(positionId) ?: return Result.failure(Exception("पोजीशन नहीं मिली"))
        val liveAsset = marketEngine.getAssetBySymbol(pos.symbol)
        val exitPrice = currentPrice ?: (liveAsset?.currentPrice ?: pos.entryPrice)

        val pnl = if (pos.orderType == OrderType.BUY.name) {
            (exitPrice - pos.entryPrice) * pos.quantity
        } else {
            (pos.entryPrice - exitPrice) * pos.quantity
        }

        val marginReleased = (pos.entryPrice * pos.quantity) / (if (pos.productType == ProductType.INTRADAY.name) 5 else 1)
        val wallet = dao.getWalletDirect()?.toDomain() ?: UserWallet()

        val newCash = wallet.availableCash + marginReleased + pnl
        val newRealizedPnl = wallet.realizedPnl + pnl
        val newDailyPnl = wallet.dailyPnl + pnl
        val isWin = pnl > 0

        dao.saveWallet(
            wallet.copy(
                availableCash = newCash,
                realizedPnl = newRealizedPnl,
                dailyPnl = newDailyPnl,
                winningTrades = wallet.winningTrades + if (isWin) 1 else 0,
                losingTrades = wallet.losingTrades + if (!isWin) 1 else 0
            ).toEntity()
        )

        // Close order in history
        val originalOrder = dao.getOrderById(pos.orderId)
        if (originalOrder != null) {
            dao.updateOrder(
                originalOrder.copy(
                    status = OrderStatus.CLOSED.name,
                    exitPrice = exitPrice,
                    realizedPnl = pnl,
                    notes = if (reason.isNotBlank()) reason else originalOrder.notes
                )
            )
        }

        dao.deletePosition(positionId)
        return Result.success(pnl)
    }

    suspend fun squareOffAllPositions(): Int {
        val positions = dao.getPositionsList()
        var count = 0
        for (pos in positions) {
            squareOffPosition(pos.id)
            count++
        }
        return count
    }

    suspend fun cancelOrder(orderId: String): Result<String> {
        val order = dao.getOrderById(orderId) ?: return Result.failure(Exception("आर्डर नहीं मिला"))
        if (order.status != OrderStatus.PENDING.name) {
            return Result.failure(Exception("केवल पेंडिंग आर्डर रद्द किए जा सकते हैं"))
        }
        dao.updateOrder(order.copy(status = OrderStatus.CANCELLED.name))
        return Result.success("आर्डर रद्द कर दिया गया")
    }

    suspend fun resetVirtualCapital(amount: Double = 1000000.0) {
        val currentWallet = dao.getWalletDirect()?.toDomain() ?: UserWallet()
        dao.deleteAllPositions()
        dao.deleteAllHoldings()
        dao.saveWallet(
            WalletEntity(
                id = 1,
                availableCash = amount,
                totalCapitalAdded = amount,
                realizedPnl = 0.0,
                dailyPnl = 0.0,
                winningTrades = 0,
                losingTrades = 0,
                resetCount = currentWallet.resetCount + 1
            )
        )
    }

    suspend fun addFunds(amount: Double) {
        val currentWallet = dao.getWalletDirect()?.toDomain() ?: UserWallet()
        dao.saveWallet(
            currentWallet.copy(
                availableCash = currentWallet.availableCash + amount,
                totalCapitalAdded = currentWallet.totalCapitalAdded + amount
            ).toEntity()
        )
    }

    suspend fun toggleFavorite(symbol: String) {
        marketEngine.toggleFavorite(symbol)
    }

    private fun formatNumber(value: Double): String {
        return String.format("%,.2f", value)
    }
}

// Converters
private fun OrderEntity.toDomain() = TradeOrder(
    id = id,
    symbol = symbol,
    assetName = assetName,
    orderType = OrderType.valueOf(orderType),
    productType = ProductType.valueOf(productType),
    executionType = OrderExecutionType.valueOf(executionType),
    targetLimitPrice = targetLimitPrice,
    executedPrice = executedPrice,
    quantity = quantity,
    status = OrderStatus.valueOf(status),
    timestamp = timestamp,
    stopLoss = stopLoss,
    targetPrice = targetPrice,
    exitPrice = exitPrice,
    realizedPnl = realizedPnl,
    notes = notes
)

private fun PositionEntity.toDomain(livePrice: Double) = Position(
    id = id,
    orderId = orderId,
    symbol = symbol,
    assetName = assetName,
    orderType = OrderType.valueOf(orderType),
    productType = ProductType.valueOf(productType),
    quantity = quantity,
    entryPrice = entryPrice,
    currentPrice = livePrice,
    stopLoss = stopLoss,
    targetPrice = targetPrice,
    timestamp = timestamp
)

private fun WalletEntity.toDomain() = UserWallet(
    availableCash = availableCash,
    totalCapitalAdded = totalCapitalAdded,
    realizedPnl = realizedPnl,
    dailyPnl = dailyPnl,
    winningTrades = winningTrades,
    losingTrades = losingTrades,
    resetCount = resetCount
)

private fun UserWallet.toEntity() = WalletEntity(
    id = 1,
    availableCash = availableCash,
    totalCapitalAdded = totalCapitalAdded,
    realizedPnl = realizedPnl,
    dailyPnl = dailyPnl,
    winningTrades = winningTrades,
    losingTrades = losingTrades,
    resetCount = resetCount
)
