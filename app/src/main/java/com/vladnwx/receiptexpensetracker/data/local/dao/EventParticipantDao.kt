package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.EventParticipantEntity

@Dao
interface EventParticipantDao {
    @Insert
    suspend fun insert(entity: EventParticipantEntity): Long

    @Insert
    suspend fun insertAll(entities: List<EventParticipantEntity>)

    @Query("SELECT * FROM event_participants WHERE eventId = :eventId")
    suspend fun getByEventId(eventId: Long): List<EventParticipantEntity>

    @Query("DELETE FROM event_participants WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: Long)
}
