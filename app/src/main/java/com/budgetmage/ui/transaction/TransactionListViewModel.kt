package com.budgetmage.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.MonthSummary
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.database.entity.TransactionWithDetails
import com.budgetmage.data.repository.AccountRepository
import com.budgetmage.data.repository.CategoryRepository
import com.budgetmage.data.repository.TransactionRepository
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

private fun currentMonthFilter(): TransactionFilter {
    val now = YearMonth.now()
    return TransactionFilter(
        startDate = now.atDay(1),
        endDate = now.atEndOfMonth()
    )
}

data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryIds: Set<Long> = emptySet(),
    val accountId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val descriptionQuery: String = ""
) {
    val hasActiveFilters: Boolean
        get() = type != null || categoryIds.isNotEmpty() || accountId != null ||
                startDate != null || endDate != null || descriptionQuery.isNotBlank()
}

data class TransactionListUiState(
    val isLoading: Boolean = true,
    val filter: TransactionFilter = TransactionFilter(),
    val showFilterSheet: Boolean = false,
    val transactionToDelete: TransactionWithDetails? = null,
    val summary: MonthSummary? = null
)

sealed class TransactionListEvent {
    data class Error(val message: String) : TransactionListEvent()
    object TransactionDeleted : TransactionListEvent()
}

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialFilter: TransactionFilter = run {
        val categoryId = savedStateHandle.get<Long>("categoryId")?.takeIf { it != -1L }
        val savedStart = savedStateHandle.get<Long>("startDate")?.takeIf { it != -1L }?.let { LocalDate.ofEpochDay(it) }
        val savedEnd = savedStateHandle.get<Long>("endDate")?.takeIf { it != -1L }?.let { LocalDate.ofEpochDay(it) }
        val defaults = currentMonthFilter()
        TransactionFilter(
            type = if (categoryId != null) TransactionType.EXPENSE else null,
            categoryIds = if (categoryId != null) setOf(categoryId) else emptySet(),
            startDate = savedStart ?: defaults.startDate,
            endDate = savedEnd ?: defaults.endDate
        )
    }

    private val _uiState = MutableStateFlow(TransactionListUiState(filter = initialFilter))
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TransactionListEvent>()
    val events: SharedFlow<TransactionListEvent> = _events.asSharedFlow()

    private val _filter = MutableStateFlow(initialFilter)

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionWithDetails>> = _filter
        .flatMapLatest { filter ->
            transactionRepository.getFilteredTransactionsPaged(
                type = filter.type,
                categoryIds = filter.categoryIds.toList(),
                accountId = filter.accountId,
                startDate = filter.startDate?.toEpochDay(),
                endDate = filter.endDate?.toEpochDay(),
                descriptionQuery = filter.descriptionQuery.trim().takeIf { it.isNotBlank() }
            )
        }
        .cachedIn(viewModelScope)

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun showFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = true) }
    }

    fun hideFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun applyFilter(filter: TransactionFilter) {
        _filter.value = filter
        _uiState.update {
            it.copy(
                filter = filter,
                showFilterSheet = false,
                summary = null
            )
        }
    }

    fun clearFilters() {
        val resetFilter = currentMonthFilter()
        _filter.value = resetFilter
        _uiState.update {
            it.copy(
                filter = resetFilter,
                showFilterSheet = false,
                summary = null
            )
        }
    }

    fun toggleSummary() {
        if (_uiState.value.summary != null) {
            _uiState.update { it.copy(summary = null) }
            return
        }
        val filter = _filter.value
        viewModelScope.launch {
            val result = transactionRepository.getFilteredSummaryOnce(
                type = filter.type,
                categoryIds = filter.categoryIds.toList(),
                accountId = filter.accountId,
                startDate = filter.startDate?.toEpochDay(),
                endDate = filter.endDate?.toEpochDay(),
                descriptionQuery = filter.descriptionQuery.trim().takeIf { it.isNotBlank() }
            )
            _uiState.update { it.copy(summary = result) }
        }
    }

    fun showDeleteConfirmation(transaction: TransactionWithDetails) {
        _uiState.update { it.copy(transactionToDelete = transaction) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(transactionToDelete = null) }
    }

    fun deleteTransaction() {
        val transaction = _uiState.value.transactionToDelete ?: return

        viewModelScope.launch {
            transactionRepository.deleteById(transaction.id).fold(
                onSuccess = {
                    _uiState.update { it.copy(transactionToDelete = null) }
                    _events.emit(TransactionListEvent.TransactionDeleted)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(transactionToDelete = null) }
                    _events.emit(TransactionListEvent.Error(e.message ?: "Erro ao excluir"))
                }
            )
        }
    }
}
