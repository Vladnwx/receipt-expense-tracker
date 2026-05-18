package com.qrcode.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity

@Dao
interface ReceiptItemDao {
    @Insert
    suspend fun insert(entity: ReceiptItemEntity): Long

    @Insert
    suspend fun insertAll(entities: List<ReceiptItemEntity>)

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    suspend fun getByReceiptId(receiptId: Long): List<ReceiptItemEntity>

    @Query("UPDATE receipt_items SET categoryId = :categoryId WHERE id = :id")
    suspend fun updateCategory(id: Long, categoryId: Long?)
}
