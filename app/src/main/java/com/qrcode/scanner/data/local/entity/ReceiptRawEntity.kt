package com.qrcode.scanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipt_raw")
data class ReceiptRawEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawData: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val format: String = "",
    val isParsed: Boolean = false
)
