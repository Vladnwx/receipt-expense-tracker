package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OperationType {
    EXPENSE, INCOME, TRANSFER
}

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("accountId"), Index("eventId"), Index("advanceReportId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: OperationType = OperationType.EXPENSE,
    val receiptId: Long? = null,
    val receiptItemId: Long? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val amount: Double = 0.0,
    val quantity: Double? = null,
    val price: Double? = null,
    val description: String? = null,
    val tags: String? = null,
    val date: Long = System.currentTimeMillis(),
    val isFamilyExpense: Boolean = false,
    val attachmentPath: String? = null,
    val eventId: Long? = null,
    val advanceReportId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
