package com.vladnwx.receiptexpensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private data class Tab(val label: String, val screen: AppScreen)

enum class AppScreen {
    EXPENSE, INCOME, TRANSFER, DEBTS, REPORTS, SYNC, SCAN, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }

    fun msg(id: Int) = context.getString(id)

    val tabs = listOf(
        Tab(msg(R.string.app_expense), AppScreen.EXPENSE),
        Tab(msg(R.string.app_income), AppScreen.INCOME),
        Tab(msg(R.string.app_transfer), AppScreen.TRANSFER),
        Tab(msg(R.string.app_debts), AppScreen.DEBTS),
        Tab(msg(R.string.app_reports), AppScreen.REPORTS),
        Tab(msg(R.string.app_sync), AppScreen.SYNC),
        Tab(msg(R.string.app_scan), AppScreen.SCAN),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(msg(R.string.app_name)) },
                actions = {
                    Row {
                        Text(
                            text = msg(R.string.app_settings),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .clickable { selectedTab = tabs.size }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        Text(
                            text = msg(R.string.app_github),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .clickable { showAboutDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedTab == tabs.size) {
                FeaturePlaceholder(
                    title = msg(R.string.app_settings),
                    message = msg(R.string.coming_soon)
                )
            } else {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg(R.string.coming_soon),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selectedTab == index)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                FeaturePlaceholder(
                    title = tabs[selectedTab].label,
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
                Text(
                    text = "OK",
                    modifier = Modifier.clickable { showAboutDialog = false }.padding(12.dp)
                )
            }
        )
    }
}

@Composable
private fun FeaturePlaceholder(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}
