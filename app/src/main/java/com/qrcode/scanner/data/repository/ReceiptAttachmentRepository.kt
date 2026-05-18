package com.qrcode.scanner.data.repository

import com.qrcode.scanner.data.local.dao.ReceiptAttachmentDao
import com.qrcode.scanner.data.local.entity.ReceiptAttachmentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptAttachmentRepository @Inject constructor(
    private val attachmentDao: ReceiptAttachmentDao
) {
    suspend fun save(entity: ReceiptAttachmentEntity): Long = attachmentDao.insert(entity)

    suspend fun getByRawId(rawId: Long): List<ReceiptAttachmentEntity> =
        attachmentDao.getByRawId(rawId)

    suspend fun getByReceiptId(receiptId: Long): List<ReceiptAttachmentEntity> =
        attachmentDao.getByReceiptId(receiptId)

    suspend fun getOrphans(): List<ReceiptAttachmentEntity> = attachmentDao.getOrphans()

    suspend fun delete(id: Long) = attachmentDao.deleteById(id)

    suspend fun updateOcrText(id: Long, ocrText: String) =
        attachmentDao.updateOcrText(id, ocrText)
}
