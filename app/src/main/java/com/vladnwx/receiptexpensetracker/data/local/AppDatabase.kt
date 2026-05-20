package com.vladnwx.receiptexpensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDao
import com.vladnwx.receiptexpensetracker.data.local.dao.AccountDefaultCategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.AdvanceReportDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ContactDao
import com.vladnwx.receiptexpensetracker.data.local.dao.CurrencyDao
import com.vladnwx.receiptexpensetracker.data.local.dao.DebtDao
import com.vladnwx.receiptexpensetracker.data.local.dao.DebtPaymentDao
import com.vladnwx.receiptexpensetracker.data.local.dao.EventDao
import com.vladnwx.receiptexpensetracker.data.local.dao.EventParticipantDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptAttachmentDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptItemDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ReceiptRawDao
import com.vladnwx.receiptexpensetracker.data.local.dao.TransferDao
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountDefaultCategoryEntity
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
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptAttachmentEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptItemEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ReceiptRawEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.TransferEntity

@Database(
    entities = [
        ReceiptRawEntity::class,
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        AccountEntity::class,
        AccountDefaultCategoryEntity::class,
        ReceiptAttachmentEntity::class,
        TransferEntity::class,
        ContactEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        CurrencyEntity::class,
        EventEntity::class,
        EventParticipantEntity::class,
        AdvanceReportEntity::class
    ],
    version = 6,
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
    abstract fun transferDao(): TransferDao
    abstract fun contactDao(): ContactDao
    abstract fun debtDao(): DebtDao
    abstract fun debtPaymentDao(): DebtPaymentDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun eventDao(): EventDao
    abstract fun eventParticipantDao(): EventParticipantDao
    abstract fun advanceReportDao(): AdvanceReportDao
}
