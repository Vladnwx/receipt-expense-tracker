package com.vladnwx.receiptexpensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDefaultCategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptAttachmentDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptItemDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptRawDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountDefaultCategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptAttachmentEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptItemEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptRawEntity

@Database(
    entities = [
        ReceiptRawEntity::class,
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        AccountEntity::class,
        AccountDefaultCategoryEntity::class,
        ReceiptAttachmentEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptRawDao(): ReceiptRawDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun receiptItemDao(): ReceiptItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountDao(): AccountDao
    abstract fun accountDefaultCategoryDao(): AccountDefaultCategoryDao
    abstract fun receiptAttachmentDao(): ReceiptAttachmentDao
}
