package com.qrcode.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.qrcode.scanner.data.local.entity.ReceiptEntity

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insert(entity: ReceiptEntity): Long

    @Update
    suspend fun update(entity: ReceiptEntity)

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    suspend fun getAll(): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getById(id: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE rawId = :rawId")
    suspend fun getByRawId(rawId: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE fiscalDriveNumber = :fn AND fiscalDocumentNumber = :fd AND fiscalSign = :fp LIMIT 1")
    suspend fun findByFiscalInfo(fn: String, fd: String, fp: String): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE status != 'Checked' ORDER BY date DESC")
    suspend fun getUnchecked(): List<ReceiptEntity>

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
