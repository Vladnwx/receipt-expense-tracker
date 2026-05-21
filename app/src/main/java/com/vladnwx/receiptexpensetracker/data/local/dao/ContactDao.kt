package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(entity: ContactEntity): Long

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun search(query: String): List<ContactEntity>

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}
