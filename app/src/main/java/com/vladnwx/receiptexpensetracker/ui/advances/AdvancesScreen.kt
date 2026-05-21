package com.vladnwx.receiptexpensetracker.ui.advances

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceStatus
import com.vladnwx.receiptexpensetracker.data.local.entity.AdvanceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancesScreen(viewModel: AdvancesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Создать аванс")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Авансовые отчёты", style = MaterialTheme.typography.headlineMedium)
            }

            val openReports = state.reports.filter { it.report.status == AdvanceStatus.OPEN }
            val closedReports = state.reports.filter { it.report.status == AdvanceStatus.CLOSED }

            if (openReports.isEmpty() && closedReports.isEmpty()) {
                item {
                    Text("Нет авансовых отчётов",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp))
                }
            }

            if (openReports.isNotEmpty()) {
                item {
                    Text("Открытые", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                items(openReports) { rw ->
                    AdvanceCard(rw = rw, dateFormat = dateFormat,
                        onClose = { viewModel.closeReport(rw.report.id) },
                        onDelete = { viewModel.deleteReport(rw.report.id) })
                }
            }

            if (closedReports.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Закрытые", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                items(closedReports) { rw ->
                    AdvanceCard(rw = rw, dateFormat = dateFormat,
                        onClose = null, onDelete = { viewModel.deleteReport(rw.report.id) })
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateAdvanceDialog(
            contacts = state.contacts,
            accounts = state.accounts,
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { contactId, accountId, amount, type, purpose, dueDate ->
                viewModel.createReport(contactId, accountId, amount, type, purpose, dueDate)
            }
        )
    }
}

@Composable
private fun AdvanceCard(
    rw: ReportWithContact,
    dateFormat: SimpleDateFormat,
    onClose: (() -> Unit)?,
    onDelete: () -> Unit
) {
    val r = rw.report
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(rw.contact?.name ?: "Контрагент", fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text(if (r.type == AdvanceType.ADVANCE) "Под отчёт" else "Возмещение",
                    color = if (r.type == AdvanceType.ADVANCE) Color(0xFFFF9800) else Color(0xFF2196F3))
            }
            Text("Сумма: ${String.format("%.2f", r.amount)} ₽")
            Text("Потрачено: ${String.format("%.2f", rw.spent)} ₽")
            Text("Остаток: ${String.format("%.2f", r.amount - rw.spent)} ₽")
            if (r.purpose != null) Text("Назначение: ${r.purpose}")
            Text("Дата: ${dateFormat.format(Date(r.date))}", style = MaterialTheme.typography.bodySmall)
            if (r.dueDate != null) {
                Text("Срок: ${dateFormat.format(Date(r.dueDate))}", style = MaterialTheme.typography.bodySmall)
            }
            if (r.status == AdvanceStatus.CLOSED && r.closedAt != null) {
                Text("Закрыт: ${dateFormat.format(Date(r.closedAt))}",
                    style = MaterialTheme.typography.bodySmall)
            }
            if (r.status == AdvanceStatus.OPEN && rw.spent >= r.amount) {
                Text("Весь аванс потрачен", color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onClose != null) {
                    Button(onClick = onClose) { Text("Закрыть") }
                }
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAdvanceDialog(
    contacts: List<com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity>,
    accounts: List<com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity>,
    onDismiss: () -> Unit,
    onCreate: (Long, Long, Double, AdvanceType, String, Long?) -> Unit
) {
    var selectedContact by remember { mutableStateOf<Long?>(null) }
    var selectedAccount by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var advanceType by remember { mutableStateOf(AdvanceType.ADVANCE) }
    var contactExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый авансовый отчёт") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { advanceType = AdvanceType.ADVANCE }) {
                        Text("Под отчёт",
                            fontWeight = if (advanceType == AdvanceType.ADVANCE) FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(onClick = { advanceType = AdvanceType.REIMBURSEMENT }) {
                        Text("Возмещение",
                            fontWeight = if (advanceType == AdvanceType.REIMBURSEMENT) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = contactExpanded,
                    onExpandedChange = { contactExpanded = !contactExpanded }
                ) {
                    OutlinedTextField(
                        value = contacts.find { it.id == selectedContact }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Контрагент") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = contactExpanded, onDismissRequest = { contactExpanded = false }) {
                        contacts.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = { selectedContact = c.id; contactExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccount }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Счёт") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                        accounts.forEach { a ->
                            DropdownMenuItem(
                                text = { Text("${a.name} (${String.format("%.2f", a.initialBalance)} ${a.currency})") },
                                onClick = { selectedAccount = a.id; accountExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Сумма") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Назначение") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.replace(",", ".").toDoubleOrNull() ?: return@TextButton
                    val cId = selectedContact ?: return@TextButton
                    val aId = selectedAccount ?: return@TextButton
                    onCreate(cId, aId, amt, advanceType, purpose, null)
                },
                enabled = amount.toDoubleOrNull() != null && selectedContact != null && selectedAccount != null
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
