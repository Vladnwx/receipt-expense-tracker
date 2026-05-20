package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "CARD",
    val includeInBudget: Boolean = true,
    val color: Int = 0,
    val icon: String? = null,
    val initialBalance: Double = 0.0,
    val currency: String = "RUB",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
