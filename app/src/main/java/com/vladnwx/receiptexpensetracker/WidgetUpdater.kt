package com.vladnwx.receiptexpensetracker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdater {
    fun updateBalance(context: Context, balance: Double) {
        context.getSharedPreferences("widget", Context.MODE_PRIVATE)
            .edit()
            .putFloat("balance", balance.toFloat())
            .apply()

        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, WidgetProvider::class.java)
        )
        for (id in ids) {
            WidgetProvider.updateWidget(context, manager, id)
        }
    }
}
