package com.vladnwx.receiptexpensetracker.ui.sync

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncScreen(viewModel: SyncViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("NextCloud Синхронизация", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.serverUrl,
                        onValueChange = { viewModel.updateUrl(it) },
                        label = { Text("URL сервера") },
                        placeholder = { Text("https://nextcloud.example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { viewModel.updateUsername(it) },
                        label = { Text("Имя пользователя") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("Пароль / App token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.testConnection() },
                            modifier = Modifier.weight(1f)) {
                            Text("Проверить")
                        }
                        TextButton(onClick = { viewModel.saveSettings() }) {
                            Text("Сохранить")
                        }
                    }
                    if (state.connectionStatus != null) {
                        Text(state.connectionStatus!!, color = if (state.connectionStatus!!.startsWith("Ошибка"))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (state.serverUrl.isNotBlank()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.syncExport() },
                        enabled = !state.syncLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.syncLoading) CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        else Text("Выгрузить")
                    }
                    Button(
                        onClick = { viewModel.syncImport() },
                        enabled = !state.syncLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.syncLoading) CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        else Text("Загрузить")
                    }
                }
                if (state.syncError != null) {
                    Text(state.syncError!!, color = MaterialTheme.colorScheme.error)
                }
                if (state.syncSuccess != null) {
                    Text(state.syncSuccess!!, color = MaterialTheme.colorScheme.primary)
                }
                if (state.lastSyncAt > 0) {
                    Text("Последняя синхронизация: ${dateFormat.format(Date(state.lastSyncAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Курсы валют (ЦБ РФ)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { viewModel.refreshCurrencies() },
                enabled = !state.currencyLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.currencyLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Text("Обновить курсы")
                }
            }
            if (state.currencyError != null) {
                Text(state.currencyError!!, color = MaterialTheme.colorScheme.error)
            }
        }

        if (state.currencies.isNotEmpty()) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Валюта", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Курс к рублю", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                HorizontalDivider()
            }
        }

        items(state.currencies) { currency ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${currency.code} — ${currency.name}", modifier = Modifier.weight(1f))
                Text(
                    if (currency.isBase) "1.0000" else String.format("%.4f", currency.rateToBase),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
