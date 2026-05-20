package com.vladnwx.receiptexpensetracker.ui.debts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.DebtType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: DebtViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { viewModel.showCreateDialog(DebtType.LEND) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("💳 Дать в долг", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
            item {
                Button(
                    onClick = { viewModel.showCreateDialog(DebtType.BORROW) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("💰 Взять в долг", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }

            if (state.activeDebts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Активные долги", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(state.activeDebts, key = { it.id }) { debt ->
                    DebtCard(debt = debt, onClose = { viewModel.closeDebt(debt.id) })
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateDebtDialog(
            type = state.createType,
            contacts = state.contacts,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { contactId, amount, dueDate, desc ->
                viewModel.createDebt(contactId, amount, dueDate, desc)
            }
        )
    }
}

@Composable
private fun DebtCard(debt: DebtEntity, onClose: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = if (debt.type == DebtType.LEND) MaterialTheme.colorScheme.primaryContainer
                         else MaterialTheme.colorScheme.tertiaryContainer
    )) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (debt.type == DebtType.LEND) "Должен мне" else "Я должен",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClose) { Text("Закрыть") }
            }
            Text("Сумма: ${fmt(debt.amount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            debt.dueDate?.let { Text("Срок: ${fmtDate(it)}", style = MaterialTheme.typography.bodySmall) }
            debt.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDebtDialog(
    type: DebtType,
    contacts: List<com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity>,
    onDismiss: () -> Unit,
    onSave: (Long, Double, Long?, String?) -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == DebtType.LEND) "Дать в долг" else "Взять в долг") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Контакт") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = if (dueDate != null) fmtDate(dueDate!!) else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Срок возврата") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                TextButton(onClick = { showDatePicker = true }) { Text("Выбрать дату") }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val contactId = if (contacts.isNotEmpty()) contacts.first().id else 0L
                    val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    onSave(contactId, amount, dueDate, description.ifBlank { null })
                },
                enabled = contactName.isNotBlank() && amountText.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (showDatePicker) {
        val picker = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = {
                picker.selectedDateMillis?.let { dueDate = it }
                showDatePicker = false
            }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) { DatePicker(state = picker) }
    }
}

private fun fmt(amount: Double): String {
    val w = kotlin.math.abs(amount).toLong()
    return "$w,00 ₽"
}

private fun fmtDate(millis: Long): String {
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))
}
