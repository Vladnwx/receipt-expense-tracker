package com.qrcode.scanner.domain.parser

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qrcode.scanner.data.remote.FnsShareResponse
import com.qrcode.scanner.domain.fetcher.FetchedItem
import com.qrcode.scanner.domain.fetcher.FetchedReceipt
import javax.inject.Inject

class FnsShareParser @Inject constructor() {
    private val gson = Gson()

    fun parse(jsonString: String): FnsShareResponse? {
        return try {
            val listType = object : TypeToken<List<FnsShareResponse>>() {}.type
            val list: List<FnsShareResponse> = gson.fromJson(jsonString, listType)
            list.firstOrNull()
        } catch (e: Exception) {
            try {
                gson.fromJson(jsonString, FnsShareResponse::class.java)
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun toFnsQrData(response: FnsShareResponse): FnsQrData {
        val r = response.ticket.document.receipt
        return FnsQrData(
            fiscalNumber = r.fiscalDriveNumber,
            fiscalDocument = r.fiscalDocumentNumber.toString(),
            fiscalSign = r.fiscalSign.toString(),
            sum = r.totalSum / 100.0,
            date = r.dateTime,
            operationType = r.operationType
        )
    }

    fun toFetchedReceipt(response: FnsShareResponse): FetchedReceipt {
        val r = response.ticket.document.receipt
        return FetchedReceipt(
            items = r.items.map { item ->
                FetchedItem(
                    name = item.name,
                    price = item.price / 100.0,
                    quantity = item.quantity,
                    sum = item.sum / 100.0
                )
            },
            totalSum = r.totalSum / 100.0,
            dateTime = r.dateTime,
            retailPlace = r.retailPlace,
            user = r.user,
            retailerInn = r.userInn
        )
    }

    fun extractQrData(jsonString: String): FnsQrData? {
        return parse(jsonString)?.let { toFnsQrData(it) }
    }
}
