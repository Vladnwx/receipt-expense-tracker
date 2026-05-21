package com.vladnwx.receiptexpensetracker.data.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.util.Calendar

object CalendarHelper {

    fun addEvent(
        context: Context,
        title: String,
        description: String?,
        eventDate: Long,
        reminderMinutes: Int = 30
    ): Result<Unit> = runCatching {
        val cal = Calendar.getInstance().apply { timeInMillis = eventDate }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = eventDate
            add(Calendar.HOUR_OF_DAY, 1)
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, getCalendarId(context))
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description ?: "")
            put(CalendarContract.Events.DTSTART, cal.timeInMillis)
            put(CalendarContract.Events.DTEND, endCal.timeInMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
            put(CalendarContract.Events.ALL_DAY, 0)
        }

        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        val eventId = uri?.lastPathSegment?.toLongOrNull()

        if (eventId != null && reminderMinutes > 0) {
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, reminderMinutes)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }
    }

    private fun getCalendarId(context: Context): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) it.getLong(0)
            else throw Exception("No visible calendar found")
        } ?: throw Exception("No visible calendar found")
    }

    fun hasCalendarPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }
}
