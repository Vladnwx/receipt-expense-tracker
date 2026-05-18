package com.qrcode.scanner.data.repository

import com.qrcode.scanner.data.local.dao.ReceiptDao
import com.qrcode.scanner.data.local.dao.ReceiptItemDao
import com.qrcode.scanner.data.local.dao.ReceiptRawDao
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity
import com.qrcode.scanner.data.local.entity.ReceiptRawEntity
import com.qrcode.scanner.data.local.entity.ReceiptStatus
import com.qrcode.scanner.domain.fetcher.FetchResult
import com.qrcode.scanner.domain.fetcher.FetchedReceipt
import com.qrcode.scanner.domain.fetcher.FnsReceiptFetcher
import com.qrcode.scanner.domain.parser.FnsReceiptParser
import com.qrcode.scanner.domain.parser.FnsQrData
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedReceiptResult(
    val raw: ReceiptRawEntity,
    val receipt: ReceiptEntity?,
    val items: List<ReceiptItemEntity>,
    val unauthorized: Boolean = false
)

@Singleton
class ReceiptRepository @Inject constructor(
    private val rawDao: ReceiptRawDao,
    private val receiptDao: ReceiptDao,
    private val itemDao: ReceiptItemDao,
    private val parser: FnsReceiptParser,
    private val fetcher: FnsReceiptFetcher
) {

    suspend fun saveRaw(rawData: String): ReceiptRawEntity {
        val format = parser.detectFormat(rawData)
        val entity = ReceiptRawEntity(
            rawData = rawData,
            format = format
        )
        val id = rawDao.insert(entity)
        return entity.copy(id = id)
    }

    suspend fun createReceiptFromQr(rawId: Long, qrData: FnsQrData): ReceiptEntity? {
        val entity = ReceiptEntity(
            rawId = rawId,
            fiscalDriveNumber = qrData.fiscalNumber,
            fiscalDocumentNumber = qrData.fiscalDocument,
            fiscalSign = qrData.fiscalSign,
            amount = qrData.sum ?: 0.0,
            date = parseDate(qrData.date) ?: 0L,
            operationType = qrData.operationType,
            status = ReceiptStatus.Pending.name
        )
        val id = receiptDao.insert(entity)
        return entity.copy(id = id)
    }

    suspend fun fetchAndUpdate(receiptId: Long): Boolean {
        val receipt = receiptDao.getById(receiptId) ?: return false
        val raw = rawDao.getById(receipt.rawId) ?: return false
        val qrData = parser.parse(raw.rawData) ?: return false

        return when (val result = fetcher.fetch(qrData)) {
            is FetchResult.Success -> {
                val fetched = result.receipt
                val updated = receipt.copy(
                    amount = fetched.totalSum,
                    date = parseDate(qrData.date) ?: receipt.date,
                    retailerName = fetched.retailPlace,
                    retailerInn = fetched.retailerInn,
                    status = ReceiptStatus.Checked.name
                )
                receiptDao.update(updated)
                saveItems(receiptId, fetched)
                rawDao.markParsed(receipt.rawId)
                true
            }
            is FetchResult.Unauthorized -> false
            is FetchResult.NotFound, is FetchResult.Error -> {
                val updated = receipt.copy(status = ReceiptStatus.Failed.name)
                receiptDao.update(updated)
                false
            }
        }
    }

    suspend fun checkUncheckedReceipts(): Int {
        val unchecked = receiptDao.getUnchecked()
        var checked = 0
        for (receipt in unchecked) {
            if (fetchAndUpdate(receipt.id)) {
                checked++
            }
        }
        return checked
    }

    suspend fun findExistingReceipt(fn: String, fd: String, fp: String): ReceiptEntity? =
        receiptDao.findByFiscalInfo(fn, fd, fp)

    suspend fun getAllReceipts(): List<ReceiptEntity> = receiptDao.getAll()

    suspend fun getReceiptById(id: Long): ReceiptEntity? = receiptDao.getById(id)

    suspend fun getItemsByReceiptId(receiptId: Long): List<ReceiptItemEntity> =
        itemDao.getByReceiptId(receiptId)

    suspend fun getRawById(id: Long): ReceiptRawEntity? = rawDao.getById(id)

    suspend fun getAllRaws(): List<ReceiptRawEntity> = rawDao.getAll()

    suspend fun getUncheckedReceipts(): List<ReceiptEntity> = receiptDao.getUnchecked()

    suspend fun deleteReceipt(receipt: ReceiptEntity) {
        itemDao.getByReceiptId(receipt.id).forEach { itemDao.updateCategory(it.id, null) }
    }

    private suspend fun saveItems(
        receiptId: Long,
        fetched: FetchedReceipt
    ): List<ReceiptItemEntity> {
        val entities = fetched.items.map { item ->
            ReceiptItemEntity(
                receiptId = receiptId,
                name = item.name,
                price = item.price,
                quantity = item.quantity,
                amount = item.sum
            )
        }
        itemDao.insertAll(entities)
        return entities
    }

    private fun parseDate(dateTime: String?): Long? {
        if (dateTime == null) return null
        return try {
            val cleaned = dateTime.replace("T", " ").substringBefore("+").substringBefore("Z")
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            formatter.parse(cleaned)?.time
        } catch (e: Exception) {
            null
        }
    }
}
