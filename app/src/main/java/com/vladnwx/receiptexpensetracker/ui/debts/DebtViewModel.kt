package com.vladnwx.receiptexpensetracker.ui.debts

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtPaymentEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtStatus
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtType
import com.vladnwx.receiptexpensetracker.data.repository.ContactRepository
import com.vladnwx.receiptexpensetracker.data.repository.DebtRepository
import com.vladnwx.receiptexpensetracker.data.util.CalendarHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebtUiState(
    val activeDebts: List<DebtEntity> = emptyList(),
    val contacts: List<ContactEntity> = emptyList(),
    val showCreateDialog: Boolean = false,
    val createType: DebtType = DebtType.LEND,
    val loading: Boolean = true
)

@HiltViewModel
class DebtViewModel @Inject constructor(
    application: Application,
    private val debtRepository: DebtRepository,
    private val contactRepository: ContactRepository
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    private val _state = MutableStateFlow(DebtUiState())
    val state = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                activeDebts = debtRepository.getActive(),
                contacts = contactRepository.getAll(),
                loading = false
            )
        }
    }

    fun showCreateDialog(type: DebtType) {
        _state.value = _state.value.copy(showCreateDialog = true, createType = type)
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(showCreateDialog = false)
    }

    fun createDebt(contactId: Long, amount: Double, dueDate: Long?, description: String?) {
        viewModelScope.launch {
            debtRepository.save(DebtEntity(
                contactId = contactId,
                amount = amount,
                type = _state.value.createType,
                dueDate = dueDate,
                description = description
            ))

            if (dueDate != null && CalendarHelper.hasCalendarPermission(context)) {
                val contactName = contactRepository.getById(contactId)?.name ?: "Долг"
                val title = if (_state.value.createType == DebtType.LEND)
                    "Долг мне: $contactName" else "Мой долг: $contactName"
                CalendarHelper.addEvent(
                    context = context,
                    title = title,
                    description = description ?: "Сумма: ${String.format("%.2f", amount)} ₽",
                    eventDate = dueDate,
                    reminderMinutes = 1440 // 1 day before
                )
            }

            dismissDialog()
            load()
        }
    }

    fun closeDebt(debtId: Long) {
        viewModelScope.launch {
            debtRepository.close(debtId)
            load()
        }
    }
}
