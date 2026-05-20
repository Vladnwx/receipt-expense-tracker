package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(entity: ExpenseEntity): Long

    @Update
    suspend fun update(entity: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    suspend fun getByType(type: OperationType): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE receiptId = :receiptId")
    suspend fun getByReceiptId(receiptId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    suspend fun getByCategoryId(categoryId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun getByAccountId(accountId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE eventId = :eventId ORDER BY date DESC")
    suspend fun getByEventId(eventId: Long): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE advanceReportId = :reportId ORDER BY date DESC")
    suspend fun getByAdvanceReportId(reportId: Long): List<ExpenseEntity>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :start AND :end")
    suspend fun getTotalByDateRange(start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE type = :type AND date BETWEEN :start AND :end")
    suspend fun getTotalByTypeAndDateRange(type: OperationType, start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotal(): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getTotalByCategory(categoryId: Long): Double?

    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun search(query: String): List<ExpenseEntity>

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expenses WHERE receiptId = :receiptId")
    suspend fun deleteByReceiptId(receiptId: Long)
}
