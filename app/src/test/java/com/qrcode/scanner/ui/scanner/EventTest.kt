package com.qrcode.scanner.ui.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventTest {

    @Test
    fun `getContentIfNotHandled returns content on first call`() {
        val event = Event("hello")
        assertEquals("hello", event.getContentIfNotHandled())
    }

    @Test
    fun `getContentIfNotHandled returns null on second call`() {
        val event = Event("hello")
        event.getContentIfNotHandled()
        assertNull(event.getContentIfNotHandled())
    }

    @Test
    fun `getContentIfNotHandled returns null on multiple calls`() {
        val event = Event(42)
        assertEquals(42, event.getContentIfNotHandled())
        assertNull(event.getContentIfNotHandled())
        assertNull(event.getContentIfNotHandled())
        assertNull(event.getContentIfNotHandled())
    }

    @Test
    fun `different Event instances are independent`() {
        val event1 = Event("first")
        val event2 = Event("second")

        assertEquals("first", event1.getContentIfNotHandled())
        assertEquals("second", event2.getContentIfNotHandled())

        assertNull(event1.getContentIfNotHandled())
        assertNull(event2.getContentIfNotHandled())
    }

    @Test
    fun `works with null content`() {
        val event = Event<String?>(null)
        assertNull(event.getContentIfNotHandled())
    }

    @Test
    fun `works with custom objects`() {
        data class TestData(val id: Int, val name: String)
        val obj = TestData(1, "test")
        val event = Event(obj)
        assertEquals(obj, event.getContentIfNotHandled())
        assertNull(event.getContentIfNotHandled())
    }
}
