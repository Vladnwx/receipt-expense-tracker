package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.DebtDao
import com.vladnwx.receiptexpensetracker.data.local.dao.DebtPaymentDao
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtPaymentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao,
    private val paymentDao: DebtPaymentDao
) {
    suspend fun getAll(): List<DebtEntity> = debtDao.getAll()
    suspend fun getActive(): List<DebtEntity> = debtDao.getActive()
    suspend fun getById(id: Long): DebtEntity? = debtDao.getById(id)
    suspend fun save(entity: DebtEntity): Long = debtDao.insert(entity)
    suspend fun update(entity: DebtEntity) = debtDao.update(entity)
    suspend fun close(id: Long) = debtDao.close(id)
    suspend fun deleteById(id: Long) = debtDao.deleteById(id)

    suspend fun getPayments(debtId: Long): List<DebtPaymentEntity> = paymentDao.getByDebtId(debtId)
    suspend fun getTotalPaid(debtId: Long): Double = paymentDao.getTotalPaid(debtId) ?: 0.0
    suspend fun addPayment(entity: DebtPaymentEntity): Long = paymentDao.insert(entity)
}
