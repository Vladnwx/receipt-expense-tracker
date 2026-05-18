package com.qrcode.scanner.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FnsApiService {

    @GET("api/v1/ticket")
    suspend fun getTicketInfo(
        @Header("Cookie") cookies: String? = null,
        @Query("fn") fiscalNumber: String,
        @Query("i") fiscalDocument: String,
        @Query("fp") fiscalSign: String,
        @Query("n") operationType: Int = 1,
        @Query("date") date: String? = null,
        @Query("sum") sum: Double? = null
    ): FnsTicketResponse

    @POST("api/v1/auth/phone/request-code")
    suspend fun requestAuthCode(
        @Body body: FnsAuthCodeRequest
    ): FnsAuthCodeResponse

    @POST("api/v1/auth/phone/confirm-code")
    suspend fun confirmAuthCode(
        @Body body: FnsAuthConfirmRequest
    ): FnsAuthConfirmResponse
}

data class FnsAuthCodeRequest(
    val phone: String
)

data class FnsAuthConfirmRequest(
    val phone: String,
    val code: String
)

data class FnsAuthCodeResponse(
    val code: Int? = null,
    val message: String? = null,
    val data: FnsAuthCodeData? = null
)

data class FnsAuthCodeData(
    @SerializedName("sessionId")
    val sessionId: String? = null,
    @SerializedName("expiresIn")
    val expiresIn: Long? = null
)

data class FnsAuthConfirmResponse(
    val code: Int? = null,
    val message: String? = null,
    val data: FnsAuthConfirmData? = null
)

data class FnsAuthConfirmData(
    @SerializedName("sessionId")
    val sessionId: String? = null,
    @SerializedName("deviceId")
    val deviceId: String? = null,
    val cookies: String? = null
)

data class FnsTicketResponse(
    val code: Int? = null,
    val error: String? = null,
    val data: FnsTicketData? = null,
    val message: String? = null
)

data class FnsTicketData(
    val ticket: FnsTicket? = null
)

data class FnsTicket(
    val document: FnsDocument? = null
)

data class FnsDocument(
    val receipt: FnsReceipt? = null
)

data class FnsReceipt(
    val totalSum: Long? = null,
    val dateTime: String? = null,
    val operationType: Int? = null,
    val fiscalDriveNumber: String? = null,
    val fiscalDocumentNumber: Long? = null,
    val fiscalSign: Long? = null,
    val retailPlace: String? = null,
    val user: String? = null,
    val retailerInn: String? = null,
    val items: List<FnsItem>? = null
)

data class FnsItem(
    val name: String? = null,
    val price: Long? = null,
    val quantity: Double? = null,
    val sum: Long? = null
)
