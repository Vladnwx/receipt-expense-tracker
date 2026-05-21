package com.vladnwx.receiptexpensetracker.ui.reports

import android.content.Intent
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.util.ExportUtil

private val presetColors = listOf(
    0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFFFF9800.toInt(),
    0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF00BCD4.toInt(),
    0xFFFF5722.toInt(), 0xFF607D8B.toInt(), 0xFF795548.toInt(),
    0xFF8BC34A.toInt(), 0xFF03A9F4.toInt(), 0xFFFFC107.toInt()
)

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Отчёты", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ReportPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.setPeriod(period) },
                        label = { Text(period.label) }
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Доходы", style = MaterialTheme.typography.bodySmall)
                        Text("${String.format("%.2f", state.totalIncome)} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF4CAF50))
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Расходы", style = MaterialTheme.typography.bodySmall)
                        Text("${String.format("%.2f", state.totalExpense)} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFE53935))
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Баланс", style = MaterialTheme.typography.bodySmall)
                        val balance = state.totalIncome - state.totalExpense
                        Text("${String.format("%.2f", balance)} ₽",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFE53935))
                    }
                }
            }
        }

        if (state.pieData.isNotEmpty()) {
            item {
                Text("По категориям", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PieChart(
                            slices = state.pieData,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        state.pieData.forEach { slice ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(12.dp).padding(end = 8.dp)) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(Color(slice.color))
                                    }
                                }
                                Text(slice.label, modifier = Modifier.weight(1f))
                                Text("${String.format("%.2f", slice.amount)} ₽",
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (state.dailyTotals.isNotEmpty()) {
            item {
                Text("Динамика", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    LineChart(
                        totals = state.dailyTotals,
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Экспорт", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    val uri = ExportUtil.exportPdf(context, state.pieData, state.dailyTotals, state.period.label)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Отправить PDF"))
                }, modifier = Modifier.weight(1f)) {
                    Text("PDF")
                }
                Button(onClick = {
                    val uri = ExportUtil.exportCsv(context, state.pieData, state.dailyTotals, state.period.label)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Отправить CSV"))
                }, modifier = Modifier.weight(1f)) {
                    Text("CSV")
                }
                Button(onClick = {
                    val uri = ExportUtil.exportExcel(context, state.pieData, state.dailyTotals, state.period.label)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.ms-excel"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Отправить Excel"))
                }, modifier = Modifier.weight(1f)) {
                    Text("Excel")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.amount }
    if (total <= 0) return

    Canvas(modifier = modifier) {
        val strokeWidth = 40f
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val diameter = radius * 2 + strokeWidth

        var startAngle = -90f
        slices.forEachIndexed { index, slice ->
            val sweepAngle = (slice.amount / total * 360f).toFloat()
            val color = if (slice.color != 0) Color(slice.color)
                        else Color(presetColors[index % presetColors.size])

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LineChart(
    totals: List<DailyTotal>,
    modifier: Modifier = Modifier
) {
    if (totals.isEmpty()) return
    val maxVal = maxOf(totals.maxOf { it.expense }, totals.maxOf { it.income }, 1.0)

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height - 30f
        val stepX = if (totals.size > 1) chartWidth / (totals.size - 1) else chartWidth

        drawIntoCanvas { cv ->
            val paint = Paint().apply {
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }

            totals.forEachIndexed { i, total ->
                val x = i * stepX
                val expY = chartHeight - (total.expense / maxVal * chartHeight).toFloat()
                val incY = chartHeight - (total.income / maxVal * chartHeight).toFloat()

                if (total.expense > 0) {
                    cv.nativeCanvas.drawCircle(x, expY, 4f, Paint().apply {
                        color = 0xFFE53935.toInt()
                        setStyle(Paint.Style.FILL)
                    })
                }
                if (total.income > 0) {
                    cv.nativeCanvas.drawCircle(x, incY, 4f, Paint().apply {
                        color = 0xFF4CAF50.toInt()
                        setStyle(Paint.Style.FILL)
                    })
                }

                if (i % 3 == 0 || i == totals.size - 1) {
                    cv.nativeCanvas.drawText(total.date, x, chartHeight + 20f, paint)
                }
            }
        }
    }
}
