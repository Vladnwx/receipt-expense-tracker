package com.qrcode.scanner.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ReceiptStatus {
    Pending,
    Checked,
    Failed
}

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptRawEntity::class,
            parentColumns = ["id"],
            childColumns = ["rawId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rawId")]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawId: Long,
    val fiscalDriveNumber: String = "",
    val fiscalDocumentNumber: String = "",
    val fiscalSign: String = "",
    val amount: Double = 0.0,
    val date: Long = 0,
    val retailerName: String? = null,
    val retailerInn: String? = null,
    val operationType: Int = 1,
    val status: String = ReceiptStatus.Pending.name
)
