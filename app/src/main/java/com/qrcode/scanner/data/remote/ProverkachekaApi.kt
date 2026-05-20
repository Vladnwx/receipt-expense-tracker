package com.qrcode.scanner.data.remote

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

data class ProverkachekaRequest(
    val fn: String,
    val fd: String,
    val fp: String,
    val n: Int = 1,
    val s: Double? = null,
    val t: String? = null,
    val token: String? = null,
    val qr: Int = 1
)

data class ProverkachekaResponse(
    val code: Int? = null,
    val first: Int? = null,
    val data: JsonElement? = null
)

data class ProverkachekaJson(
    val items: List<ProverkachekaItem>? = null,
    val totalSum: Long? = null,
    val dateTime: String? = null,
    val retailPlace: String? = null,
    val user: String? = null,
    val retailerInn: String? = null,
    val operationType: Int? = null
)

data class ProverkachekaItem(
    val name: String? = null,
    val price: Long? = null,
    val quantity: Double? = null,
    val sum: Long? = null
)

interface ProverkachekaApi {

    @POST("api/v1/check/get")
    suspend fun getCheckInfo(
        @Body request: ProverkachekaRequest
    ): ProverkachekaResponse
}
