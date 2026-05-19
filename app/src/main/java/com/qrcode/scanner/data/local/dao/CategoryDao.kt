package com.qrcode.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.qrcode.scanner.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Delete
    suspend fun delete(entity: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE isPredefined = 1")
    suspend fun predefinedCount(): Int

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)
}
