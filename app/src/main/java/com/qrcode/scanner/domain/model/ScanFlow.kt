package com.qrcode.scanner.domain.model

import com.qrcode.scanner.data.local.entity.ReceiptAttachmentEntity
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity
import com.qrcode.scanner.data.local.entity.ReceiptRawEntity

data class ScanFlow(
    val raw: ReceiptRawEntity,
    val receipt: ReceiptEntity?,
    val items: List<ItemWithCategory>,
    val attachments: List<ReceiptAttachmentEntity>,
    val fnsStatus: FnsStatus,
    val accountId: Long? = null
)

data class ItemWithCategory(
    val item: ReceiptItemEntity,
    val categoryName: String?,
    val classificationConfidence: Float?
)

enum class FnsStatus {
    NOT_PARSED,
    PARSING,
    FETCHING,
    FETCHED,
    FAILED
}
