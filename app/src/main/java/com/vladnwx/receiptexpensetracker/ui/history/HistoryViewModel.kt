package com.vladnwx.receiptexpensetracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.dao.TransferDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import com.vladnwx.receiptexpensetracker.data.local.entity.TransferEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryItem(
    val id: Long,
    val type: OperationType,
    val amount: Double,
    val date: Long,
    val description: String?,
    val categoryName: String?,
    val accountName: String?,
    val tags: String?
)

enum class HistoryFilter { ALL, EXPENSES, INCOMES, TRANSFERS }

data class HistoryUiState(
    val query: String = "",
    val filter: HistoryFilter = HistoryFilter.ALL,
    val items: List<HistoryItem> = emptyList(),
    val loading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val transferDao: TransferDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state = _state.asStateFlow()

    init { search() }

    fun setQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        search()
    }

    fun setFilter(filter: HistoryFilter) {
        _state.value = _state.value.copy(filter = filter)
        search()
    }

    fun deleteItem(id: Long, type: OperationType) {
        viewModelScope.launch {
            if (type == OperationType.TRANSFER) {
                transferDao.deleteById(id)
            } else {
                expenseDao.deleteById(id)
            }
            search()
        }
    }

    fun search() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val categories = categoryDao.getAll().associateBy { it.id }
            val accounts = accountDao.getAll().associateBy { it.id }
            val filter = _state.value.filter
            val query = _state.value.query

            val result = mutableListOf<HistoryItem>()

            if (filter != HistoryFilter.TRANSFERS) {
                val expenses = if (query.isBlank()) {
                    if (filter == HistoryFilter.INCOMES) {
                        expenseDao.getByType(OperationType.INCOME)
                    } else {
                        expenseDao.getAll()
                    }
                } else {
                    expenseDao.search(query).filter { e ->
                        when (filter) {
                            HistoryFilter.EXPENSES -> e.type == OperationType.EXPENSE
                            HistoryFilter.INCOMES -> e.type == OperationType.INCOME
                            else -> true
                        }
                    }
                }
                result.addAll(expenses.map { e ->
                    HistoryItem(
                        id = e.id,
                        type = e.type,
                        amount = e.amount,
                        date = e.date,
                        description = e.description,
                        categoryName = e.categoryId?.let { categories[it]?.name },
                        accountName = e.accountId?.let { accounts[it]?.name },
                        tags = e.tags
                    )
                })
            }

            if ((filter == HistoryFilter.TRANSFERS || filter == HistoryFilter.ALL) && query.isBlank()) {
                val transfers = transferDao.getAll()
                result.addAll(transfers.map { t ->
                    HistoryItem(
                        id = t.id,
                        type = OperationType.TRANSFER,
                        amount = t.amount,
                        date = t.date,
                        description = t.description,
                        categoryName = null,
                        accountName = "${accounts[t.fromAccountId]?.name ?: "?"} → ${accounts[t.toAccountId]?.name ?: "?"}",
                        tags = null
                    )
                })
            }

            _state.value = _state.value.copy(
                items = result.sortedByDescending { it.date },
                loading = false
            )
        }
    }
}
