package com.qrcode.scanner.domain.fetcher

import com.qrcode.scanner.data.remote.FnsApiService
import com.qrcode.scanner.data.remote.FnsReceipt
import com.qrcode.scanner.BuildConfig
import com.qrcode.scanner.data.remote.ProverkachekaApi
import com.qrcode.scanner.data.remote.ProverkachekaRequest
import com.qrcode.scanner.data.reporter.AppLogger
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
    private val proverkachekaApi: ProverkachekaApi,
    private val fnsAuthService: FnsAuthService
) {

    suspend fun fetch(qrData: FnsQrData): FetchResult {
        AppLogger.i("ReceiptFetcher", "fetch fn=${qrData.fiscalNumber} fd=${qrData.fiscalDocument}")

        val pkResult = tryProverkacheka(qrData)
        if (pkResult != null) return pkResult

        val fnsResult = tryFns(qrData)
        if (fnsResult != null) return fnsResult

        return FetchResult.NotFound
    }

    private suspend fun tryProverkacheka(qrData: FnsQrData): FetchResult? {
        return try {
            AppLogger.d("ReceiptFetcher", "trying proverkacheka.com")
            val response = proverkachekaApi.getCheckInfo(
                ProverkachekaRequest(
                    fn = qrData.fiscalNumber,
                    fd = qrData.fiscalDocument,
                    fp = qrData.fiscalSign,
                    n = qrData.operationType,
                    s = qrData.sum?.let { (it * 100).toLong() },
                    t = qrData.date,
                    token = BuildConfig.PROVERKACHEKA_TOKEN.ifBlank { null }
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

    private suspend fun tryFns(qrData: FnsQrData): FetchResult? {
        return try {
            AppLogger.d("ReceiptFetcher", "trying check.nalog.ru")
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
                AppLogger.w("ReceiptFetcher", "FNS response code=${response.code}")
                return null
            }
            val receipt = response.data?.ticket?.document?.receipt
                ?: return null
            AppLogger.i("ReceiptFetcher", "FNS: success")
            FetchResult.Success(mapFnsToFetched(receipt))
        } catch (e: FnsAuthService.AuthError) {
            AppLogger.w("ReceiptFetcher", "FNS unauthorized")
            null
        } catch (e: Exception) {
            AppLogger.w("ReceiptFetcher", "FNS failed: ${e.localizedMessage}")
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

    private fun mapFnsToFetched(receipt: FnsReceipt): FetchedReceipt {
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
