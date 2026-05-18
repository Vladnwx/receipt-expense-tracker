package com.qrcode.scanner.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_attachments",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptRawEntity::class,
            parentColumns = ["id"],
            childColumns = ["rawId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("rawId"), Index("receiptId")]
)
data class ReceiptAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawId: Long? = null,
    val receiptId: Long? = null,
    val filePath: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long = 0,
    val ocrText: String? = null,
    val uploadedAt: Long = System.currentTimeMillis()
)
