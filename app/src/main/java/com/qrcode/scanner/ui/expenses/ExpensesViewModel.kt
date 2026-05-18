package com.qrcode.scanner.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.ExpenseEntity
import com.qrcode.scanner.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _expenses = MutableLiveData<List<ExpenseEntity>>(emptyList())
    val expenses: LiveData<List<ExpenseEntity>> = _expenses

    private val _total = MutableLiveData(0.0)
    val total: LiveData<Double> = _total

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _expenses.value = expenseRepository.getAll()
        }
    }

    fun loadByPeriod(start: Long, end: Long) {
        viewModelScope.launch {
            _expenses.value = expenseRepository.getByDateRange(start, end)
            _total.value = expenseRepository.getTotalByDateRange(start, end)
        }
    }
}
