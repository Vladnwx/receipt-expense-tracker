package com.qrcode.scanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qrcode.scanner.data.local.dao.AccountDao
import com.qrcode.scanner.data.local.dao.AccountDefaultCategoryDao
import com.qrcode.scanner.data.local.dao.CategoryDao
import com.qrcode.scanner.data.local.dao.ExpenseDao
import com.qrcode.scanner.data.local.dao.ReceiptAttachmentDao
import com.qrcode.scanner.data.local.dao.ReceiptDao
import com.qrcode.scanner.data.local.dao.ReceiptItemDao
import com.qrcode.scanner.data.local.dao.ReceiptRawDao
import com.qrcode.scanner.data.local.entity.AccountDefaultCategoryEntity
import com.qrcode.scanner.data.local.entity.AccountEntity
import com.qrcode.scanner.data.local.entity.CategoryEntity
import com.qrcode.scanner.data.local.entity.ExpenseEntity
import com.qrcode.scanner.data.local.entity.ReceiptAttachmentEntity
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity
import com.qrcode.scanner.data.local.entity.ReceiptRawEntity

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
    version = 4,
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
