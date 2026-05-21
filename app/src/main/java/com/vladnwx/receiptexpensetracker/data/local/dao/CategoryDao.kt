package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Delete
    suspend fun delete(entity: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder ASC, name ASC")
    suspend fun getParents(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder ASC, name ASC")
    suspend fun getChildren(parentId: Long): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories WHERE isPredefined = 1")
    suspend fun predefinedCount(): Int

    @Query("UPDATE categories SET isFamilyDefault = :value WHERE id = :id")
    suspend fun setFamilyDefault(id: Long, value: Boolean)

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
