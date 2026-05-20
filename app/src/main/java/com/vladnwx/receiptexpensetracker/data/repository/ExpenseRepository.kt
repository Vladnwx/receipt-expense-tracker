package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun getAll(): List<ExpenseEntity> = expenseDao.getAll()
    suspend fun getById(id: Long): ExpenseEntity? = expenseDao.getById(id)
    suspend fun getByType(type: OperationType): List<ExpenseEntity> = expenseDao.getByType(type)
    suspend fun getByReceiptId(receiptId: Long): List<ExpenseEntity> = expenseDao.getByReceiptId(receiptId)
    suspend fun getByCategoryId(categoryId: Long): List<ExpenseEntity> = expenseDao.getByCategoryId(categoryId)
    suspend fun getByAccountId(accountId: Long): List<ExpenseEntity> = expenseDao.getByAccountId(accountId)
    suspend fun getByDateRange(start: Long, end: Long): List<ExpenseEntity> = expenseDao.getByDateRange(start, end)
    suspend fun getByEventId(eventId: Long): List<ExpenseEntity> = expenseDao.getByEventId(eventId)
    suspend fun getByAdvanceReportId(reportId: Long): List<ExpenseEntity> = expenseDao.getByAdvanceReportId(reportId)
    suspend fun getTotalByDateRange(start: Long, end: Long): Double = expenseDao.getTotalByDateRange(start, end) ?: 0.0
    suspend fun getTotalByTypeAndDateRange(type: OperationType, start: Long, end: Long): Double = expenseDao.getTotalByTypeAndDateRange(type, start, end) ?: 0.0
    suspend fun getTotal(): Double = expenseDao.getTotal() ?: 0.0
    suspend fun getTotalByCategory(categoryId: Long): Double = expenseDao.getTotalByCategory(categoryId) ?: 0.0
    suspend fun search(query: String): List<ExpenseEntity> = expenseDao.search(query)
    suspend fun save(entity: ExpenseEntity): Long = expenseDao.insert(entity)
    suspend fun update(entity: ExpenseEntity) = expenseDao.update(entity)
    suspend fun deleteById(id: Long) = expenseDao.deleteById(id)
    suspend fun deleteByReceiptId(receiptId: Long) = expenseDao.deleteByReceiptId(receiptId)
}
