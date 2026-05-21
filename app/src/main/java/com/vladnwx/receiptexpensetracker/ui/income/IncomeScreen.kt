package com.vladnwx.receiptexpensetracker.ui.income

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import com.vladnwx.receiptexpensetracker.ui.expense.ExpenseScreen
import com.vladnwx.receiptexpensetracker.ui.expense.ExpenseViewModel

@Composable
fun IncomeScreen() {
    val vm: ExpenseViewModel = hiltViewModel()
    vm.configure(OperationType.INCOME)
    ExpenseScreen(viewModel = vm)
}
