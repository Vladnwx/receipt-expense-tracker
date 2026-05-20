package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDefaultCategoryDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountDefaultCategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val defaultCategoryDao: AccountDefaultCategoryDao
) {
    suspend fun getAll(): List<AccountEntity> = accountDao.getAll()

    suspend fun getById(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun getBudgetAccounts(): List<AccountEntity> = accountDao.getBudgetAccounts()

    suspend fun save(entity: AccountEntity): Long = accountDao.insert(entity)

    suspend fun update(entity: AccountEntity) = accountDao.update(entity)

    suspend fun delete(entity: AccountEntity) = accountDao.delete(entity)

    suspend fun getDefaultCategoryIds(accountId: Long): List<Long> =
        defaultCategoryDao.getCategoryIdsByAccountId(accountId)

    suspend fun setDefaultCategories(accountId: Long, categoryIds: List<Long>) {
        defaultCategoryDao.deleteByAccountId(accountId)
        if (categoryIds.isNotEmpty()) {
            defaultCategoryDao.insertAll(
                categoryIds.map { categoryId ->
                    AccountDefaultCategoryEntity(
                        accountId = accountId,
                        categoryId = categoryId
                    )
                }
            )
        }
    }
}
