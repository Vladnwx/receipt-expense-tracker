package com.qrcode.scanner.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qrcode.scanner.data.local.AppDatabase
import com.qrcode.scanner.data.local.dao.AccountDao
import com.qrcode.scanner.data.local.dao.AccountDefaultCategoryDao
import com.qrcode.scanner.data.local.dao.CategoryDao
import com.qrcode.scanner.data.local.dao.ExpenseDao
import com.qrcode.scanner.data.local.dao.ReceiptAttachmentDao
import com.qrcode.scanner.data.local.dao.ReceiptDao
import com.qrcode.scanner.data.local.dao.ReceiptItemDao
import com.qrcode.scanner.data.local.dao.ReceiptRawDao
import com.qrcode.scanner.data.local.entity.CategoryEntity
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "receipt_expense_db"
        )
            .fallbackToDestructiveMigration()
            .addCallback(seedCallback)
            .build()
    }

    private val seedCallback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedPredefinedCategories(db)
        }

        private fun seedPredefinedCategories(db: SupportSQLiteDatabase) {
            val categories = listOf(
                "Продукты" to "🛒",
                "Транспорт" to "🚗",
                "Жильё" to "🏠",
                "Здоровье" to "💊",
                "Развлечения" to "🎬",
                "Рестораны" to "🍽️",
                "Одежда" to "👕",
                "Связь" to "📱",
                "Образование" to "📚",
                "Подарки" to "🎁",
                "Спорт" to "🏋️",
                "Другое" to "📦"
            )
            categories.forEach { (name, icon) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO categories (name, icon, isPredefined) VALUES (?, ?, 1)",
                    arrayOf(name, icon)
                )
            }
        }
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
