package com.qrcode.scanner.domain.fetcher

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.qrcode.scanner.data.remote.ProverkachekaApi
import com.qrcode.scanner.data.remote.ProverkachekaItem
import com.qrcode.scanner.data.remote.ProverkachekaJson
import com.qrcode.scanner.data.remote.ProverkachekaRequest
import com.qrcode.scanner.data.reporter.AppLogger
import com.qrcode.scanner.data.repository.TokenRepository
import com.qrcode.scanner.domain.parser.FnsQrData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class FetchResult {
    data class Success(val receipt: FetchedReceipt) : FetchResult()
    data object Unauthorized : FetchResult()
    data object NotFound : FetchResult()
    data object RateLimited : FetchResult()
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
    private val gson = Gson()

    suspend fun fetch(qrData: FnsQrData): FetchResult {
        AppLogger.i("ReceiptFetcher", "fetch fn=${qrData.fiscalNumber} fd=${qrData.fiscalDocument} fp=${qrData.fiscalSign}")
        val startTime = System.currentTimeMillis()
        val result = tryProverkacheka(qrData)
        val elapsed = System.currentTimeMillis() - startTime
        AppLogger.i("ReceiptFetcher", "fetch done in ${elapsed}ms: $result")
        return result ?: FetchResult.NotFound
    }

    private suspend fun tryProverkacheka(qrData: FnsQrData): FetchResult? {
        return try {
            val token = tokenRepository.getToken()
            if (token.isNullOrBlank()) {
                AppLogger.w("ReceiptFetcher", "proverkacheka token not set — Unauthorized")
                return FetchResult.Unauthorized
            }
            AppLogger.d("ReceiptFetcher", "calling proverkacheka fn=${qrData.fiscalNumber} fd=${qrData.fiscalDocument}")

            val response = withContext(Dispatchers.IO) {
                proverkachekaApi.getCheckInfo(
                    ProverkachekaRequest(
                        fn = qrData.fiscalNumber,
                        fd = qrData.fiscalDocument,
                        fp = qrData.fiscalSign,
                        n = qrData.operationType,
                        s = qrData.sum,
                        t = qrData.date,
                        token = token
                    )
                )
            }

            AppLogger.d("ReceiptFetcher", "proverkacheka response code=${response.code} first=${response.first}")

            val code = response.code ?: return null
            when (code) {
                1 -> {
                    // success — data received
                }
                0 -> {
                    val msg = response.data?.asJsonPrimitive?.asString ?: "чек некорректен"
                    AppLogger.w("ReceiptFetcher", "proverkacheka: $msg")
                    return FetchResult.NotFound
                }
                2 -> {
                    AppLogger.w("ReceiptFetcher", "proverkacheka: данные пока не получены")
                    return FetchResult.NotFound
                }
                3, 4 -> {
                    AppLogger.w("ReceiptFetcher", "proverkacheka: rate limited (code=$code)")
                    return FetchResult.RateLimited
                }
                5 -> {
                    val msg = response.data?.asJsonPrimitive?.asString ?: "прочее (данные не получены)"
                    AppLogger.w("ReceiptFetcher", "proverkacheka: $msg")
                    return FetchResult.NotFound
                }
                else -> {
                    AppLogger.w("ReceiptFetcher", "proverkacheka: unknown API code=$code")
                    return FetchResult.Error("API error code=$code")
                }
            }

            val dataObj = response.data?.asJsonObject ?: run {
                AppLogger.w("ReceiptFetcher", "proverkacheka: response data is not an object")
                return null
            }
            val jsonEl = dataObj.get("json")
            val json = gson.fromJson(jsonEl, ProverkachekaJson::class.java)
            if (json == null || json.items.isNullOrEmpty()) {
                AppLogger.w("ReceiptFetcher", "proverkacheka: no items in response")
                return null
            }

            AppLogger.i("ReceiptFetcher", "proverkacheka success: ${json.items.size} items, totalSum=${json.totalSum}, place=${json.retailPlace}")
            FetchResult.Success(mapPkToFetched(json))
        } catch (e: ClassCastException) {
            AppLogger.w("ReceiptFetcher", "proverkacheka: CameraX ClassCastException (ignored), will retry")
            null
        } catch (e: Exception) {
            AppLogger.e("ReceiptFetcher", "proverkacheka request failed", e)
            null
        }
    }

    private fun mapPkToFetched(json: ProverkachekaJson): FetchedReceipt {
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
