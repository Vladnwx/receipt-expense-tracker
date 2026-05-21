package com.vladnwx.receiptexpensetracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladnwx.receiptexpensetracker.data.local.dao.CategoryDao
import com.vladnwx.receiptexpensetracker.data.local.dao.ExpenseDao
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.ExpenseEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PieSlice(
    val label: String,
    val amount: Double,
    val color: Int
)

data class DailyTotal(
    val date: String,
    val expense: Double,
    val income: Double
)

enum class ReportPeriod(val label: String, val days: Int) {
    WEEK("Неделя", 7),
    MONTH("Месяц", 30),
    QUARTER("Квартал", 90),
    YEAR("Год", 365)
}

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.MONTH,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val pieData: List<PieSlice> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val categories: Map<Long, CategoryEntity> = emptyMap(),
    val loading: Boolean = false
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun setPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val categories: Map<Long, CategoryEntity> = categoryDao.getAll().associateBy { it.id }
            val period = _state.value.period

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -period.days)
            val start = cal.timeInMillis
            val end = System.currentTimeMillis()

            val expenses: List<ExpenseEntity> = expenseDao.getExpensesByTypeAndDateRange(OperationType.EXPENSE, start, end)
            val incomes: List<ExpenseEntity> = expenseDao.getExpensesByTypeAndDateRange(OperationType.INCOME, start, end)

            val totalExpense = expenses.sumOf { e: ExpenseEntity -> e.amount }
            val totalIncome = incomes.sumOf { e: ExpenseEntity -> e.amount }

            val categoryTotals: Map<Long?, Double> = expenses
                .groupBy { e: ExpenseEntity -> e.categoryId }
                .mapValues { (_, exps: List<ExpenseEntity>) -> exps.sumOf { e: ExpenseEntity -> e.amount } }

            val pieData: List<PieSlice> = categoryTotals.map { (catId: Long?, amount: Double) ->
                val cat = categories[catId]
                PieSlice(
                    label = cat?.name ?: "Без категории",
                    amount = amount,
                    color = cat?.color ?: 0
                )
            }.sortedByDescending { it.amount }

            val dailyMap = mutableMapOf<String, DailyTotal>()
            val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

            for (e in expenses) {
                val key = dateFormat.format(Date(e.date))
                val prev = dailyMap[key]
                dailyMap[key] = DailyTotal(key, (prev?.expense ?: 0.0) + e.amount, prev?.income ?: 0.0)
            }
            for (e in incomes) {
                val key = dateFormat.format(Date(e.date))
                val prev = dailyMap[key]
                dailyMap[key] = DailyTotal(key, prev?.expense ?: 0.0, (prev?.income ?: 0.0) + e.amount)
            }

            _state.value = _state.value.copy(
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                pieData = pieData,
                dailyTotals = dailyMap.values.sortedBy { it.date },
                categories = categories,
                loading = false
            )
        }
    }
}
