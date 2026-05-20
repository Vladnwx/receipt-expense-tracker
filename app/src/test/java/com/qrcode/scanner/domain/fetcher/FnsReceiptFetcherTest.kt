package com.qrcode.scanner.domain.fetcher

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.qrcode.scanner.data.remote.ProverkachekaApi
import com.qrcode.scanner.data.remote.ProverkachekaItem
import com.qrcode.scanner.data.remote.ProverkachekaJson
import com.qrcode.scanner.data.remote.ProverkachekaRequest
import com.qrcode.scanner.data.remote.ProverkachekaResponse
import com.qrcode.scanner.data.repository.TokenRepository
import com.qrcode.scanner.domain.parser.FnsQrData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FnsReceiptFetcherTest {

    private val api: ProverkachekaApi = mockk()
    private val tokenRepository: TokenRepository = mockk()
    private lateinit var fetcher: FnsReceiptFetcher

    private val sampleQrData = FnsQrData(
        fiscalNumber = "9282440100695643",
        fiscalDocument = "62494",
        fiscalSign = "4892257755",
        sum = 4530.0,
        date = "20250514T1531",
        operationType = 1
    )

    private val gson = Gson()

    @Before
    fun setup() {
        fetcher = FnsReceiptFetcher(api, tokenRepository)
    }

    @Test
    fun `fetch returns Unauthorized when token not set`() = runTest {
        every { tokenRepository.getToken() } returns null

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.Unauthorized)
    }

    @Test
    fun `fetch returns Unauthorized when token is blank`() = runTest {
        every { tokenRepository.getToken() } returns "  "

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.Unauthorized)
    }

    @Test
    fun `fetch returns NotFound when code is 0`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 0,
            data = JsonParser.parseString("\"чек не найден\"")
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.NotFound)
    }

    @Test
    fun `fetch returns NotFound when code is 2`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 2
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.NotFound)
    }

    @Test
    fun `fetch returns RateLimited when code is 3`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 3
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.RateLimited)
    }

    @Test
    fun `fetch returns RateLimited when code is 4`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 4
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.RateLimited)
    }

    @Test
    fun `fetch returns NotFound when code is 5`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 5,
            data = JsonParser.parseString("\"ошибка\"")
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.NotFound)
    }

    @Test
    fun `fetch returns Error when code is unknown`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 99
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.Error)
        assertTrue((result as FetchResult.Error).message.contains("99"))
    }

    @Test
    fun `fetch returns Success with mapped items when code is 1`() = runTest {
        val jsonObj = mapOf(
            "json" to mapOf(
                "items" to listOf(
                    mapOf("name" to "Молоко", "price" to 5999, "quantity" to 1.0, "sum" to 5999),
                    mapOf("name" to "Хлеб", "price" to 3500, "quantity" to 2.0, "sum" to 7000)
                ),
                "totalSum" to 12999,
                "dateTime" to "2025-05-14T15:31:00",
                "retailPlace" to "МАГАЗИН",
                "user" to "Покупатель",
                "retailerInn" to "7712345678"
            )
        )

        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 1,
            first = 1,
            data = JsonParser.parseString(gson.toJson(jsonObj))
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.Success)
        val success = result as FetchResult.Success
        assertEquals(2, success.receipt.items.size)
        assertEquals("Молоко", success.receipt.items[0].name)
        assertEquals(59.99, success.receipt.items[0].price, 0.001)
        assertEquals(1.0, success.receipt.items[0].quantity, 0.001)
        assertEquals(59.99, success.receipt.items[0].sum, 0.001)
        assertEquals("Хлеб", success.receipt.items[1].name)
        assertEquals(35.0, success.receipt.items[1].price, 0.001)
        assertEquals(2.0, success.receipt.items[1].quantity, 0.001)
        assertEquals(70.0, success.receipt.items[1].sum, 0.001)
        assertEquals(129.99, success.receipt.totalSum, 0.001)
        assertEquals("2025-05-14T15:31:00", success.receipt.dateTime)
        assertEquals("МАГАЗИН", success.receipt.retailPlace)
        assertEquals("Покупатель", success.receipt.user)
        assertEquals("7712345678", success.receipt.retailerInn)
    }

    @Test
    fun `fetch returns Success with single item`() = runTest {
        val jsonObj = mapOf(
            "json" to mapOf(
                "items" to listOf(
                    mapOf("name" to "Кофе", "price" to 25000, "quantity" to 1.0, "sum" to 25000)
                ),
                "totalSum" to 25000,
                "dateTime" to "2025-01-01T12:00:00"
            )
        )

        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 1,
            first = 1,
            data = JsonParser.parseString(gson.toJson(jsonObj))
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.Success)
        val success = result as FetchResult.Success
        assertEquals(1, success.receipt.items.size)
        assertEquals(250.0, success.receipt.items[0].price, 0.001)
        assertEquals(250.0, success.receipt.totalSum, 0.001)
    }

    @Test
    fun `fetch returns null when no items in response`() = runTest {
        val jsonObj = mapOf(
            "json" to mapOf(
                "items" to emptyList<Map<String, Any>>(),
                "totalSum" to 0
            )
        )

        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 1,
            first = 1,
            data = JsonParser.parseString(gson.toJson(jsonObj))
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.NotFound)
    }

    @Test
    fun `fetch returns null when response code is null`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = null
        )

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.NotFound)
    }

    @Test
    fun `fetcher passes correct parameters to API`() = runTest {
        val jsonObj = mapOf(
            "json" to mapOf(
                "items" to listOf(mapOf("name" to "Item", "price" to 100, "quantity" to 1.0, "sum" to 100)),
                "totalSum" to 100
            )
        )

        every { tokenRepository.getToken() } returns "my-secret-token"

        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } answers {
            val request = firstArg<ProverkachekaRequest>()
            assertEquals("9282440100695643", request.fn)
            assertEquals("62494", request.fd)
            assertEquals("4892257755", request.fp)
            assertEquals(1, request.n)
            assertEquals(4530.0, request.s!!, 0.001)
            assertEquals("20250514T1531", request.t)
            assertEquals("my-secret-token", request.token)
            assertEquals(1, request.qr)
            ProverkachekaResponse(code = 1, first = 1, data = JsonParser.parseString(gson.toJson(jsonObj)))
        }

        val result = fetcher.fetch(sampleQrData)

        assertTrue(result is FetchResult.Success)
    }

    @Test
    fun `fetch returns null when response data is not json object`() = runTest {
        every { tokenRepository.getToken() } returns "valid-token"
        coEvery { api.getCheckInfo(any<ProverkachekaRequest>()) } returns ProverkachekaResponse(
            code = 1,
            first = 1,
            data = null
        )

        val result = fetcher.fetch(sampleQrData)
        assertTrue(result is FetchResult.NotFound)
    }
}
