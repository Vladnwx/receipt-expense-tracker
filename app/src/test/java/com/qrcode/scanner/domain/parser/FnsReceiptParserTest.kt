package com.qrcode.scanner.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FnsReceiptParserTest {

    private lateinit var parser: FnsReceiptParser

    @Before
    fun setup() {
        parser = FnsReceiptParser()
    }

    @Test
    fun `parse returns null for empty string`() {
        assertNull(parser.parse(""))
    }

    @Test
    fun `parse returns null for random text`() {
        assertNull(parser.parse("some random text without qr data"))
    }

    @Test
    fun `parse returns null for whitespace`() {
        assertNull(parser.parse("   "))
    }

    @Test
    fun `parse url format with all params`() {
        val url = "https://check.ofd.ru/check?fn=1234567890123456&i=1001&fp=9876543210&s=450.50&t=20250101T1200&n=1"
        val result = parser.parse(url)
        assertNotNull(result)
        assertEquals("1234567890123456", result!!.fiscalNumber)
        assertEquals("1001", result.fiscalDocument)
        assertEquals("9876543210", result.fiscalSign)
        assertEquals(450.50, result.sum!!, 0.001)
        assertEquals("20250101T1200", result.date)
        assertEquals(1, result.operationType)
        assertEquals(url, result.rawUrl)
    }

    @Test
    fun `parse url format http`() {
        val url = "http://example.com/qr?fn=1111111111111111&i=222&fp=333&s=100.00&t=20240101T0000"
        val result = parser.parse(url)
        assertNotNull(result)
        assertEquals("1111111111111111", result!!.fiscalNumber)
    }

    @Test
    fun `parse url format missing fn returns null`() {
        assertNull(parser.parse("https://check.ofd.ru/check?i=1001&fp=9876543210"))
    }

    @Test
    fun `parse url format missing i returns null`() {
        assertNull(parser.parse("https://check.ofd.ru/check?fn=1234567890123456&fp=9876543210"))
    }

    @Test
    fun `parse data format with all params`() {
        val data = "t=20250101T1200&s=450.50&fn=1234567890123456&i=1001&fp=9876543210&n=1"
        val result = parser.parse(data)
        assertNotNull(result)
        assertEquals("1234567890123456", result!!.fiscalNumber)
        assertEquals("1001", result.fiscalDocument)
        assertEquals("9876543210", result.fiscalSign)
        assertEquals(450.50, result.sum!!, 0.001)
        assertEquals("20250101T1200", result.date)
        assertEquals(1, result.operationType)
    }

    @Test
    fun `parse data format without t`() {
        val data = "s=200.00&fn=1234567890123456&i=1001&fp=9876543210&n=1"
        val result = parser.parse(data)
        assertNotNull(result)
        assertEquals("1234567890123456", result!!.fiscalNumber)
        assertEquals("1001", result.fiscalDocument)
        assertEquals("9876543210", result.fiscalSign)
        assertEquals(200.00, result.sum!!, 0.001)
        assertNull(result.date)
        assertEquals(1, result.operationType)
    }

    @Test
    fun `parse data format without n defaults to 1`() {
        val data = "s=300.00&fn=1234567890123456&i=1001&fp=9876543210"
        val result = parser.parse(data)
        assertNotNull(result)
        assertEquals(1, result!!.operationType)
    }

    @Test
    fun `parse data format missing fn returns null`() {
        assertNull(parser.parse("s=100.00&i=1001&fp=9876543210"))
    }

    @Test
    fun `parse data format missing i returns null`() {
        assertNull(parser.parse("s=100.00&fn=1234567890123456&fp=9876543210"))
    }

    @Test
    fun `parse url with extra params after`() {
        val url = "https://check.ofd.ru/check?fn=1234567890123456&i=1001&fp=9876543210&s=100&extra=foo"
        val result = parser.parse(url)
        assertNotNull(result)
        assertEquals("1001", result!!.fiscalDocument)
        assertEquals(100.0, result.sum!!, 0.001)
    }

    @Test
    fun `detectFormat returns url for http`() {
        assertEquals("url", parser.detectFormat("https://example.com/qr?fn=123"))
    }

    @Test
    fun `detectFormat returns data for inline`() {
        assertEquals("data", parser.detectFormat("s=100&fn=123&i=456&fp=789"))
    }

    @Test
    fun `detectFormat returns unknown for gibberish`() {
        assertEquals("unknown", parser.detectFormat("gibberish"))
    }

    @Test
    fun `parse real world url example`() {
        val url = "https://qr.nspk.ru/AD10001LKFJM4C1D89GG0E4C8I7D5T6I?type=01&bank=100000000007&sum=4530000&cur=643&tt=12&ff=20250514T1531&to=proverkacheka.com&fn=9282440100695643&i=62494&fp=4892257755&n=0"
        val result = parser.parse(url)
        assertNotNull(result)
        assertEquals("9282440100695643", result!!.fiscalNumber)
        assertEquals("62494", result.fiscalDocument)
        assertEquals("4892257755", result.fiscalSign)
        assertEquals(0, result.operationType)
    }
}
