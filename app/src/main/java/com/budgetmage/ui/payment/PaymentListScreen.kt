package com.budgetmage.ui.payment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetmage.data.database.entity.PaymentWithStatus
import com.budgetmage.data.repository.PaymentRepository
import com.budgetmage.ui.components.MonthSelector
import com.budgetmage.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentListScreen(
    onAddPayment: () -> Unit,
    onEditPayment: (Long) -> Unit,
    onMarkAsPaid: (paymentId: Long, yearMonth: Int, amountCents: Long, categoryId: Long, accountId: Long, description: String) -> Unit,
    onMenuClick: () -> Unit,
    viewModel: PaymentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentListEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val pending = payments.filter { !it.paid }
    val paid = payments.filter { it.paid }
    val yearMonthEncoded = PaymentRepository.encodeYearMonth(uiState.selectedMonth)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contas Fixas") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPayment) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MonthSelector(
                selectedMonth = uiState.selectedMonth,
                onPreviousMonth = viewModel::selectPreviousMonth,
                onNextMonth = viewModel::selectNextMonth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Divider()

            if (payments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma conta fixa para este mês",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item("header_pending") {
                            SectionHeader("Pendentes (${pending.size})")
                        }
                        items(pending, key = { "p_${it.id}" }) { item ->
                            PaymentRow(
                                item = item,
                                onClick = { onEditPayment(item.id) },
                                onMarkPaid = {
                                    onMarkAsPaid(
                                        item.id,
                                        yearMonthEncoded,
                                        item.amountCents,
                                        item.categoryId,
                                        item.accountId,
                                        item.name
                                    )
                                },
                                onUnmark = null
                            )
                        }
                    }

                    if (paid.isNotEmpty()) {
                        item("header_paid") {
                            if (pending.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            SectionHeader("Pagos (${paid.size})")
                        }
                        items(paid, key = { "d_${it.id}" }) { item ->
                            PaymentRow(
                                item = item,
                                onClick = { onEditPayment(item.id) },
                                onMarkPaid = null,
                                onUnmark = { viewModel.unmarkPaid(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun PaymentRow(
    item: PaymentWithStatus,
    onClick: () -> Unit,
    onMarkPaid: (() -> Unit)?,
    onUnmark: (() -> Unit)?
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.paid) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.categoryName} • ${item.accountName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!item.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatCents(item.amountCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (onMarkPaid != null) {
                TextButton(onClick = onMarkPaid) {
                    Text("Pagar")
                }
            }
            if (onUnmark != null) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Pago",
                    tint = MaterialTheme.colorScheme.primary
                )
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Desmarcar como pago") },
                            onClick = {
                                menuExpanded = false
                                onUnmark()
                            }
                        )
                    }
                }
            }
        }
    }
}
