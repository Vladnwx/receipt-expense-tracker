package com.vladnwx.receiptexpensetracker.ui.income

import androidx.compose.runtime.Composable
import com.vladnwx.receiptexpensetracker.ui.expense.ExpenseScreen

@Composable
fun IncomeScreen() {
    ExpenseScreen(isIncome = true)
}
