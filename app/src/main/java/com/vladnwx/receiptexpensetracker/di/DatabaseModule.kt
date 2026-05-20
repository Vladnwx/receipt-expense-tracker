package com.vladnwx.receiptexpensetracker.di

import android.content.Context
import androidx.room.Room
import com.vladnwx.receiptexpensetracker.data.local.AppDatabase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "receipt_expense_tracker.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideReceiptRawDao(db: AppDatabase): ReceiptRawDao = db.receiptRawDao()
    @Provides fun provideReceiptDao(db: AppDatabase): ReceiptDao = db.receiptDao()
    @Provides fun provideReceiptItemDao(db: AppDatabase): ReceiptItemDao = db.receiptItemDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideAccountDefaultCategoryDao(db: AppDatabase): AccountDefaultCategoryDao = db.accountDefaultCategoryDao()
    @Provides fun provideReceiptAttachmentDao(db: AppDatabase): ReceiptAttachmentDao = db.receiptAttachmentDao()
    @Provides fun provideTransferDao(db: AppDatabase): TransferDao = db.transferDao()
    @Provides fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()
    @Provides fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()
    @Provides fun provideDebtPaymentDao(db: AppDatabase): DebtPaymentDao = db.debtPaymentDao()
    @Provides fun provideCurrencyDao(db: AppDatabase): CurrencyDao = db.currencyDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
    @Provides fun provideEventParticipantDao(db: AppDatabase): EventParticipantDao = db.eventParticipantDao()
    @Provides fun provideAdvanceReportDao(db: AppDatabase): AdvanceReportDao = db.advanceReportDao()
}
