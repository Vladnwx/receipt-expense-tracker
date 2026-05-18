package com.qrcode.scanner.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FnsApiService {

    @GET("api/v1/ticket")
    suspend fun getTicketInfo(
        @Query("fn") fiscalNumber: String,
        @Query("i") fiscalDocument: String,
        @Query("fp") fiscalSign: String,
        @Query("n") operationType: Int = 1,
        @Query("date") date: String? = null,
        @Query("sum") sum: Double? = null
    ): FnsTicketResponse
}

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
