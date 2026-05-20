package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptRawEntity

@Dao
interface ReceiptRawDao {
    @Insert
    suspend fun insert(entity: ReceiptRawEntity): Long

    @Query("SELECT * FROM receipt_raw ORDER BY scannedAt DESC")
    suspend fun getAll(): List<ReceiptRawEntity>

    @Query("SELECT * FROM receipt_raw WHERE id = :id")
    suspend fun getById(id: Long): ReceiptRawEntity?

    @Query("UPDATE receipt_raw SET isParsed = 1 WHERE id = :id")
    suspend fun markParsed(id: Long)

    @Query("DELETE FROM receipt_raw WHERE id = :id")
    suspend fun deleteById(id: Long)
}
