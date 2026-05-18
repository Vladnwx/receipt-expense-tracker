package com.qrcode.scanner.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryEntity>>(emptyList())
    val categories: LiveData<List<CategoryEntity>> = _categories

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _categories.value = categoryRepository.getAll()
        }
    }

    fun add(name: String) {
        viewModelScope.launch {
            categoryRepository.save(CategoryEntity(name = name))
            load()
        }
    }

    fun delete(entity: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.delete(entity)
            load()
        }
    }
}
