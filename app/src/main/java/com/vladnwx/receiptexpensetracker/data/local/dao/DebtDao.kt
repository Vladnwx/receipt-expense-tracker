package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtEntity

@Dao
interface DebtDao {
    @Insert
    suspend fun insert(entity: DebtEntity): Long

    @Update
    suspend fun update(entity: DebtEntity)

    @Query("SELECT * FROM debts ORDER BY date DESC")
    suspend fun getAll(): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE status = 'ACTIVE' ORDER BY date DESC")
    suspend fun getActive(): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE contactId = :contactId ORDER BY date DESC")
    suspend fun getByContactId(contactId: Long): List<DebtEntity>

    @Query("UPDATE debts SET status = 'CLOSED' WHERE id = :id")
    suspend fun close(id: Long)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM debts")
    suspend fun deleteAll()
}
