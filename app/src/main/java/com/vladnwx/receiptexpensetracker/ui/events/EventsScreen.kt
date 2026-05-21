package com.vladnwx.receiptexpensetracker.ui.events

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.ContactEntity
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(viewModel: EventsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Создать мероприятие")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Мероприятия", style = MaterialTheme.typography.headlineMedium)
            }

            if (state.events.isEmpty()) {
                item {
                    Text("Нет мероприятий",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp))
                }
            }

            items(state.events) { ew ->
                EventCard(
                    ew = ew,
                    contacts = state.contacts,
                    onDelete = { viewModel.deleteEvent(ew.event.id) },
                    onAddParticipant = { contactId, contribution ->
                        viewModel.addParticipant(ew.event.id, contactId, contribution)
                    }
                )
            }
        }
    }

    if (state.showCreateDialog) {
        CreateEventDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, desc -> viewModel.createEvent(name, desc) }
        )
    }
}

@Composable
private fun EventCard(
    ew: com.vladnwx.receiptexpensetracker.ui.events.EventWithDetails,
    contacts: List<ContactEntity>,
    onDelete: () -> Unit,
    onAddParticipant: (Long, Double) -> Unit
) {
    val event = ew.event
    var showAddParticipant by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(event.name, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            if (event.description != null) Text(event.description)
            Text("Всего потрачено: ${String.format("%.2f", ew.totalSpent)} ₽")

            if (ew.participants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Участники:", fontWeight = FontWeight.Bold)
                val share = if (ew.participants.isNotEmpty()) ew.totalSpent / ew.participants.size else 0.0
                ew.participants.forEach { p ->
                    val name = contacts.find { it.id == p.contactId }?.name ?: "Участник"
                    Text("• $name — взнос ${String.format("%.2f", p.contribution)} ₽" +
                            if (share > 0) " (доля ${String.format("%.2f", share)} ₽)" else "")
                }
                val balance = ew.participants.sumOf { it.contribution } - ew.totalSpent
                if (balance != 0.0) {
                    Text("Баланс: ${String.format("%.2f", balance)} ₽",
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showAddParticipant = !showAddParticipant }) {
                    Text(if (showAddParticipant) "Отмена" else "Добавить участника")
                }
                TextButton(onClick = onDelete) { Text("Удалить") }
            }

            if (showAddParticipant) {
                AddParticipantSection(
                    contacts = contacts,
                    onAdd = { contactId, contribution ->
                        onAddParticipant(contactId, contribution)
                        showAddParticipant = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddParticipantSection(
    contacts: List<ContactEntity>,
    onAdd: (Long, Double) -> Unit
) {
    var selectedContact by remember { mutableStateOf<Long?>(null) }
    var contribution by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = contacts.find { it.id == selectedContact }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Участник") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                contacts.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = { selectedContact = c.id; expanded = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = contribution,
            onValueChange = { contribution = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
            label = { Text("Взнос") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val contrib = contribution.replace(",", ".").toDoubleOrNull() ?: 0.0
                selectedContact?.let { onAdd(it, contrib) }
            },
            enabled = selectedContact != null
        ) {
            Text("Добавить")
        }
    }
}

@Composable
private fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое мероприятие") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
