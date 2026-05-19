package com.qrcode.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.qrcode.scanner.data.local.entity.ExpenseEntity

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(entity: ExpenseEntity): Long

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE receiptId = :receiptId")
    suspend fun getByReceiptId(receiptId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    suspend fun getByCategoryId(categoryId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<ExpenseEntity>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :start AND :end")
    suspend fun getTotalByDateRange(start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotal(): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getTotalByCategory(categoryId: Long): Double?
}
