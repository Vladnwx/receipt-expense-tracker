package com.vladnwx.receiptexpensetracker.ui.expense

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.AccountEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }
    var showFamilySheet by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "receipt_${System.currentTimeMillis()}.jpg"
                val outputFile = java.io.File(context.filesDir, fileName)
                inputStream?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.setAttachment(outputFile.absolutePath, fileName)
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.resetForm()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (viewModel.operationType == OperationType.INCOME) "+" else "−",
                style = MaterialTheme.typography.displaySmall,
                color = if (viewModel.operationType == OperationType.INCOME) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = state.amountText,
                onValueChange = { viewModel.onAmountChanged(it) },
                placeholder = { Text("0,00", fontSize = 32.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formatDate(state.dateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDateDialog = true },
                    enabled = false
                )
            }

            OutlinedTextField(
                value = state.selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Категория") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                enabled = false
            )

            OutlinedTextField(
                value = state.selectedAccount?.let { "${it.name} (${formatAmount(it.initialBalance, it.currency)})" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Счёт") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAccountDialog = true },
                enabled = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.quantityText,
                    onValueChange = { viewModel.onQuantityChanged(it) },
                    label = { Text("Кол-во") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text("×", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = state.priceText,
                    onValueChange = { viewModel.onPriceChanged(it) },
                    label = { Text("Цена") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text("=", style = MaterialTheme.typography.titleLarge)
                val qty = state.quantityText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val price = state.priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val total = qty * price
                Text(
                    text = formatAmount(if (total > 0) total else 0.0, "₽"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Описание") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Теги", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { viewModel.removeTag(tag) },
                        label = { Text(tag, style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Удалить",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.removeTag(tag) }
                            )
                        }
                    )
                }
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = { Text("Тег") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.addTag(tagInput.trim()); tagInput = "" }
                    )
                )
            }

            if (state.attachmentPath != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null,
                            tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(state.attachmentName ?: "Файл",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = { viewModel.clearAttachment() }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Прикрепить файл")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Семейный расход", modifier = Modifier.weight(1f))
                Switch(
                    checked = state.isFamilyExpense,
                    onCheckedChange = {
                        viewModel.onFamilyChanged(it)
                        if (it) showFamilySheet = true
                    }
                )
            }

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = state.amountText.isNotBlank()
            ) {
                Text("Сохранить", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDateDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChanged(it) }
                    showDateDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCategoryDialog) {
        CategoryDialog(
            parents = state.categories.filter { it.parentId == null },
            childrenMap = state.childrenMap,
            onSelect = { category ->
                viewModel.onCategorySelected(category)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    if (showAccountDialog) {
        AccountDialog(
            accounts = state.accounts,
            onSelect = { account ->
                viewModel.onAccountSelected(account)
                showAccountDialog = false
            },
            onDismiss = { showAccountDialog = false }
        )
    }

    if (showFamilySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showFamilySheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Семейный расход", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                val amount = state.amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val half = amount / 2

                Text("Сумма: ${String.format("%.2f", amount)} ₽",
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Участник 1 (Вы)", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("${String.format("%.2f", half)} ₽",
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Участник 2", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("${String.format("%.2f", half)} ₽",
                            color = androidx.compose.ui.graphics.Color(0xFF2196F3))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Разделение 50/50", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showFamilySheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Понятно")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    parents: List<CategoryEntity>,
    childrenMap: Map<Long, List<CategoryEntity>>,
    onSelect: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedParentId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите категорию") },
        text = {
            Column {
                parents.forEach { parent ->
                    Text(
                        text = parent.name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val children = childrenMap[parent.id].orEmpty()
                                if (children.isEmpty()) {
                                    onSelect(parent)
                                } else {
                                    expandedParentId = if (expandedParentId == parent.id) null else parent.id
                                }
                            }
                            .padding(vertical = 8.dp)
                    )
                    if (expandedParentId == parent.id) {
                        childrenMap[parent.id]?.forEach { child ->
                            Text(
                                text = "  ${child.name}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(child) }
                                    .padding(vertical = 4.dp, horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
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
                accounts.forEach { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(account) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(account.color))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account.name, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            formatAmount(account.initialBalance, account.currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatAmount(amount: Double, currency: String): String {
    val sign = if (amount < 0) "-" else ""
    val abs = kotlin.math.abs(amount)
    val whole = abs.toLong()
    val frac = ((abs - whole) * 100).toLong()
    return "$sign$whole,${frac.toString().padStart(2, '0')} $currency"
}
