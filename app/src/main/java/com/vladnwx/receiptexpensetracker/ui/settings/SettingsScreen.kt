package com.vladnwx.receiptexpensetracker.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vladnwx.receiptexpensetracker.BuildConfig
import com.vladnwx.receiptexpensetracker.data.reporter.AppLogger
import com.vladnwx.receiptexpensetracker.data.reporter.GitHubIssueReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var fnsToken by remember { mutableStateOf(
        prefs.getString("fns_token", "") ?: ""
    )}
    var githubToken by remember { mutableStateOf(
        prefs.getString("github_token", "") ?: ""
    )}
    val githubTokenSet = githubToken.isNotBlank()
    var issueDescription by remember { mutableStateOf("") }
    var sendingIssue by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
                Spacer(modifier = Modifier.height(8.dp))
                Text("GitHub токен", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(20.dp),
                                tint = if (githubTokenSet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (githubTokenSet) "Токен добавлен" else "Токен не настроен",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (githubTokenSet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedTextField(
                            value = githubToken,
                            onValueChange = {
                                githubToken = it
                                prefs.edit().putString("github_token", it).apply()
                            },
                            label = { Text("GitHub Personal Access Token") },
                            placeholder = { Text("Токен для создания Issues") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (githubTokenSet) {
                            OutlinedButton(
                                onClick = {
                                    githubToken = ""
                                    prefs.edit().remove("github_token").apply()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Удалить токен")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Отправить Issue", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = issueDescription,
                            onValueChange = { issueDescription = it },
                            label = { Text("Описание ошибки") },
                            placeholder = { Text("Опишите проблему…") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        Button(
                            onClick = {
                                if (!githubTokenSet) {
                                    scope.launch { snackbarHostState.showSnackbar("Сначала добавьте GitHub токен") }
                                    return@Button
                                }
                                if (issueDescription.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Опишите проблему") }
                                    return@Button
                                }
                                scope.launch {
                                    sendingIssue = true
                                    val result = withContext(Dispatchers.IO) {
                                        GitHubIssueReporter.reportError(
                                            token = githubToken.trim(),
                                            title = issueDescription.take(80),
                                            details = issueDescription
                                        )
                                    }
                                    sendingIssue = false
                                    issueDescription = ""
                                    snackbarHostState.showSnackbar(result)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !sendingIssue
                        ) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (sendingIssue) "Отправка…" else "Отправить Issue")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Логирование", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Лог приложения для отладки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = {
                                scope.launch {
                                    val logText = withContext(Dispatchers.IO) { AppLogger.getLogText() }
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("App Log", logText))
                                    snackbarHostState.showSnackbar("Лог скопирован в буфер обмена")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Скопировать лог")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) { AppLogger.clearLog() }
                                    snackbarHostState.showSnackbar("Лог очищен")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Очистить лог")
                        }
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
                        Text("Версия: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
                        Text("github.com/Vladnwx/receipt-expense-tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}
