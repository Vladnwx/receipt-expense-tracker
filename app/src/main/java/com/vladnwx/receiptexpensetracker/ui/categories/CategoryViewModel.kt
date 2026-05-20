package com.vladnwx.receiptexpensetracker.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val parents: List<CategoryEntity> = emptyList(),
    val children: Map<Long, List<CategoryEntity>> = emptyMap(),
    val editingCategory: CategoryEntity? = null,
    val showDialog: Boolean = false,
    val loading: Boolean = true
)

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

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryUiState())
    val state = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)

            if (repository.predefinedCount() == 0) {
                repository.insertAll(defaultCategories)
            }

            val parents = repository.getParents()
            val children = mutableMapOf<Long, List<CategoryEntity>>()
            parents.forEach { parent ->
                children[parent.id] = repository.getChildren(parent.id)
            }
            _state.value = _state.value.copy(parents = parents, children = children, loading = false)
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showDialog = true, editingCategory = null)
    }

    fun showEditDialog(category: CategoryEntity) {
        _state.value = _state.value.copy(showDialog = true, editingCategory = category)
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(showDialog = false, editingCategory = null)
    }

    fun save(name: String, parentId: Long?, color: Int, isFamily: Boolean) {
        viewModelScope.launch {
            val editing = _state.value.editingCategory
            if (editing != null) {
                repository.save(editing.copy(
                    name = name, parentId = parentId, color = color, isFamilyDefault = isFamily
                ))
            } else {
                repository.save(CategoryEntity(
                    name = name, parentId = parentId, color = color,
                    isFamilyDefault = isFamily, isPredefined = false
                ))
            }
            dismissDialog()
            loadCategories()
        }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch {
            repository.delete(category)
            loadCategories()
        }
    }

    fun getParentOptions(): List<CategoryEntity> {
        return _state.value.parents
    }
}
