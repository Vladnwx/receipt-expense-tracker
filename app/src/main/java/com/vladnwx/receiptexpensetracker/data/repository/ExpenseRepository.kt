package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun getAll(): List<ExpenseEntity> = expenseDao.getAll()
    suspend fun getByReceiptId(receiptId: Long): List<ExpenseEntity> =
        expenseDao.getByReceiptId(receiptId)
    suspend fun getByCategoryId(categoryId: Long): List<ExpenseEntity> =
        expenseDao.getByCategoryId(categoryId)
    suspend fun getByDateRange(start: Long, end: Long): List<ExpenseEntity> =
        expenseDao.getByDateRange(start, end)
    suspend fun getTotalByDateRange(start: Long, end: Long): Double =
        expenseDao.getTotalByDateRange(start, end) ?: 0.0
    suspend fun getTotal(): Double = expenseDao.getTotal() ?: 0.0
    suspend fun getTotalByCategory(categoryId: Long): Double =
        expenseDao.getTotalByCategory(categoryId) ?: 0.0
    suspend fun save(entity: ExpenseEntity): Long = expenseDao.insert(entity)

    suspend fun deleteByReceiptId(receiptId: Long) {
        expenseDao.deleteByReceiptId(receiptId)
    }

    suspend fun createFromReceiptItems(
        receiptId: Long,
        items: List<com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptItemEntity>,
        accountId: Long? = null
    ) {
        items.forEach { item ->
            expenseDao.insert(
                ExpenseEntity(
                    receiptId = receiptId,
                    receiptItemId = item.id,
                    categoryId = item.categoryId,
                    accountId = accountId,
                    amount = item.amount,
                    description = "[чек] ${item.name}",
                    date = System.currentTimeMillis()
                )
            )
        }
    }
}
