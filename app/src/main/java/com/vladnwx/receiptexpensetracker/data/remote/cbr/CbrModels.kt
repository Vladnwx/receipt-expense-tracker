package com.vladnwx.receiptexpensetracker.data.remote.cbr

import com.google.gson.annotations.SerializedName

data class CbrDailyResponse(
    @SerializedName("Date") val date: String,
    @SerializedName("Valute") val valute: Map<String, CbrValuteResponse>
)

data class CbrValuteResponse(
    @SerializedName("CharCode") val charCode: String,
    @SerializedName("Nominal") val nominal: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("Value") val value: Double
)
