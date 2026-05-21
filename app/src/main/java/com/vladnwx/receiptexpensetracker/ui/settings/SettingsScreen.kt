package com.vladnwx.receiptexpensetracker.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var fnsToken by remember { mutableStateOf(
        prefs.getString("fns_token", "") ?: ""
    )}

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Настройки", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Text("API интеграции", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fnsToken,
                        onValueChange = {
                            fnsToken = it
                            prefs.edit().putString("fns_token", it).apply()
                        },
                        label = { Text("Proverkacheka API Token") },
                        placeholder = { Text("Токен для проверки чеков") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Используется для получения деталей чека по QR-коду",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("О приложении", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Receipt Expense Tracker", style = MaterialTheme.typography.titleSmall)
                    Text("Версия: 1.0", style = MaterialTheme.typography.bodySmall)
                    Text("github.com/Vladnwx/receipt-expense-tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
