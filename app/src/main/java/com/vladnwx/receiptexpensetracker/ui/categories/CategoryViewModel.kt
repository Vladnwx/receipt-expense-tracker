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
