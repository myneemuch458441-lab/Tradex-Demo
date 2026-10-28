package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val assetName: String,
    val orderType: String, // BUY, SELL
    val productType: String, // INTRADAY, DELIVERY
    val executionType: String, // MARKET, LIMIT
    val targetLimitPrice: Double?,
    val executedPrice: Double,
    val quantity: Int,
    val status: String, // PENDING, EXECUTED, CLOSED, CANCELLED
    val timestamp: Long,
    val stopLoss: Double?,
    val targetPrice: Double?,
    val exitPrice: Double?,
    val realizedPnl: Double?,
    val notes: String
)

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val symbol: String,
    val assetName: String,
    val orderType: String,
    val productType: String,
    val quantity: Int,
    val entryPrice: Double,
    val stopLoss: Double?,
    val targetPrice: Double?,
    val timestamp: Long
)

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val symbol: String,
    val assetName: String,
    val quantity: Int,
    val avgBuyPrice: Double
)

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val availableCash: Double,
    val totalCapitalAdded: Double,
    val realizedPnl: Double,
    val dailyPnl: Double,
    val winningTrades: Int,
    val losingTrades: Int,
    val resetCount: Int
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val symbol: String
)
