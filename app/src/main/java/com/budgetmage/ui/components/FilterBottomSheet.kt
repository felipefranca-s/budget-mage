package com.budgetmage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.budgetmage.R
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.ui.transaction.TransactionFilter
import com.budgetmage.util.DateFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    currentFilter: TransactionFilter,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    onApplyFilter: (TransactionFilter) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember(currentFilter) { mutableStateOf(currentFilter.type) }
    var selectedCategoryIds by remember(currentFilter) { mutableStateOf(currentFilter.categoryIds) }
    var selectedAccountId by remember(currentFilter) { mutableStateOf(currentFilter.accountId) }
    var startDate by remember(currentFilter) { mutableStateOf(currentFilter.startDate) }
    var endDate by remember(currentFilter) { mutableStateOf(currentFilter.endDate) }
    var descriptionQuery by remember(currentFilter) { mutableStateOf(currentFilter.descriptionQuery) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Filter categories by selected type
    val filteredCategories = if (selectedType != null) {
        categories.filter { it.type == selectedType }
    } else {
        categories
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.filter),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Scrollable content area
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Type filter
                Text(
                    text = stringResource(R.string.transaction_type),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategoryIds = selectedCategoryIds.filter { id ->
                                categories.find { it.id == id }?.type == TransactionType.EXPENSE
                            }.toSet()
                        },
                        label = { Text(stringResource(R.string.transaction_expense)) }
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategoryIds = selectedCategoryIds.filter { id ->
                                categories.find { it.id == id }?.type == TransactionType.INCOME
                            }.toSet()
                        },
                        label = { Text(stringResource(R.string.transaction_income)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Account filter
                Text(
                    text = stringResource(R.string.transaction_account),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedAccountId == null,
                        onClick = { selectedAccountId = null },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                    accounts.forEach { account ->
                        FilterChip(
                            selected = selectedAccountId == account.id,
                            onClick = { selectedAccountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Date range filter
                Text(
                    text = stringResource(R.string.filter_date_range),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate?.let { DateFormatter.formatMedium(it) } ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.filter_start_date)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate?.let { DateFormatter.formatMedium(it) } ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(R.string.filter_end_date)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Description search
                Text(
                    text = stringResource(R.string.filter_description_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descriptionQuery,
                    onValueChange = { descriptionQuery = it },
                    placeholder = { Text(stringResource(R.string.filter_description_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Category filter
                Text(
                    text = stringResource(R.string.transaction_category),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryIds.isEmpty(),
                        onClick = { selectedCategoryIds = emptySet() },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                    filteredCategories.forEach { category ->
                        CategoryChip(
                            category = category,
                            selected = category.id in selectedCategoryIds,
                            onClick = {
                                selectedCategoryIds = if (category.id in selectedCategoryIds) {
                                    selectedCategoryIds - category.id
                                } else {
                                    selectedCategoryIds + category.id
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons (fixed at bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedType = null
                        selectedCategoryIds = emptySet()
                        selectedAccountId = null
                        startDate = null
                        endDate = null
                        descriptionQuery = ""
                        onClearFilters()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.filter_clear))
                }
                Button(
                    onClick = {
                        onApplyFilter(
                            TransactionFilter(
                                type = selectedType,
                                categoryIds = selectedCategoryIds,
                                accountId = selectedAccountId,
                                startDate = startDate,
                                endDate = endDate,
                                descriptionQuery = descriptionQuery
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.filter_apply))
                }
            }
        }
    }

    // Start date picker
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End date picker
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
