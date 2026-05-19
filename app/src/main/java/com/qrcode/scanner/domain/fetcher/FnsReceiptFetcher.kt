package com.qrcode.scanner.domain.fetcher

import com.qrcode.scanner.data.remote.ProverkachekaApi
import com.qrcode.scanner.data.remote.ProverkachekaRequest
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.repository.TokenRepository
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
    private val proverkachekaApi: ProverkachekaApi,
    private val tokenRepository: TokenRepository
) {

    suspend fun fetch(qrData: FnsQrData): FetchResult {
        AppLogger.i("ReceiptFetcher", "fetch fn=${qrData.fiscalNumber} fd=${qrData.fiscalDocument}")
        return tryProverkacheka(qrData) ?: FetchResult.NotFound
    }

    private suspend fun tryProverkacheka(qrData: FnsQrData): FetchResult? {
        return try {
            AppLogger.d("ReceiptFetcher", "trying proverkacheka.com")
            val token = tokenRepository.getToken()
            if (token.isNullOrBlank()) {
                AppLogger.w("ReceiptFetcher", "proverkacheka token not set")
                return FetchResult.Unauthorized
            }
            val response = proverkachekaApi.getCheckInfo(
                ProverkachekaRequest(
                    fn = qrData.fiscalNumber,
                    fd = qrData.fiscalDocument,
                    fp = qrData.fiscalSign,
                    n = qrData.operationType,
                    s = qrData.sum?.let { (it * 100).toLong() },
                    t = qrData.date,
                    token = token
                )
            )
            val json = response.data?.json
            if (json == null || json.items.isNullOrEmpty()) {
                AppLogger.w("ReceiptFetcher", "proverkacheka: no data")
                return null
            }
            AppLogger.i("ReceiptFetcher", "proverkacheka: success")
            FetchResult.Success(mapPkToFetched(json))
        } catch (e: Exception) {
            AppLogger.w("ReceiptFetcher", "proverkacheka failed: ${e.localizedMessage}")
            null
        }
    }

    private fun mapPkToFetched(json: com.qrcode.scanner.data.remote.ProverkachekaJson): FetchedReceipt {
        val items = json.items?.mapNotNull { item ->
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
            totalSum = (json.totalSum ?: 0) / 100.0,
            dateTime = json.dateTime,
            retailPlace = json.retailPlace,
            user = json.user,
            retailerInn = json.retailerInn
        )
    }
}
