package com.vladnwx.receiptexpensetracker.data.sync

import com.google.gson.Gson
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ContactDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import javax.inject.Inject
import javax.inject.Singleton

data class SyncBundle(
    val expenses: List<ExpenseEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val contacts: List<ContactEntity> = emptyList(),
    val syncedAt: Long = System.currentTimeMillis()
)

enum class SyncConflict {
    LOCAL_NEWER, REMOTE_NEWER, SAME, CONFLICT
}

@Singleton
class SyncManager @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val contactDao: ContactDao,
    private val gson: Gson
) {
    suspend fun exportAll(): String {
        val bundle = SyncBundle(
            expenses = expenseDao.getAll(),
            accounts = accountDao.getAll(),
            categories = categoryDao.getAll(),
            contacts = contactDao.getAll()
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
