package com.vladnwx.receiptexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountDefaultCategoryEntity

@Dao
interface AccountDefaultCategoryDao {
    @Insert
    suspend fun insert(entity: AccountDefaultCategoryEntity): Long

    @Insert
    suspend fun insertAll(entities: List<AccountDefaultCategoryEntity>)

    @Query("DELETE FROM account_default_categories WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Long)

    @Query("SELECT categoryId FROM account_default_categories WHERE accountId = :accountId")
    suspend fun getCategoryIdsByAccountId(accountId: Long): List<Long>
}
