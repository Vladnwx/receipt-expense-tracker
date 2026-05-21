package com.vladnwx.receiptexpensetracker.data.remote.cbr

import retrofit2.http.GET

interface CbrApi {
    @GET("daily_json.js")
    suspend fun getDailyRates(): CbrDailyResponse
}
