package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AdvanceType { ADVANCE, REIMBURSEMENT }
enum class AdvanceStatus { OPEN, CLOSED }

@Entity(
    tableName = "advance_reports",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId"), Index("accountId")]
)
data class AdvanceReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val accountId: Long,
    val amount: Double = 0.0,
    val type: AdvanceType = AdvanceType.ADVANCE,
    val status: AdvanceStatus = AdvanceStatus.OPEN,
    val purpose: String? = null,
    val dueDate: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
