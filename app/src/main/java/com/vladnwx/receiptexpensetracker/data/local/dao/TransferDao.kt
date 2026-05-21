package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.TransferEntity

@Dao
interface TransferDao {
    @Insert
    suspend fun insert(entity: TransferEntity): Long

    @Query("SELECT * FROM transfers ORDER BY date DESC")
    suspend fun getAll(): List<TransferEntity>

    @Query("SELECT * FROM transfers WHERE fromAccountId = :accountId OR toAccountId = :accountId ORDER BY date DESC")
    suspend fun getByAccountId(accountId: Long): List<TransferEntity>

    @Query("DELETE FROM transfers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transfers")
    suspend fun deleteAll()
}
