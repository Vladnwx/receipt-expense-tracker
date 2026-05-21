package com.vladnwx.receiptexpensetracker.data.sync

import com.google.gson.Gson
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.AdvanceReportDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ContactDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CurrencyDao
import com.vladnwx.receiptexpensetracker.data.local.dao.DebtDao
import com.vladnwx.receiptexpensetracker.data.local.dao.DebtPaymentDao
import com.vladnwx.receiptexpensetracker.data.local.dao.EventDao
import com.vladnwx.receiptexpensetracker.data.local.dao.EventParticipantDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.dao.TransferDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceReportEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CurrencyEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtPaymentEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.EventEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.EventParticipantEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.TransferEntity
import javax.inject.Inject
import javax.inject.Singleton

data class SyncBundle(
    val expenses: List<ExpenseEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val contacts: List<ContactEntity> = emptyList(),
    val transfers: List<TransferEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val debtPayments: List<DebtPaymentEntity> = emptyList(),
    val currencies: List<CurrencyEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val eventParticipants: List<EventParticipantEntity> = emptyList(),
    val advanceReports: List<AdvanceReportEntity> = emptyList(),
    val syncedAt: Long = System.currentTimeMillis()
)

@Singleton
class SyncManager @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val contactDao: ContactDao,
    private val transferDao: TransferDao,
    private val debtDao: DebtDao,
    private val debtPaymentDao: DebtPaymentDao,
    private val currencyDao: CurrencyDao,
    private val eventDao: EventDao,
    private val eventParticipantDao: EventParticipantDao,
    private val advanceReportDao: AdvanceReportDao,
    private val gson: Gson
) {
    suspend fun exportAll(): String {
        val bundle = SyncBundle(
            expenses = expenseDao.getAll(),
            accounts = accountDao.getAll(),
            categories = categoryDao.getAll(),
            contacts = contactDao.getAll(),
            transfers = transferDao.getAll(),
            debts = debtDao.getAll(),
            debtPayments = debtPaymentDao.getAll(),
            currencies = currencyDao.getAll(),
            events = eventDao.getAll(),
            eventParticipants = eventParticipantDao.getAll(),
            advanceReports = advanceReportDao.getAll()
        )
        return gson.toJson(bundle)
    }

    suspend fun importAll(json: String): Result<Int> = runCatching {
        val bundle = gson.fromJson(json, SyncBundle::class.java)

        var count = 0

        expenseDao.deleteAll()
        bundle.expenses.forEach { expenseDao.insert(it); count++ }

        accountDao.deleteAll()
        bundle.accounts.forEach { accountDao.insert(it); count++ }

        categoryDao.deleteAll()
        bundle.categories.forEach { categoryDao.insert(it); count++ }

        contactDao.deleteAll()
        bundle.contacts.forEach { contactDao.insert(it); count++ }

        transferDao.deleteAll()
        bundle.transfers.forEach { transferDao.insert(it); count++ }

        debtDao.deleteAll()
        bundle.debts.forEach { debtDao.insert(it); count++ }

        debtPaymentDao.deleteAll()
        bundle.debtPayments.forEach { debtPaymentDao.insert(it); count++ }

        currencyDao.deleteAll()
        bundle.currencies.forEach { currencyDao.insert(it); count++ }

        eventDao.deleteAll()
        bundle.events.forEach { eventDao.insert(it); count++ }

        eventParticipantDao.deleteAll()
        bundle.eventParticipants.forEach { eventParticipantDao.insert(it); count++ }

        advanceReportDao.deleteAll()
        bundle.advanceReports.forEach { advanceReportDao.insert(it); count++ }

        count
    }

    suspend fun getLastSyncTime(): Long {
        return try {
            gson.fromJson(exportAll(), SyncBundle::class.java).syncedAt
        } catch (_: Exception) {
            0L
        }
    }
}
