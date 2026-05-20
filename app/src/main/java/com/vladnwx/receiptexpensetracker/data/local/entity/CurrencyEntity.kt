package com.vladnwx.receiptexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val symbol: String,
    val rateToBase: Double = 1.0,
    val isBase: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
