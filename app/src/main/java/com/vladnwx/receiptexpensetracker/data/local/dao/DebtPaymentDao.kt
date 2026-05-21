package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtPaymentEntity

@Dao
interface DebtPaymentDao {
    @Insert
    suspend fun insert(entity: DebtPaymentEntity): Long

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY date DESC")
    suspend fun getByDebtId(debtId: Long): List<DebtPaymentEntity>

    @Query("SELECT * FROM debt_payments ORDER BY date DESC")
    suspend fun getAll(): List<DebtPaymentEntity>

    @Query("SELECT SUM(amount) FROM debt_payments WHERE debtId = :debtId")
    suspend fun getTotalPaid(debtId: Long): Double?

    @Query("DELETE FROM debt_payments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM debt_payments")
    suspend fun deleteAll()
}
