package com.qrcode.scanner.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesTabUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

@HiltViewModel
class CategoriesTabViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesTabUiState())
    val uiState: StateFlow<CategoriesTabUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                categories = categoryRepository.getAll()
            )
        }
    }

    fun showAddCategoryDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun dismissAddCategoryDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.save(CategoryEntity(name = name))
            _uiState.value = _uiState.value.copy(showAddDialog = false)
            loadCategories()
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.delete(category)
            loadCategories()
        }
    }
}
