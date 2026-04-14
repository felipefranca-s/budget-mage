package com.budgetmage.ui.transaction

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.budgetmage.R
import com.budgetmage.data.database.entity.MonthSummary
import com.budgetmage.ui.components.ConfirmDeleteDialog
import com.budgetmage.ui.components.FilterBottomSheet
import com.budgetmage.ui.components.TransactionItem
import com.budgetmage.ui.theme.ExpenseColor
import com.budgetmage.ui.theme.ExpenseColorDark
import com.budgetmage.ui.theme.IncomeColor
import com.budgetmage.ui.theme.IncomeColorDark
import com.budgetmage.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transactions = viewModel.transactions.collectAsLazyPagingItems()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionListEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is TransactionListEvent.TransactionDeleted -> {
                    snackbarHostState.showSnackbar("Transação excluída")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transactions_title)) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSummary() }) {
                        Icon(
                            Icons.Default.Functions,
                            contentDescription = "Somatória",
                            tint = if (uiState.summary != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.showFilterSheet() }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = if (uiState.filter.hasActiveFilters) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            transactions.loadState.refresh is LoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            transactions.itemCount == 0 && transactions.loadState.refresh is LoadState.NotLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_transactions),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.dashboard_add_first),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    uiState.summary?.let { SummaryHeader(summary = it) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = transactions.itemCount,
                            key = transactions.itemKey { it.id }
                        ) { index ->
                            transactions[index]?.let { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    onClick = { onTransactionClick(transaction.id) },
                                    onLongClick = { viewModel.showDeleteConfirmation(transaction) }
                                )
                            }
                        }

                        if (transactions.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    uiState.transactionToDelete?.let { transaction ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.delete),
            message = stringResource(R.string.transaction_delete_confirm),
            onConfirm = { viewModel.deleteTransaction() },
            onDismiss = { viewModel.hideDeleteConfirmation() }
        )
    }

    // Filter bottom sheet
    if (uiState.showFilterSheet) {
        FilterBottomSheet(
            sheetState = sheetState,
            currentFilter = uiState.filter,
            categories = categories,
            accounts = accounts,
            onApplyFilter = { filter ->
                viewModel.applyFilter(filter)
                scope.launch { sheetState.hide() }
            },
            onClearFilters = {
                viewModel.clearFilters()
                scope.launch { sheetState.hide() }
            },
            onDismiss = {
                viewModel.hideFilterSheet()
            }
        )
    }
}

@Composable
private fun SummaryHeader(summary: MonthSummary) {
    val hasIncome = summary.totalIncomeCents > 0L
    val hasExpense = summary.totalExpenseCents > 0L
    if (!hasIncome && !hasExpense) return

    val isDark = isSystemInDarkTheme()
    val incomeColor = if (isDark) IncomeColorDark else IncomeColor
    val expenseColor = if (isDark) ExpenseColorDark else ExpenseColor
    val showBalance = hasIncome && hasExpense

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (hasIncome) {
                    SummaryItem(
                        label = stringResource(R.string.dashboard_income),
                        amount = CurrencyFormatter.formatCents(summary.totalIncomeCents),
                        color = incomeColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (hasExpense) {
                    SummaryItem(
                        label = stringResource(R.string.dashboard_expense),
                        amount = CurrencyFormatter.formatCents(summary.totalExpenseCents),
                        color = expenseColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (showBalance) {
                Divider()
                SummaryItem(
                    label = stringResource(R.string.dashboard_balance),
                    amount = CurrencyFormatter.formatCents(summary.balanceCents),
                    color = if (summary.balanceCents >= 0) incomeColor else expenseColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
