package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Insert
    suspend fun insert(entity: EventEntity): Long

    @Query("SELECT * FROM events ORDER BY date DESC")
    suspend fun getAll(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
