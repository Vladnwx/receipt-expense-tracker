package com.vladnwx.receiptexpensetracker.data.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.vladnwx.receiptexpensetracker.ui.reports.DailyTotal
import com.vladnwx.receiptexpensetracker.ui.reports.PieSlice
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtil {

    private fun getExportDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        return dir
    }

    private fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun exportCsv(
        context: Context,
        pieData: List<PieSlice>,
        dailyTotals: List<DailyTotal>,
        periodLabel: String
    ): Uri {
        val file = File(getExportDir(context), "report_${System.currentTimeMillis()}.csv")
        val sb = StringBuilder()
        sb.appendLine("Отчёт за $periodLabel")
        sb.appendLine()
        sb.appendLine("Категория,Сумма")
        pieData.forEach { sb.appendLine("${it.label},${String.format("%.2f", it.amount)}") }
        sb.appendLine()
        sb.appendLine("Дата,Расходы,Доходы")
        dailyTotals.forEach { sb.appendLine("${it.date},${String.format("%.2f", it.expense)},${String.format("%.2f", it.income)}") }
        file.writeText(sb.toString())
        return getShareUri(context, file)
    }

    fun exportPdf(
        context: Context,
        pieData: List<PieSlice>,
        dailyTotals: List<DailyTotal>,
        periodLabel: String
    ): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 24f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val textPaint = Paint().apply { textSize = 12f }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        var y = 40f
        canvas.drawText("Отчёт за $periodLabel", 40f, y, titlePaint)
        y += 30f
        canvas.drawText("Сформирован: ${dateFormat.format(Date())}", 40f, y, textPaint)
        y += 30f

        canvas.drawText("Категории:", 40f, y, headerPaint)
        y += 20f
        for (s in pieData) {
            canvas.drawText("${s.label}: ${String.format("%.2f", s.amount)} ₽", 60f, y, textPaint)
            y += 18f
        }

        y += 20f
        canvas.drawText("Динамика:", 40f, y, headerPaint)
        y += 20f
        for (d in dailyTotals) {
            canvas.drawText("${d.date}: расход ${String.format("%.2f", d.expense)} ₽ / доход ${String.format("%.2f", d.income)} ₽", 60f, y, textPaint)
            y += 18f
        }

        document.finishPage(page)
        val file = File(getExportDir(context), "report_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return getShareUri(context, file)
    }

    fun exportExcel(
        context: Context,
        pieData: List<PieSlice>,
        dailyTotals: List<DailyTotal>,
        periodLabel: String
    ): Uri {
        val file = File(getExportDir(context), "report_${System.currentTimeMillis()}.xls")
        val sb = StringBuilder()
        sb.appendLine("<html><head><meta charset='utf-8'></head><body>")
        sb.appendLine("<h2>Отчёт за $periodLabel</h2>")
        sb.appendLine("<h3>Категории</h3><table border='1'><tr><th>Категория</th><th>Сумма</th></tr>")
        pieData.forEach { sb.appendLine("<tr><td>${it.label}</td><td>${String.format("%.2f", it.amount)}</td></tr>") }
        sb.appendLine("</table>")
        sb.appendLine("<h3>Динамика</h3><table border='1'><tr><th>Дата</th><th>Расходы</th><th>Доходы</th></tr>")
        dailyTotals.forEach { sb.appendLine("<tr><td>${it.date}</td><td>${String.format("%.2f", it.expense)}</td><td>${String.format("%.2f", it.income)}</td></tr>") }
        sb.appendLine("</table></body></html>")
        file.writeText(sb.toString())
        return getShareUri(context, file)
    }
}
