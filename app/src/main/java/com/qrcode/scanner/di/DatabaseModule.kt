package com.qrcode.scanner.di

import android.content.Context
import androidx.room.Room
import com.qrcode.scanner.data.local.AppDatabase
import com.qrcode.scanner.data.local.dao.AccountDao
import com.qrcode.scanner.data.local.dao.AccountDefaultCategoryDao
import com.qrcode.scanner.data.local.dao.CategoryDao
import com.qrcode.scanner.data.local.dao.ExpenseDao
import com.qrcode.scanner.data.local.dao.ReceiptAttachmentDao
import com.qrcode.scanner.data.local.dao.ReceiptDao
import com.qrcode.scanner.data.local.dao.ReceiptItemDao
import com.qrcode.scanner.data.local.dao.ReceiptRawDao
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
}
