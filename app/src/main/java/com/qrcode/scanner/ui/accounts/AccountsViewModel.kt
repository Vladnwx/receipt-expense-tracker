package com.qrcode.scanner.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.data.local.entity.AccountEntity
import com.qrcode.scanner.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingAccount: AccountEntity? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(accounts = accountRepository.getAll())
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingAccount = null)
    }

    fun showEditDialog(account: AccountEntity) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingAccount = account)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingAccount = null)
    }

    fun saveAccount(name: String, type: String, balance: String, currency: String) {
        val balanceValue = balance.replace(",", ".").toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            val existing = _uiState.value.editingAccount
            if (existing != null) {
                accountRepository.update(
                    existing.copy(
                        name = name,
                        type = type,
                        initialBalance = balanceValue,
                        currency = currency
                    )
                )
            } else {
                accountRepository.save(
                    AccountEntity(
                        name = name,
                        type = type,
                        initialBalance = balanceValue,
                        currency = currency
                    )
                )
            }
            _uiState.value = _uiState.value.copy(showAddDialog = false, editingAccount = null)
            load()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.delete(account)
            load()
        }
    }
}
