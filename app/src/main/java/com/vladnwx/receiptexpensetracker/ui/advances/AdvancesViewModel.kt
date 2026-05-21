package com.vladnwx.receiptexpensetracker.ui.advances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.AdvanceReportDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ContactDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceReportEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceStatus
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceType
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportWithContact(
    val report: AdvanceReportEntity,
    val contact: ContactEntity?,
    val account: AccountEntity?,
    val spent: Double = 0.0
)

data class AdvancesUiState(
    val reports: List<ReportWithContact> = emptyList(),
    val contacts: List<ContactEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val showCreateDialog: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdvancesViewModel @Inject constructor(
    private val advanceReportDao: AdvanceReportDao,
    private val contactDao: ContactDao,
    private val accountDao: AccountDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _state = MutableStateFlow(AdvancesUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val contacts = contactDao.getAll()
            val accounts = accountDao.getAll()
            val reports = advanceReportDao.getAll()

            val withContacts = reports.map { r ->
                val expenses = expenseDao.getByAdvanceReportId(r.id)
                val spent = expenses.sumOf { it.amount }
                ReportWithContact(
                    report = r,
                    contact = contacts.find { it.id == r.contactId },
                    account = accounts.find { it.id == r.accountId },
                    spent = spent
                )
            }

            _state.value = _state.value.copy(
                reports = withContacts,
                contacts = contacts,
                accounts = accounts,
                loading = false
            )
        }
    }

    fun showCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = false)
    }

    fun createReport(
        contactId: Long,
        accountId: Long,
        amount: Double,
        type: AdvanceType,
        purpose: String,
        dueDate: Long?
    ) {
        viewModelScope.launch {
            try {
                advanceReportDao.insert(AdvanceReportEntity(
                    contactId = contactId,
                    accountId = accountId,
                    amount = amount,
                    type = type,
                    purpose = purpose,
                    dueDate = dueDate
                ))
                _state.value = _state.value.copy(showCreateDialog = false)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun closeReport(id: Long) {
        viewModelScope.launch {
            advanceReportDao.close(id)
            load()
        }
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            advanceReportDao.deleteById(id)
            load()
        }
    }
}
