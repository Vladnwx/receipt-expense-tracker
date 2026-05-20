package com.vladnwx.receiptexpensetracker.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(viewModel: TransferViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var showFromDialog by remember { mutableStateOf(false) }
    var showToDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) viewModel.reset() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.fromAccount?.let { "${it.name} (${fmt(it.initialBalance, it.currency)})" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Счёт-отправитель") },
                modifier = Modifier.fillMaxWidth().clickable { showFromDialog = true },
                enabled = false
            )

            OutlinedTextField(
                value = state.toAccount?.let { "${it.name} (${fmt(it.initialBalance, it.currency)})" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Счёт-получатель") },
                modifier = Modifier.fillMaxWidth().clickable { showToDialog = true },
                enabled = false
            )

            OutlinedTextField(
                value = state.amountText,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = { Text("Сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.commissionText,
                onValueChange = { viewModel.onCommissionChanged(it) },
                label = { Text("Комиссия") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.rateText,
                onValueChange = { viewModel.onRateChanged(it) },
                label = { Text("Курс") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fmtDate(state.dateMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата") },
                modifier = Modifier.fillMaxWidth().clickable { showDateDialog = true },
                enabled = false
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Описание") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state.fromAccount != null && state.toAccount != null && state.amountText.isNotBlank()
            ) {
                Text("Сохранить", fontSize = 16.sp)
            }
        }
    }

    if (showFromDialog) AccountDialog(
        accounts = state.accounts.filter { it.id != state.toAccount?.id },
        onSelect = { viewModel.onFromAccountSelected(it); showFromDialog = false },
        onDismiss = { showFromDialog = false }
    )

    if (showToDialog) AccountDialog(
        accounts = state.accounts.filter { it.id != state.fromAccount?.id },
        onSelect = { viewModel.onToAccountSelected(it); showToDialog = false },
        onDismiss = { showToDialog = false }
    )

    if (showDateDialog) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = { TextButton(onClick = {
                picker.selectedDateMillis?.let { viewModel.onDateChanged(it) }
                showDateDialog = false
            }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDateDialog = false }) { Text("Отмена") } }
        ) { DatePicker(state = picker) }
    }
}

@Composable
private fun AccountDialog(
    accounts: List<AccountEntity>,
    onSelect: (AccountEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите счёт") },
        text = {
            Column {
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(acc) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(acc.color)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) { Text(acc.name) }
                        Text(fmt(acc.initialBalance, acc.currency), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

private fun fmt(amount: Double, cur: String): String {
    val w = kotlin.math.abs(amount).toLong()
    return "$w,00 $cur"
}

private fun fmtDate(millis: Long): String {
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))
}
