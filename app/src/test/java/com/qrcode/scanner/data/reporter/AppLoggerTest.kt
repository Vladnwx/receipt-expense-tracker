package com.qrcode.scanner.data.reporter

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

class AppLoggerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var logDir: File

    @Before
    fun setup() {
        logDir = tempFolder.newFolder("logs")
        val logField = AppLogger::class.java.getDeclaredField("logDir")
        logField.isAccessible = true
        logField.set(null, logDir)
    }

    @After
    fun teardown() {
        val logField = AppLogger::class.java.getDeclaredField("logDir")
        logField.isAccessible = true
        logField.set(null, null)
    }

    @Test
    fun `log writes to file`() {
        AppLogger.i("TestTag", "test message")
        val content = AppLogger.getLogText()
        assertTrue(content.contains("[I] TestTag: test message"))
    }

    @Test
    fun `error log writes to file`() {
        AppLogger.e("ErrTag", "error occurred")
        val content = AppLogger.getLogText()
        assertTrue(content.contains("[E] ErrTag: error occurred"))
    }

    @Test
    fun `warning log writes to file`() {
        AppLogger.w("WarnTag", "warning message")
        val content = AppLogger.getLogText()
        assertTrue(content.contains("[W] WarnTag: warning message"))
    }

    @Test
    fun `debug log writes to file`() {
        AppLogger.d("DbgTag", "debug message")
        val content = AppLogger.getLogText()
        assertTrue(content.contains("[D] DbgTag: debug message"))
    }

    @Test
    fun `getErrorLogText returns only error lines`() {
        AppLogger.i("Test", "info message")
        AppLogger.e("Test", "error one")
        AppLogger.w("Test", "warning")
        AppLogger.e("Test", "error two")

        val errors = AppLogger.getErrorLogText()
        assertTrue(errors.contains("error one"))
        assertTrue(errors.contains("error two"))
        assertFalse(errors.contains("info message"))
        assertFalse(errors.contains("warning"))
    }

    @Test
    fun `clearLog removes previous content`() {
        AppLogger.i("Test", "some message")
        AppLogger.clearLog()
        val content = AppLogger.getLogText()
        assertFalse(content.contains("some message"))
        assertTrue(content.contains("Log cleared"))
    }

    @Test
    fun `getLogFile returns file when exists`() {
        AppLogger.i("Test", "msg")
        val file = AppLogger.getLogFile()
        assertNotNull(file)
        assertTrue(file!!.exists())
    }

    @Test
    fun `getLogFile returns null when no logs`() {
        val file = AppLogger.getLogFile()
        assertNull(file)
    }

    @Test
    fun `multiple log entries preserved in order`() {
        AppLogger.i("Test", "first")
        AppLogger.i("Test", "second")
        AppLogger.i("Test", "third")

        val content = AppLogger.getLogText()
        val lines = content.lines().filter { it.isNotBlank() }
        assertTrue(lines[0].contains("first"))
        assertTrue(lines[1].contains("second"))
        assertTrue(lines[2].contains("third"))
    }
}
