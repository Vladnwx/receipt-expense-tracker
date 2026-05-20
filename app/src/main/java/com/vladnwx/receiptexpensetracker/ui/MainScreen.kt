package com.vladnwx.receiptexpensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vladnwx.receiptexpensetracker.R
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN, EXPENSE, INCOME, TRANSFER, DEBTS, REPORTS, SYNC, SCAN, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }

    fun msg(id: Int) = context.getString(id)
    fun screenTitle(screen: AppScreen): String = when (screen) {
        AppScreen.MAIN -> msg(R.string.app_name)
        AppScreen.EXPENSE -> msg(R.string.app_expense)
        AppScreen.INCOME -> msg(R.string.app_income)
        AppScreen.TRANSFER -> msg(R.string.app_transfer)
        AppScreen.DEBTS -> msg(R.string.app_debts)
        AppScreen.REPORTS -> msg(R.string.app_reports)
        AppScreen.SYNC -> msg(R.string.app_sync)
        AppScreen.SCAN -> msg(R.string.app_scan)
        AppScreen.SETTINGS -> msg(R.string.app_settings)
    }

    Scaffold(
        topBar = {
            if (currentScreen != AppScreen.MAIN) {
                TopAppBar(
                    title = { Text(screenTitle(currentScreen)) },
                    navigationIcon = {
                        TextButton(onClick = { currentScreen = AppScreen.MAIN }) {
                            Text("< ${msg(R.string.back)}")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(msg(R.string.app_name)) },
                    actions = {
                        TextButton(onClick = { currentScreen = AppScreen.SETTINGS }) {
                            Text(msg(R.string.app_settings))
                        }
                        TextButton(onClick = { showAboutDialog = true }) {
                            Text(msg(R.string.app_github))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentScreen) {
                AppScreen.MAIN -> MainMenu(
                    appName = msg(R.string.app_name),
                    features = listOf(
                        msg(R.string.app_expense) to AppScreen.EXPENSE,
                        msg(R.string.app_income) to AppScreen.INCOME,
                        msg(R.string.app_transfer) to AppScreen.TRANSFER,
                        msg(R.string.app_debts) to AppScreen.DEBTS,
                        msg(R.string.app_reports) to AppScreen.REPORTS,
                        msg(R.string.app_sync) to AppScreen.SYNC,
                        msg(R.string.app_scan) to AppScreen.SCAN,
                    ),
                    onFeatureClick = { screen ->
                        currentScreen = screen
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = msg(R.string.coming_soon),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
                else -> FeaturePlaceholder(
                    title = screenTitle(currentScreen),
                    message = msg(R.string.coming_soon)
                )
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(msg(R.string.app_name)) },
            text = { Text("https://github.com/Vladnwx/receipt-expense-tracker") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun MainMenu(
    appName: String,
    features: List<Pair<String, AppScreen>>,
    onFeatureClick: (AppScreen) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        features.chunked(2).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, screen) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        onClick = { onFeatureClick(screen) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "v1.0 — github.com/Vladnwx/receipt-expense-tracker",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeaturePlaceholder(title: String, message: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = message, style = MaterialTheme.typography.bodyLarge)
}
