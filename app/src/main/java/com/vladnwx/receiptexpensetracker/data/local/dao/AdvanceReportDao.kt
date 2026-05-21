package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceReportEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceStatus

@Dao
interface AdvanceReportDao {
    @Insert
    suspend fun insert(entity: AdvanceReportEntity): Long

    @Update
    suspend fun update(entity: AdvanceReportEntity)

    @Query("SELECT * FROM advance_reports ORDER BY date DESC")
    suspend fun getAll(): List<AdvanceReportEntity>

    @Query("SELECT * FROM advance_reports WHERE id = :id")
    suspend fun getById(id: Long): AdvanceReportEntity?

    @Query("SELECT * FROM advance_reports WHERE status = 'OPEN' ORDER BY date DESC")
    suspend fun getOpen(): List<AdvanceReportEntity>

    @Query("UPDATE advance_reports SET status = 'CLOSED', closedAt = :closedAt WHERE id = :id")
    suspend fun close(id: Long, closedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM advance_reports WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM advance_reports")
    suspend fun deleteAll()
}
