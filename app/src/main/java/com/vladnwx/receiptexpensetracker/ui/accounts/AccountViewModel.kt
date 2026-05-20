package com.vladnwx.receiptexpensetracker.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountType
import com.vladnwx.receiptexpensetracker.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val editingAccount: AccountEntity? = null,
    val showDialog: Boolean = false,
    val loading: Boolean = true
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state = _state.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val accounts = repository.getAll()
            _state.value = _state.value.copy(accounts = accounts, loading = false)
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showDialog = true, editingAccount = null)
    }

    fun showEditDialog(account: AccountEntity) {
        _state.value = _state.value.copy(showDialog = true, editingAccount = account)
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(showDialog = false, editingAccount = null)
    }

    fun save(name: String, type: AccountType, currency: String, initialBalance: Double,
             color: Int, sortOrder: Int, includeInBudget: Boolean) {
        viewModelScope.launch {
            val editing = _state.value.editingAccount
            if (editing != null) {
                repository.update(editing.copy(
                    name = name, type = type, currency = currency,
                    initialBalance = initialBalance, color = color,
                    sortOrder = sortOrder, includeInBudget = includeInBudget
                ))
            } else {
                repository.save(AccountEntity(
                    name = name, type = type, currency = currency,
                    initialBalance = initialBalance, color = color,
                    sortOrder = sortOrder, includeInBudget = includeInBudget
                ))
            }
            dismissDialog()
            loadAccounts()
        }
    }

    fun delete(account: AccountEntity) {
        viewModelScope.launch {
            repository.delete(account)
            loadAccounts()
        }
    }
}
