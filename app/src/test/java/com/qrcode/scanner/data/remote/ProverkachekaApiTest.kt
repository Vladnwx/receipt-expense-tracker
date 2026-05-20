package com.qrcode.scanner.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProverkachekaApiTest {

    private val gson = Gson()

    @Test
    fun `deserialize success response`() {
        val json = """
            {
                "code": 1,
                "first": 1,
                "data": {
                    "json": {
                        "items": [
                            {"name": "Товар", "price": 10000, "quantity": 2.0, "sum": 20000}
                        ],
                        "totalSum": 20000,
                        "dateTime": "2025-05-14T15:31:00",
                        "retailPlace": "Магазин",
                        "user": "Иван",
                        "retailerInn": "7712345678",
                        "operationType": 1
                    }
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ProverkachekaResponse::class.java)

        assertEquals(1, response.code)
        assertEquals(1, response.first)
        assertNotNull(response.data)

        val dataObj = response.data?.asJsonObject
        assertNotNull(dataObj)
        val jsonEl = dataObj?.get("json")
        assertNotNull(jsonEl)
        val pkJson = gson.fromJson(jsonEl, ProverkachekaJson::class.java)

        assertNotNull(pkJson)
        assertEquals(1, pkJson.items?.size)
        assertEquals("Товар", pkJson.items?.get(0)?.name)
        assertEquals(10000L, pkJson.items?.get(0)?.price)
        assertEquals(2.0, pkJson.items!![0].quantity!!, 0.001)
        assertEquals(20000L, pkJson.items?.get(0)?.sum)
        assertEquals(20000L, pkJson.totalSum)
        assertEquals("2025-05-14T15:31:00", pkJson.dateTime)
        assertEquals("Магазин", pkJson.retailPlace)
    }

    @Test
    fun `deserialize error response`() {
        val json = """
            {
                "code": 401,
                "data": "Не авторизован"
            }
        """.trimIndent()

        val response = gson.fromJson(json, ProverkachekaResponse::class.java)

        assertEquals(401, response.code)
        assertNull(response.first)
        assertNotNull(response.data)
        assertTrue(response.data?.isJsonPrimitive == true)
        assertEquals("Не авторизован", response.data?.asJsonPrimitive?.asString)
    }

    @Test
    fun `deserialize rate limited response`() {
        val json = """
            {
                "code": 3,
                "first": 299
            }
        """.trimIndent()

        val response = gson.fromJson(json, ProverkachekaResponse::class.java)

        assertEquals(3, response.code)
        assertEquals(299, response.first)
        assertNull(response.data)
    }

    @Test
    fun `deserialize code 0 invalid receipt`() {
        val json = """
            {
                "code": 0,
                "data": "чек некорректен"
            }
        """.trimIndent()

        val response = gson.fromJson(json, ProverkachekaResponse::class.java)

        assertEquals(0, response.code)
        assertEquals("чек некорректен", response.data?.asJsonPrimitive?.asString)
    }

    @Test
    fun `deserialize empty items list`() {
        val json = """
            {
                "code": 1,
                "first": 1,
                "data": {
                    "json": {
                        "items": [],
                        "totalSum": 0
                    }
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ProverkachekaResponse::class.java)
        val dataObj = response.data?.asJsonObject
        val pkJson = gson.fromJson(dataObj?.get("json"), ProverkachekaJson::class.java)

        assertNotNull(pkJson)
        assertTrue(pkJson?.items?.isEmpty() == true)
        assertEquals(0L, pkJson?.totalSum)
    }

    @Test
    fun `serialize request body`() {
        val request = ProverkachekaRequest(
            fn = "9282440100695643",
            fd = "62494",
            fp = "4892257755",
            n = 1,
            s = 4530.0,
            t = "20250514T1531",
            token = "my-token",
            qr = 1
        )

        val json = gson.toJson(request)
        val parsed = JsonParser.parseString(json).asJsonObject

        assertEquals("9282440100695643", parsed.get("fn")!!.asString)
        assertEquals("62494", parsed.get("fd")!!.asString)
        assertEquals("4892257755", parsed.get("fp")!!.asString)
        assertEquals(1, parsed.get("n")!!.asInt)
        assertEquals(4530.0, parsed.get("s")!!.asDouble, 0.001)
        assertEquals("20250514T1531", parsed.get("t")!!.asString)
        assertEquals("my-token", parsed.get("token")!!.asString)
        assertEquals(1, parsed.get("qr")!!.asInt)
    }

    @Test
    fun `items with null fields deserialize gracefully`() {
        val json = """
            {
                "code": 1,
                "first": 1,
                "data": {
                    "json": {
                        "items": [
                            {"name": null, "price": null, "quantity": null, "sum": null}
                        ],
                        "totalSum": null
                    }
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, ProverkachekaResponse::class.java)
        val dataObj = response.data?.asJsonObject
        val pkJson = gson.fromJson(dataObj?.get("json"), ProverkachekaJson::class.java)

        assertNotNull(pkJson)
        assertNull(pkJson?.items?.get(0)?.name)
        assertNull(pkJson?.items?.get(0)?.price)
        assertNull(pkJson?.items?.get(0)?.quantity)
        assertNull(pkJson?.totalSum)
    }
}
