package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import javax.inject.Inject
import javax.inject.Singleton

private val defaultCategories = listOf(
    CategoryEntity(name = "Продукты", color = 0xFF43A047.toInt(), isPredefined = true, isFamilyDefault = true, sortOrder = 1),
    CategoryEntity(name = "Мясо", parentId = 1, color = 0xFF66BB6A.toInt(), isPredefined = true, sortOrder = 1),
    CategoryEntity(name = "Овощи", parentId = 1, color = 0xFF66BB6A.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Бакалея", parentId = 1, color = 0xFF66BB6A.toInt(), isPredefined = true, sortOrder = 3),
    CategoryEntity(name = "Молочные", parentId = 1, color = 0xFF66BB6A.toInt(), isPredefined = true, sortOrder = 4),
    CategoryEntity(name = "Напитки", parentId = 1, color = 0xFF66BB6A.toInt(), isPredefined = true, sortOrder = 5),
    CategoryEntity(name = "Транспорт", color = 0xFF42A5F5.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Бензин", parentId = 2, color = 0xFF64B5F6.toInt(), isPredefined = true, sortOrder = 1),
    CategoryEntity(name = "Такси", parentId = 2, color = 0xFF64B5F6.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Общественный транспорт", parentId = 2, color = 0xFF64B5F6.toInt(), isPredefined = true, sortOrder = 3),
    CategoryEntity(name = "Кафе", color = 0xFFEF5350.toInt(), isPredefined = true, sortOrder = 3),
    CategoryEntity(name = "Жильё", color = 0xFFAB47BC.toInt(), isPredefined = true, isFamilyDefault = true, sortOrder = 4),
    CategoryEntity(name = "Коммунальные", parentId = 4, color = 0xFFCE93D8.toInt(), isPredefined = true, sortOrder = 1),
    CategoryEntity(name = "Аренда", parentId = 4, color = 0xFFCE93D8.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Ремонт", parentId = 4, color = 0xFFCE93D8.toInt(), isPredefined = true, sortOrder = 3),
    CategoryEntity(name = "Связь", color = 0xFF26A69A.toInt(), isPredefined = true, sortOrder = 5),
    CategoryEntity(name = "Интернет", parentId = 5, color = 0xFF4DB6AC.toInt(), isPredefined = true, sortOrder = 1),
    CategoryEntity(name = "Телефон", parentId = 5, color = 0xFF4DB6AC.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Развлечения", color = 0xFFFF7043.toInt(), isPredefined = true, sortOrder = 6),
    CategoryEntity(name = "Кино", parentId = 6, color = 0xFFFF8A65.toInt(), isPredefined = true, sortOrder = 1),
    CategoryEntity(name = "Игры", parentId = 6, color = 0xFFFF8A65.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Спорт", parentId = 6, color = 0xFFFF8A65.toInt(), isPredefined = true, sortOrder = 3),
    CategoryEntity(name = "Здоровье", color = 0xFFEF5350.toInt(), isPredefined = true, sortOrder = 7),
    CategoryEntity(name = "Аптеки", parentId = 7, color = 0xFFE57373.toInt(), isPredefined = true, sortOrder = 1),
    CategoryEntity(name = "Врачи", parentId = 7, color = 0xFFE57373.toInt(), isPredefined = true, sortOrder = 2),
    CategoryEntity(name = "Одежда", color = 0xFF8D6E63.toInt(), isPredefined = true, sortOrder = 8),
    CategoryEntity(name = "Подарки", color = 0xFFE91E63.toInt(), isPredefined = true, sortOrder = 9),
)

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun getAll(): List<CategoryEntity> {
        seedDefaults()
        return categoryDao.getAll()
    }
    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)
    suspend fun getParents(): List<CategoryEntity> = categoryDao.getParents()
    suspend fun getChildren(parentId: Long): List<CategoryEntity> = categoryDao.getChildren(parentId)
    suspend fun save(entity: CategoryEntity): Long = categoryDao.insert(entity)
    suspend fun delete(entity: CategoryEntity) = categoryDao.delete(entity)
    suspend fun setFamilyDefault(id: Long, value: Boolean) = categoryDao.setFamilyDefault(id, value)

    private suspend fun seedDefaults() {
        if (categoryDao.predefinedCount() == 0) {
            categoryDao.insertAll(defaultCategories)
        }
    }
}
