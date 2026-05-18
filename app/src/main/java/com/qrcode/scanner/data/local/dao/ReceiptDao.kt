package com.qrcode.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.qrcode.scanner.data.local.entity.ReceiptEntity

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insert(entity: ReceiptEntity): Long

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAll(): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE rawId = :rawId")
    suspend fun getByRawId(rawId: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE fiscalDriveNumber = :fn AND fiscalDocumentNumber = :fd AND fiscalSign = :fp LIMIT 1")
    suspend fun findByFiscalInfo(fn: String, fd: String, fp: String): ReceiptEntity?
}
