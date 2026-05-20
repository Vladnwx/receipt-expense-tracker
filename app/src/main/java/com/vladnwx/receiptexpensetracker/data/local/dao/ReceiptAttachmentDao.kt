package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptAttachmentEntity

@Dao
interface ReceiptAttachmentDao {
    @Insert
    suspend fun insert(entity: ReceiptAttachmentEntity): Long

    @Query("SELECT * FROM receipt_attachments WHERE rawId = :rawId ORDER BY uploadedAt DESC")
    suspend fun getByRawId(rawId: Long): List<ReceiptAttachmentEntity>

    @Query("SELECT * FROM receipt_attachments WHERE receiptId = :receiptId ORDER BY uploadedAt DESC")
    suspend fun getByReceiptId(receiptId: Long): List<ReceiptAttachmentEntity>

    @Query("SELECT * FROM receipt_attachments WHERE receiptId IS NULL AND rawId IS NULL")
    suspend fun getOrphans(): List<ReceiptAttachmentEntity>

    @Query("DELETE FROM receipt_attachments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE receipt_attachments SET ocrText = :ocrText WHERE id = :id")
    suspend fun updateOcrText(id: Long, ocrText: String)
}
