package com.vladnwx.receiptexpensetracker.data.remote.cbr

import retrofit2.http.GET

interface CbrApi {
    @GET("daily_json.json")
    suspend fun getDailyRates(): CbrDailyResponse
}
