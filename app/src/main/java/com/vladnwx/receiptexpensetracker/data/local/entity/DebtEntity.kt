package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DebtType { LEND, BORROW }
enum class DebtStatus { ACTIVE, CLOSED }

@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId")]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val amount: Double = 0.0,
    val type: DebtType = DebtType.LEND,
    val status: DebtStatus = DebtStatus.ACTIVE,
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
