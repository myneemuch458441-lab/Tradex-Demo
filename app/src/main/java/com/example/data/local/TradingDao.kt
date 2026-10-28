package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingDao {

    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE status = 'PENDING'")
    suspend fun getPendingOrdersList(): List<OrderEntity>

    // Positions
    @Query("SELECT * FROM positions ORDER BY timestamp DESC")
    fun getAllPositions(): Flow<List<PositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: PositionEntity)

    @Query("DELETE FROM positions WHERE id = :positionId")
    suspend fun deletePosition(positionId: String)

    @Query("DELETE FROM positions")
    suspend fun deleteAllPositions()

    @Query("SELECT * FROM positions WHERE id = :positionId")
    suspend fun getPositionById(positionId: String): PositionEntity?

    @Query("SELECT * FROM positions")
    suspend fun getPositionsList(): List<PositionEntity>

    // Holdings
    @Query("SELECT * FROM holdings ORDER BY symbol ASC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity)

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE symbol = :symbol")
    suspend fun deleteHolding(symbol: String)

    @Query("DELETE FROM holdings")
    suspend fun deleteAllHoldings()

    @Query("SELECT * FROM holdings WHERE symbol = :symbol")
    suspend fun getHoldingBySymbol(symbol: String): HoldingEntity?

    // Wallet
    @Query("SELECT * FROM wallet WHERE id = 1")
    fun getWalletFlow(): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet WHERE id = 1")
    suspend fun getWalletDirect(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWallet(wallet: WalletEntity)

    // Favorites
    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE symbol = :symbol")
    suspend fun removeFavorite(symbol: String)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}
