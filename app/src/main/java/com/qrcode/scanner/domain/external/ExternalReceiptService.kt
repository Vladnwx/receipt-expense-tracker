package com.qrcode.scanner.domain.external

import com.qrcode.scanner.domain.fetcher.FetchedReceipt
import com.qrcode.scanner.domain.parser.FnsQrData

interface ExternalReceiptService {

    val serviceName: String

    val serviceType: ServiceType

    val isAvailable: Boolean

    suspend fun fetchByQrData(qrData: FnsQrData): FetchedReceipt?

    suspend fun fetchByFiscalInfo(fn: String, fd: String, fp: String): FetchedReceipt?

    suspend fun checkHealth(): Boolean

    enum class ServiceType {
        OFD,
        PLATFORM,
        OTHER
    }

    data class ServiceInfo(
        val name: String,
        val type: ServiceType,
        val baseUrl: String,
        val isAuthenticated: Boolean,
        val requiresAuth: Boolean
    )
}
