package com.qrcode.scanner.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.local.entity.ExpenseEntity
import com.qrcode.scanner.data.repository.CategoryRepository
import com.qrcode.scanner.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class DateFilter {
    TODAY, WEEK, MONTH, ALL
}

data class ExpensesUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val total: Double = 0.0,
    val categories: List<CategoryEntity> = emptyList(),
    val dateFilter: DateFilter = DateFilter.ALL,
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val categories = categoryRepository.getAll()
            _uiState.value = _uiState.value.copy(categories = categories)
            loadWithFilters()
        }
    }

    fun setDateFilter(filter: DateFilter) {
        _uiState.value = _uiState.value.copy(dateFilter = filter)
        viewModelScope.launch { loadWithFilters() }
    }

    fun setCategoryFilter(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        viewModelScope.launch { loadWithFilters() }
    }

    private suspend fun loadWithFilters() {
        val state = _uiState.value
        val range = dateFilterToRange(state.dateFilter)
        val expenses = if (state.selectedCategoryId != null) {
            expenseRepository.getByCategoryId(state.selectedCategoryId)
                .filter { it.date in range.first..range.second }
        } else {
            expenseRepository.getByDateRange(range.first, range.second)
        }
        val total = expenses.sumOf { it.amount }
        _uiState.value = _uiState.value.copy(
            expenses = expenses,
            total = total,
            isLoading = false
        )
    }

    private fun dateFilterToRange(filter: DateFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        when (filter) {
            DateFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                return start to cal.timeInMillis
            }
            DateFilter.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                return start to Long.MAX_VALUE
            }
            DateFilter.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                return start to Long.MAX_VALUE
            }
            DateFilter.ALL -> return 0L to Long.MAX_VALUE
        }
    }
}
