package com.vladnwx.receiptexpensetracker.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountType

private val accountColors = listOf(
    0xFF1B5E20.toInt(), 0xFF2E7D32.toInt(), 0xFF388E3C.toInt(), 0xFF43A047.toInt(),
    0xFF1565C0.toInt(), 0xFF1976D2.toInt(), 0xFF1E88E5.toInt(), 0xFF42A5F5.toInt(),
    0xFFE65100.toInt(), 0xFFEF6C00.toInt(), 0xFFF57C00.toInt(), 0xFFFB8C00.toInt(),
    0xFF6A1B9A.toInt(), 0xFF7B1FA2.toInt(), 0xFF8E24AA.toInt(), 0xFFAB47BC.toInt(),
    0xFFC62828.toInt(), 0xFFD32F2F.toInt(), 0xFFE53935.toInt(), 0xFFEF5350.toInt(),
    0xFF37474F.toInt(), 0xFF546E7A.toInt(), 0xFF78909C.toInt(), 0xFF90A4AE.toInt(),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerGrid(selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        accountColors.forEach { color ->
            val isSelected = color == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(color) }
            )
        }
    }
}

@Composable
fun AccountsScreen(viewModel: AccountViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить счёт")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.accounts, key = { it.id }) { account ->
                AccountCard(
                    account = account,
                    onClick = { viewModel.showEditDialog(account) },
                    onDelete = { viewModel.delete(account) }
                )
            }
        }
    }

    if (state.showDialog) {
        AccountDialog(
            editing = state.editingAccount,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { name, type, currency, balance, color, sortOrder, budget ->
                viewModel.save(name, type, currency, balance, color, sortOrder, budget)
            }
        )
    }
}

@Composable
private fun AccountCard(account: AccountEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(account.color))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (account.type) {
                        AccountType.CASH -> "Наличные"
                        AccountType.CARD -> "Карта"
                        AccountType.CREDIT_CARD -> "Кредитная карта"
                        AccountType.SAVINGS -> "Накопительный"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatAmount(account.initialBalance, account.currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    editing: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (String, AccountType, String, Double, Int, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var selectedType by remember { mutableStateOf(editing?.type ?: AccountType.CARD) }
    var currency by remember { mutableStateOf(editing?.currency ?: "RUB") }
    var balanceText by remember { mutableStateOf(if (editing != null) formatRaw(editing.initialBalance) else "") }
    var selectedColor by remember { mutableStateOf(editing?.color ?: accountColors[0]) }
    var sortOrderText by remember { mutableStateOf(editing?.sortOrder?.toString() ?: "0") }
    var typeExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val isEditing = editing != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Редактировать счёт" else "Новый счёт") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when (selectedType) {
                            AccountType.CASH -> "Наличные"
                            AccountType.CARD -> "Карта"
                            AccountType.CREDIT_CARD -> "Кредитная карта"
                            AccountType.SAVINGS -> "Накопительный"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        AccountType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(when (type) {
                                    AccountType.CASH -> "Наличные"
                                    AccountType.CARD -> "Карта"
                                    AccountType.CREDIT_CARD -> "Кредитная карта"
                                    AccountType.SAVINGS -> "Накопительный"
                                }) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Валюта") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        listOf("RUB", "USD", "EUR", "CNY").forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    currency = code
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Начальный баланс") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sortOrderText,
                    onValueChange = { sortOrderText = it },
                    label = { Text("Сортировка") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Цвет", style = MaterialTheme.typography.labelLarge)
                ColorPickerGrid(selected = selectedColor) { selectedColor = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val sortOrder = sortOrderText.toIntOrNull() ?: 0
                    onSave(name, selectedType, currency, balance, selectedColor, sortOrder, true)
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun formatAmount(amount: Double, currency: String): String {
    val sign = if (amount < 0) "-" else ""
    val abs = kotlin.math.abs(amount)
    val whole = abs.toLong()
    val frac = ((abs - whole) * 100).toLong()
    return "$sign$whole,${frac.toString().padStart(2, '0')} $currency"
}

private fun formatRaw(amount: Double): String {
    val whole = amount.toLong()
    val frac = ((amount - whole) * 100).toLong()
    return "$whole,${frac.toString().padStart(2, '0')}"
}
