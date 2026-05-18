package com.qrcode.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.qrcode.scanner.data.local.entity.FnsSessionEntity

@Dao
interface FnsSessionDao {
    @Insert
    suspend fun insert(entity: FnsSessionEntity): Long

    @Update
    suspend fun update(entity: FnsSessionEntity)

    @Query("SELECT * FROM fns_sessions WHERE isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveSession(): FnsSessionEntity?

    @Query("SELECT * FROM fns_sessions ORDER BY createdAt DESC")
    suspend fun getAll(): List<FnsSessionEntity>

    @Query("UPDATE fns_sessions SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE fns_sessions SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()
}
