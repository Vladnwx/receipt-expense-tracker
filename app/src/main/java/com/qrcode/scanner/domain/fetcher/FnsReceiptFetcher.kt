package com.qrcode.scanner.domain.fetcher

import com.qrcode.scanner.data.remote.FnsApiService
import com.qrcode.scanner.data.remote.FnsReceipt
import com.qrcode.scanner.domain.parser.FnsQrData
import javax.inject.Inject
import javax.inject.Singleton

data class FetchedReceipt(
    val items: List<FetchedItem>,
    val totalSum: Double,
    val dateTime: String?,
    val retailPlace: String?,
    val user: String?,
    val retailerInn: String?
)

data class FetchedItem(
    val name: String,
    val price: Double,
    val quantity: Double,
    val sum: Double
)

@Singleton
class FnsReceiptFetcher @Inject constructor(
    private val apiService: FnsApiService
) {

    suspend fun fetch(qrData: FnsQrData): FetchedReceipt? {
        return try {
            val response = apiService.getTicketInfo(
                fiscalNumber = qrData.fiscalNumber,
                fiscalDocument = qrData.fiscalDocument,
                fiscalSign = qrData.fiscalSign,
                operationType = qrData.operationType,
                date = qrData.date,
                sum = qrData.sum
            )

            if (response.code != null && response.code != 0) {
                return null
            }

            val receipt = response.data?.ticket?.document?.receipt ?: return null
            mapToFetched(receipt)
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToFetched(receipt: FnsReceipt): FetchedReceipt {
        val items = receipt.items?.mapNotNull { item ->
            if (item.name.isNullOrBlank()) return@mapNotNull null
            FetchedItem(
                name = item.name,
                price = (item.price ?: 0) / 100.0,
                quantity = item.quantity ?: 1.0,
                sum = (item.sum ?: 0) / 100.0
            )
        } ?: emptyList()

        return FetchedReceipt(
            items = items,
            totalSum = (receipt.totalSum ?: 0) / 100.0,
            dateTime = receipt.dateTime,
            retailPlace = receipt.retailPlace,
            user = receipt.user,
            retailerInn = receipt.retailerInn
        )
    }
}
