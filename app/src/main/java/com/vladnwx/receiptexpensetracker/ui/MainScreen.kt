package com.vladnwx.receiptexpensetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vladnwx.receiptexpensetracker.BuildConfig
import com.vladnwx.receiptexpensetracker.R
import com.vladnwx.receiptexpensetracker.ui.accounts.AccountsScreen
import com.vladnwx.receiptexpensetracker.ui.advances.AdvancesScreen
import com.vladnwx.receiptexpensetracker.ui.categories.CategoriesScreen
import com.vladnwx.receiptexpensetracker.ui.debts.DebtsScreen
import com.vladnwx.receiptexpensetracker.ui.events.EventsScreen
import com.vladnwx.receiptexpensetracker.ui.expense.ExpenseScreen
import com.vladnwx.receiptexpensetracker.ui.history.HistoryScreen
import com.vladnwx.receiptexpensetracker.ui.income.IncomeScreen
import com.vladnwx.receiptexpensetracker.ui.reports.ReportsScreen
import com.vladnwx.receiptexpensetracker.ui.scan.ScanScreen
import com.vladnwx.receiptexpensetracker.ui.settings.SettingsScreen
import com.vladnwx.receiptexpensetracker.ui.sync.SyncScreen
import com.vladnwx.receiptexpensetracker.ui.transfer.TransferScreen

enum class Tab(val labelRes: Int) {
    EXPENSE(R.string.app_expense),
    INCOME(R.string.app_income),
    TRANSFER(R.string.app_transfer),
    DEBTS(R.string.app_debts),
    REPORTS(R.string.app_reports),
    ADVANCES(R.string.app_advances),
    EVENTS(R.string.app_events),
    SYNC(R.string.app_sync),
    SCAN(R.string.app_scan),
    ACCOUNTS(R.string.app_accounts),
}

enum class SubScreen { HISTORY, SETTINGS, CATEGORIES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var subScreen by remember { mutableStateOf<SubScreen?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    fun msg(id: Int) = context.getString(id)

    val tabs = Tab.entries

    val content: @Composable () -> Unit = when (subScreen) {
        SubScreen.HISTORY -> { { HistoryScreen() } }
        SubScreen.SETTINGS -> { { SettingsScreen() } }
        SubScreen.CATEGORIES -> { { CategoriesScreen() } }
        null -> {
            {
                when (tabs[selectedTab]) {
                    Tab.EXPENSE -> ExpenseScreen()
                    Tab.INCOME -> IncomeScreen()
                    Tab.TRANSFER -> TransferScreen()
                    Tab.DEBTS -> DebtsScreen()
                    Tab.REPORTS -> ReportsScreen()
                    Tab.ADVANCES -> AdvancesScreen()
                    Tab.EVENTS -> EventsScreen()
                    Tab.SYNC -> SyncScreen()
                    Tab.SCAN -> ScanScreen()
                    Tab.ACCOUNTS -> AccountsScreen()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (subScreen != null) {
                TopAppBar(
                    title = {
                        Text(
                            when (subScreen) {
                                SubScreen.HISTORY -> msg(R.string.app_history)
                                SubScreen.SETTINGS -> msg(R.string.app_settings)
                                SubScreen.CATEGORIES -> msg(R.string.category)
                                null -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        Text(
                            text = "< ${msg(R.string.back)}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .clickable { subScreen = null }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(msg(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (subScreen == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = msg(R.string.app_history),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { subScreen = SubScreen.HISTORY }
                            .padding(vertical = 4.dp)
                    )
                    Text(
                        text = msg(R.string.app_settings),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { subScreen = SubScreen.SETTINGS }
                            .padding(vertical = 4.dp)
                    )
                    Text(
                        text = msg(R.string.app_github),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 4.dp)
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = msg(tab.labelRes),
                                    fontWeight = if (selectedTab == index)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            content()

            if (subScreen == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "v${BuildConfig.VERSION_NAME} — github.com/Vladnwx/receipt-expense-tracker",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
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
                    modifier = Modifier
                        .clickable { showAboutDialog = false }
                        .padding(12.dp)
                )
            }
        )
    }
}
