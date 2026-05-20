package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun getAll(): List<CategoryEntity> = categoryDao.getAll()
    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)
    suspend fun getParents(): List<CategoryEntity> = categoryDao.getParents()
    suspend fun getChildren(parentId: Long): List<CategoryEntity> = categoryDao.getChildren(parentId)
    suspend fun save(entity: CategoryEntity): Long = categoryDao.insert(entity)
    suspend fun delete(entity: CategoryEntity) = categoryDao.delete(entity)
    suspend fun predefinedCount(): Int = categoryDao.predefinedCount()
    suspend fun insertAll(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)
    suspend fun setFamilyDefault(id: Long, value: Boolean) = categoryDao.setFamilyDefault(id, value)
}
