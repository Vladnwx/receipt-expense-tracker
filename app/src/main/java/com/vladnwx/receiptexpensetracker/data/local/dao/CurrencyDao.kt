package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.CurrencyEntity

@Dao
interface CurrencyDao {
    @Insert
    suspend fun insert(entity: CurrencyEntity): Long

    @Insert
    suspend fun insertAll(entities: List<CurrencyEntity>)

    @Query("SELECT * FROM currencies ORDER BY code ASC")
    suspend fun getAll(): List<CurrencyEntity>

    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): CurrencyEntity?

    @Query("SELECT * FROM currencies WHERE isBase = 1 LIMIT 1")
    suspend fun getBaseCurrency(): CurrencyEntity?

    @Query("DELETE FROM currencies")
    suspend fun deleteAll()

    @Query("UPDATE currencies SET rateToBase = :rate, updatedAt = :updatedAt WHERE code = :code")
    suspend fun updateRate(code: String, rate: Double, updatedAt: Long)
}
