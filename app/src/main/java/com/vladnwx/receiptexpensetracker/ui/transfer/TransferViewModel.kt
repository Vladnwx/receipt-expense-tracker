package com.vladnwx.receiptexpensetracker.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.TransferEntity
import com.vladnwx.receiptexpensetracker.data.repository.AccountRepository
import com.vladnwx.receiptexpensetracker.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferFormState(
    val fromAccount: AccountEntity? = null,
    val toAccount: AccountEntity? = null,
    val amountText: String = "",
    val commissionText: String = "0",
    val rateText: String = "1.0",
    val dateMillis: Long = System.currentTimeMillis(),
    val description: String = "",
    val accounts: List<AccountEntity> = emptyList(),
    val saved: Boolean = false
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransferFormState())
    val state = _state.asStateFlow()

    init { loadAccounts() }

    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.getAll()
            _state.value = _state.value.copy(
                accounts = accounts,
                fromAccount = accounts.getOrNull(0),
                toAccount = accounts.getOrNull(1)
            )
        }
    }

    fun onFromAccountSelected(acc: AccountEntity) { _state.value = _state.value.copy(fromAccount = acc) }
    fun onToAccountSelected(acc: AccountEntity) { _state.value = _state.value.copy(toAccount = acc) }
    fun onAmountChanged(v: String) { _state.value = _state.value.copy(amountText = v) }
    fun onCommissionChanged(v: String) { _state.value = _state.value.copy(commissionText = v) }
    fun onRateChanged(v: String) { _state.value = _state.value.copy(rateText = v) }
    fun onDateChanged(v: Long) { _state.value = _state.value.copy(dateMillis = v) }
    fun onDescriptionChanged(v: String) { _state.value = _state.value.copy(description = v) }

    fun save() {
        val s = _state.value
        val from = s.fromAccount ?: return
        val to = s.toAccount ?: return
        val amount = s.amountText.replace(",", ".").toDoubleOrNull() ?: return
        val commission = s.commissionText.replace(",", ".").toDoubleOrNull() ?: 0.0
        val rate = s.rateText.replace(",", ".").toDoubleOrNull() ?: 1.0

        viewModelScope.launch {
            transferRepository.save(TransferEntity(
                fromAccountId = from.id,
                toAccountId = to.id,
                amount = amount,
                fromCurrency = from.currency,
                toCurrency = to.currency,
                rate = rate,
                commission = commission,
                date = s.dateMillis,
                description = s.description.ifBlank { null }
            ))
            _state.value = s.copy(saved = true)
        }
    }

    fun reset() {
        val accounts = _state.value.accounts
        _state.value = TransferFormState(accounts = accounts)
    }
}
