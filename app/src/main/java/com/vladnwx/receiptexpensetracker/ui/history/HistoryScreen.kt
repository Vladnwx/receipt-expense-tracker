package com.vladnwx.receiptexpensetracker.ui.history

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.vladnwx.receiptexpensetracker.data.local.entity.OperationType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { viewModel.setQuery(it) },
            label = { Text("Поиск") },
            placeholder = { Text("Описание, сумма, теги...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = "Фильтр: ${state.filter.name.lowercase().replaceFirstChar { it.uppercase() }}",
                modifier = Modifier.clickable { expanded = true }.padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                HistoryFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = { viewModel.setFilter(filter); expanded = false }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.items.isEmpty()) {
                item {
                    Text("Ничего не найдено",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp))
                }
            }

            items(state.items, key = { "${it.type}_${it.id}" }) { item ->
                HistoryItemCard(
                    item = item,
                    dateFormat = dateFormat,
                    onDelete = { viewModel.deleteItem(item.id, item.type) }
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: HistoryItem,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val typeColor = when (item.type) {
        OperationType.EXPENSE -> Color(0xFFE53935)
        OperationType.INCOME -> Color(0xFF4CAF50)
        OperationType.TRANSFER -> Color(0xFF2196F3)
    }
    val typeLabel = when (item.type) {
        OperationType.EXPENSE -> "Расход"
        OperationType.INCOME -> "Доход"
        OperationType.TRANSFER -> "Перевод"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(typeLabel, color = typeColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${String.format("%.2f", item.amount)} ₽",
                        fontWeight = FontWeight.Bold)
                }
                if (item.description != null) {
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    if (item.categoryName != null) Text(item.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.accountName != null) {
                        Text(" • ${item.accountName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(dateFormat.format(Date(item.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
