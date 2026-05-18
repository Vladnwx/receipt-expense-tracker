package com.qrcode.scanner.data.repository

import com.qrcode.scanner.data.local.dao.CategoryDao
import com.qrcode.scanner.data.local.entity.CategoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun getAll(): List<CategoryEntity> = categoryDao.getAll()
    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)
    suspend fun save(entity: CategoryEntity): Long = categoryDao.insert(entity)
    suspend fun delete(entity: CategoryEntity) = categoryDao.delete(entity)
}
