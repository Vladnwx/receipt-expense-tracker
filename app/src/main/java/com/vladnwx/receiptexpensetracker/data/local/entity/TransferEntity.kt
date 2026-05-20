package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fromAccountId"), Index("toAccountId")]
)
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: Double = 0.0,
    val fromCurrency: String = "RUB",
    val toCurrency: String = "RUB",
    val rate: Double = 1.0,
    val commission: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
