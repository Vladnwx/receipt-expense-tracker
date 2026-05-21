package com.vladnwx.receiptexpensetracker.data.repository

import com.vladnwx.receiptexpensetracker.data.local.dao.CurrencyDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CurrencyEntity
import com.vladnwx.receiptexpensetracker.data.remote.cbr.CbrApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val cbrApi: CbrApi
) {
    suspend fun getAll(): List<CurrencyEntity> = currencyDao.getAll()

    suspend fun getBaseCurrency(): CurrencyEntity? = currencyDao.getBaseCurrency()

    suspend fun getRate(code: String): Double {
        val currency = currencyDao.getByCode(code) ?: return 1.0
        return if (currency.isBase) 1.0 else currency.rateToBase
    }

    suspend fun fetchAndSaveRates() {
        val response = cbrApi.getDailyRates()
        val now = System.currentTimeMillis()

        currencyDao.deleteAll()

        currencyDao.insert(CurrencyEntity(
            code = "RUB", name = "Российский рубль", symbol = "₽",
            rateToBase = 1.0, isBase = true, updatedAt = now
        ))

        val codesToTrack = listOf("USD", "EUR", "CNY", "GBP", "KZT", "BYN")
        response.valute.forEach { (_, v) ->
            if (v.charCode in codesToTrack) {
                currencyDao.insert(CurrencyEntity(
                    code = v.charCode,
                    name = v.name,
                    symbol = v.charCode.take(1),
                    rateToBase = v.value / v.nominal,
                    isBase = false,
                    updatedAt = now
                ))
            }
        }
    }
}
