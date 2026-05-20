package com.qrcode.scanner.data.remote

import com.google.gson.annotations.SerializedName

data class FnsShareResponse(
    @SerializedName("_id") val id: String,
    val createdAt: String,
    val ticket: FnsTicket
)

data class FnsTicket(
    val document: FnsDocument
)

data class FnsDocument(
    val receipt: FnsReceipt
)

data class FnsReceipt(
    val dateTime: String,
    val fiscalDriveNumber: String,
    val fiscalDocumentNumber: Long,
    val fiscalSign: Long,
    val totalSum: Long,
    val ecashTotalSum: Long? = null,
    val cashTotalSum: Long? = null,
    val prepaidSum: Long? = null,
    val provisionSum: Long? = null,
    val creditSum: Long? = null,
    val retailPlace: String? = null,
    val retailPlaceAddress: String? = null,
    val user: String? = null,
    val userInn: String? = null,
    val items: List<FnsShareItem>,
    val operationType: Int = 1,
    val kktRegId: String? = null,
    val machineNumber: String? = null,
    val shiftNumber: Int? = null,
    val requestNumber: Int? = null,
    val taxationType: Int? = null,
    val appliedTaxationType: Int? = null,
    val fiscalDocumentFormatVer: Int? = null,
    val amountsReceiptNds: FnsAmountsReceiptNds? = null,
    val properties: FnsProperties? = null,
    val sellerAddress: String? = null,
    @SerializedName("fnsUrl") val fnsUrl: String? = null,
    val code: Int? = null
)

data class FnsShareItem(
    val name: String,
    val price: Long,
    val quantity: Double,
    val sum: Long,
    val nds: Int? = null,
    val paymentType: Int? = null,
    val productType: Int? = null,
    val productCodeData: FnsProductCode? = null
)

data class FnsProductCode(
    val gtin: Long? = null,
    val rawProductCode: String? = null,
    val productIdType: Int? = null,
    val sernum: String? = null
)

data class FnsAmountsReceiptNds(
    val amountsNds: List<FnsAmountNds>? = null
)

data class FnsAmountNds(
    val nds: Int,
    val ndsSum: Long
)

data class FnsProperties(
    val propertyName: String? = null,
    val propertyValue: String? = null
)
