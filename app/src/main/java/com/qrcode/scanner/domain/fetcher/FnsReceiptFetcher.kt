package com.qrcode.scanner.domain.fetcher

import com.qrcode.scanner.data.remote.FnsApiService
import com.qrcode.scanner.data.remote.FnsReceipt
import com.qrcode.scanner.domain.fns.FnsAuthService
import com.qrcode.scanner.domain.parser.FnsQrData
import javax.inject.Inject
import javax.inject.Singleton

sealed class FetchResult {
    data class Success(val receipt: FetchedReceipt) : FetchResult()
    data object Unauthorized : FetchResult()
    data object NotFound : FetchResult()
    data class Error(val message: String) : FetchResult()
}

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
    private val apiService: FnsApiService,
    private val fnsAuthService: FnsAuthService
) {

    suspend fun fetch(qrData: FnsQrData): FetchResult {
        return try {
            val cookies = fnsAuthService.getActiveSession()?.cookies
            val response = apiService.getTicketInfo(
                cookies = cookies,
                fiscalNumber = qrData.fiscalNumber,
                fiscalDocument = qrData.fiscalDocument,
                fiscalSign = qrData.fiscalSign,
                operationType = qrData.operationType,
                date = qrData.date,
                sum = qrData.sum
            )

            if (response.code != null && response.code != 0) {
                return FetchResult.NotFound
            }

            val receipt = response.data?.ticket?.document?.receipt
                ?: return FetchResult.NotFound
            FetchResult.Success(mapToFetched(receipt))
        } catch (e: FnsAuthService.AuthError) {
            FetchResult.Unauthorized
        } catch (e: Exception) {
            FetchResult.Error(e.localizedMessage ?: "Неизвестная ошибка")
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
