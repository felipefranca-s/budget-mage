package com.budgetmage.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.budgetmage.BuildConfig
import com.budgetmage.R
import com.budgetmage.ui.components.ConfirmDeleteDialog
import com.budgetmage.ui.components.FilterBottomSheet
import com.budgetmage.ui.components.TransactionItem
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
                is TransactionListEvent.TestDataSeeded -> {
                    snackbarHostState.showSnackbar("100 transações de teste criadas!")
                    transactions.refresh()
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
                    // Debug button to seed test data (only in debug builds)
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = { viewModel.seedTestData() }) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = "Gerar dados de teste"
                            )
                        }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
