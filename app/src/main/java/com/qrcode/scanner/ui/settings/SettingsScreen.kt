package com.qrcode.scanner.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.BuildConfig
import com.qrcode.scanner.data.local.entity.AccountEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onUpdateAvailable: (latestVersion: String, downloadUrl: String, releaseNotes: String, isMandatory: Boolean) -> Unit,
    onAccountsClick: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.status) {
        when (uiState.status) {
            UpdateStatus.UpToDate, UpdateStatus.Error -> {
                uiState.message?.let { snackbarHostState.showSnackbar(it) }
                viewModel.consumeMessage()
            }
            UpdateStatus.Available -> {
                onUpdateAvailable(
                    uiState.latestVersion ?: "",
                    uiState.downloadUrl ?: "",
                    uiState.releaseNotes ?: "",
                    uiState.isMandatory
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.tokenSaved) {
        if (uiState.tokenSaved) {
            snackbarHostState.showSnackbar("Токен сохранён")
            viewModel.consumeTokenSaved()
        }
    }

    LaunchedEffect(uiState.githubTokenSaved) {
        if (uiState.githubTokenSaved) {
            snackbarHostState.showSnackbar("GitHub токен сохранён")
            viewModel.consumeGitHubTokenSaved()
        }
    }

    LaunchedEffect(uiState.logCopied) {
        if (uiState.logCopied) {
            snackbarHostState.showSnackbar("Лог скопирован в буфер обмена")
            viewModel.consumeLogCopied()
        }
    }

    LaunchedEffect(uiState.logCleared) {
        if (uiState.logCleared) {
            snackbarHostState.showSnackbar("Лог очищен")
            viewModel.consumeLogCleared()
        }
    }

    LaunchedEffect(uiState.logSent) {
        if (uiState.logSent) {
            snackbarHostState.showSnackbar("Ошибки отправлены в GitHub Issues")
            viewModel.consumeLogSent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TokenSection(
                currentToken = uiState.proverkachekaToken,
                onEditClick = { viewModel.showTokenDialog() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GitHubTokenSection(
                currentToken = uiState.githubIssuesToken,
                onEditClick = { viewModel.showGitHubTokenDialog() },
                onTestIssue = { viewModel.testCreateIssue() },
                isTestingIssue = uiState.status == UpdateStatus.Checking
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultAccountSection(
                accounts = uiState.accounts,
                defaultAccountId = uiState.defaultAccountId,
                onSelectAccount = { viewModel.setDefaultAccount(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AccountsSection(onAccountsClick = onAccountsClick)

            Spacer(modifier = Modifier.height(16.dp))

            UpdateSection(
                currentVersion = BuildConfig.VERSION_NAME,
                isChecking = uiState.status == UpdateStatus.Checking,
                onCheckUpdate = { viewModel.checkUpdate() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LogSection(
                onCopyLog = { viewModel.copyLog() },
                onClearLog = { viewModel.clearLog() },
                onSendLogs = { viewModel.sendErrorLogsToIssue() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AboutSection()
        }
    }

    if (uiState.showTokenDialog) {
        TokenDialog(
            title = "Токен Proverkacheka",
            description = "Введите API токен от proverkacheka.com",
            currentValue = uiState.proverkachekaToken,
            onValueChange = { viewModel.onTokenInputChanged(it) },
            onSave = { viewModel.saveToken() },
            onDismiss = { viewModel.dismissTokenDialog() }
        )
    }

    if (uiState.showGitHubTokenDialog) {
        TokenDialog(
            title = "GitHub токен",
            description = "Введите GitHub Personal Access Token (scopes: public_repo)",
            currentValue = uiState.githubIssuesToken,
            onValueChange = { viewModel.onGitHubTokenInputChanged(it) },
            onSave = { viewModel.saveGitHubToken() },
            onDismiss = { viewModel.dismissGitHubTokenDialog() }
        )
    }
}

@Composable
private fun TokenSection(
    currentToken: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "API токен Proverkacheka",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (currentToken.isNotBlank()) "Токен установлен" else "Токен не задан",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (currentToken.isNotBlank()) "Изменить токен" else "Ввести токен")
            }
        }
    }
}

@Composable
private fun GitHubTokenSection(
    currentToken: String,
    onEditClick: () -> Unit,
    onTestIssue: () -> Unit,
    isTestingIssue: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "GitHub токен (crash reports)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (currentToken.isNotBlank()) "Токен установлен" else "Токен не задан",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Используется для автоматического создания issue при краше. Минимальные права: public_repo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (currentToken.isNotBlank()) "Изменить токен" else "Ввести токен")
            }
            if (currentToken.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onTestIssue,
                    enabled = !isTestingIssue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (isTestingIssue) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isTestingIssue) "Создание…" else "Тест создания Issue")
                }
            }
        }
    }
}

@Composable
private fun AccountsSection(onAccountsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Счета",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Управление счетами и картами",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAccountsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Управлять счетами")
            }
        }
    }
}

@Composable
private fun DefaultAccountSection(
    accounts: List<AccountEntity>,
    defaultAccountId: Long?,
    onSelectAccount: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Счёт по умолчанию",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Расходы из чеков будут привязаны к этому счёту",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (accounts.isEmpty()) {
                Text(
                    text = "Нет доступных счетов. Создайте счёт в разделе «Счета»",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = defaultAccountId?.let { id ->
                                accounts.find { it.id == id }?.name ?: "Выберите счёт"
                            } ?: "Не выбран",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Не выбран") },
                            onClick = {
                                onSelectAccount(null)
                                expanded = false
                            },
                            leadingIcon = {
                                if (defaultAccountId == null) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                }
                            }
                        )
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    onSelectAccount(account.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (defaultAccountId == account.id) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenDialog(
    title: String,
    description: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var localValue by remember(currentValue) { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = localValue,
                    onValueChange = {
                        localValue = it
                        onValueChange(it)
                    },
                    label = { Text("Токен") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onSave()
                    })
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = localValue.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun UpdateSection(
    currentVersion: String,
    isChecking: Boolean,
    onCheckUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Обновление",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Текущая версия: $currentVersion",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Проверка обновлений происходит автоматически при запуске приложения",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCheckUpdate,
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isChecking) "Проверка…" else "Проверить обновления")
            }
        }
    }
}

@Composable
private fun LogSection(
    onCopyLog: () -> Unit,
    onClearLog: () -> Unit,
    onSendLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Логирование",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Лог приложения для отладки",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCopyLog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Скопировать лог")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSendLogs,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Отправить ошибки в Issue")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClearLog,
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

@Composable
private fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "О программе",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AboutItem("Название", "ReceiptExpenseTracker")
            AboutItem("Версия", BuildConfig.VERSION_NAME)
            AboutItem("Код сборки", BuildConfig.VERSION_CODE.toString())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Приложение для сканирования и учёта чеков. Данные загружаются через Proverkacheka API.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
