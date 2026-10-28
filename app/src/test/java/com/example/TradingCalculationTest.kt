package com.example

import com.example.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradingCalculationTest {

    @Test
    fun testBuyPositionPnl() {
        val position = Position(
            id = "pos_1",
            orderId = "ord_1",
            symbol = "RELIANCE",
            assetName = "Reliance Industries",
            orderType = OrderType.BUY,
            productType = ProductType.INTRADAY,
            quantity = 10,
            entryPrice = 2900.0,
            currentPrice = 2950.0,
            timestamp = System.currentTimeMillis()
        )
        // Profit: (2950 - 2900) * 10 = 500.0
        assertEquals(500.0, position.unrealizedPnl, 0.001)
    }

    @Test
    fun testShortPositionPnl() {
        val position = Position(
            id = "pos_2",
            orderId = "ord_2",
            symbol = "TATAMOTORS",
            assetName = "Tata Motors",
            orderType = OrderType.SELL, // Short
            productType = ProductType.INTRADAY,
            quantity = 20,
            entryPrice = 1000.0,
            currentPrice = 980.0, // Price dropped -> profit on short
            timestamp = System.currentTimeMillis()
        )
        // Profit: (1000 - 980) * 20 = 400.0
        assertEquals(400.0, position.unrealizedPnl, 0.001)
    }

    @Test
    fun testHoldingPnl() {
        val holding = Holding(
            symbol = "INFY",
            assetName = "Infosys",
            quantity = 15,
            avgBuyPrice = 1800.0,
            currentPrice = 1860.0
        )
        assertEquals(27000.0, holding.investedAmount, 0.001)
        assertEquals(27900.0, holding.currentValue, 0.001)
        assertEquals(900.0, holding.pnl, 0.001)
        assertTrue(holding.pnlPercent > 3.0)
    }

    @Test
    fun testIntradayLeverageMargin() {
        val price = 1000.0
        val qty = 50
        val total = price * qty // 50,000
        val margin = total / ProductType.INTRADAY.leverage // 50,000 / 5 = 10,000
        assertEquals(10000.0, margin, 0.001)
    }
}
