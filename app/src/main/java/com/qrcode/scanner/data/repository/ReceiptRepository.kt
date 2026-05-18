package com.qrcode.scanner.data.repository

import com.qrcode.scanner.data.local.dao.ReceiptDao
import com.qrcode.scanner.data.local.dao.ReceiptItemDao
import com.qrcode.scanner.data.local.dao.ReceiptRawDao
import com.qrcode.scanner.data.local.entity.ReceiptEntity
import com.qrcode.scanner.data.local.entity.ReceiptItemEntity
import com.qrcode.scanner.data.local.entity.ReceiptRawEntity
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

    suspend fun parseAndFetch(rawId: Long): ParsedReceiptResult? {
        val raw = rawDao.getById(rawId) ?: return null
        val qrData = parser.parse(raw.rawData) ?: return null
        when (val result = fetcher.fetch(qrData)) {
            is FetchResult.Unauthorized -> {
                return ParsedReceiptResult(
                    raw = raw,
                    receipt = null,
                    items = emptyList(),
                    unauthorized = true
                )
            }
            is FetchResult.NotFound, is FetchResult.Error -> {
                return ParsedReceiptResult(raw = raw, receipt = null, items = emptyList())
            }
            is FetchResult.Success -> {
                val fetched = result.receipt
                val receipt = saveReceipt(raw.id, qrData, fetched)
                val items = saveItems(receipt.id, fetched)
                rawDao.markParsed(rawId)
                return ParsedReceiptResult(raw = raw, receipt = receipt, items = items)
            }
        }
    }

    private suspend fun saveReceipt(
        rawId: Long,
        qrData: FnsQrData,
        fetched: FetchedReceipt?
    ): ReceiptEntity {
        val entity = ReceiptEntity(
            rawId = rawId,
            fiscalDriveNumber = qrData.fiscalNumber,
            fiscalDocumentNumber = qrData.fiscalDocument,
            fiscalSign = qrData.fiscalSign,
            amount = fetched?.totalSum ?: qrData.sum ?: 0.0,
            date = parseDate(qrData.date) ?: 0L,
            retailerName = fetched?.retailPlace,
            retailerInn = fetched?.retailerInn,
            operationType = qrData.operationType
        )
        val id = receiptDao.insert(entity)
        return entity.copy(id = id)
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

    suspend fun findExistingReceipt(fn: String, fd: String, fp: String): ReceiptEntity? =
        receiptDao.findByFiscalInfo(fn, fd, fp)

    suspend fun getAllReceipts(): List<ReceiptEntity> = receiptDao.getAll()

    suspend fun getReceiptById(id: Long): ReceiptEntity? = receiptDao.getById(id)

    suspend fun getItemsByReceiptId(receiptId: Long): List<ReceiptItemEntity> =
        itemDao.getByReceiptId(receiptId)

    suspend fun getRawById(id: Long): ReceiptRawEntity? = rawDao.getById(id)

    suspend fun getAllRaws(): List<ReceiptRawEntity> = rawDao.getAll()

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
