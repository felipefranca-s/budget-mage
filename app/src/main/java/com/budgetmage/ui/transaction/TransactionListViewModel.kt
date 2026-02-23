package com.budgetmage.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.budgetmage.data.database.TestDataSeeder
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.database.entity.TransactionWithDetails
import com.budgetmage.data.repository.AccountRepository
import com.budgetmage.data.repository.CategoryRepository
import com.budgetmage.data.repository.TransactionRepository
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
import javax.inject.Inject

data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val hasActiveFilters: Boolean
        get() = type != null || categoryId != null || accountId != null ||
                startDate != null || endDate != null
}

data class TransactionListUiState(
    val isLoading: Boolean = true,
    val filter: TransactionFilter = TransactionFilter(),
    val showFilterSheet: Boolean = false,
    val transactionToDelete: TransactionWithDetails? = null
)

sealed class TransactionListEvent {
    data class Error(val message: String) : TransactionListEvent()
    object TransactionDeleted : TransactionListEvent()
    object TestDataSeeded : TransactionListEvent()
}

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val testDataSeeder: TestDataSeeder,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TransactionListEvent>()
    val events: SharedFlow<TransactionListEvent> = _events.asSharedFlow()

    private val _filter = MutableStateFlow(TransactionFilter())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<PagingData<TransactionWithDetails>> = _filter
        .flatMapLatest { filter ->
            transactionRepository.getFilteredTransactionsPaged(
                type = filter.type,
                categoryId = filter.categoryId,
                accountId = filter.accountId,
                startDate = filter.startDate?.toEpochDay(),
                endDate = filter.endDate?.toEpochDay()
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
                showFilterSheet = false
            )
        }
    }

    fun clearFilters() {
        val emptyFilter = TransactionFilter()
        _filter.value = emptyFilter
        _uiState.update {
            it.copy(
                filter = emptyFilter,
                showFilterSheet = false
            )
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

    fun seedTestData() {
        viewModelScope.launch {
            try {
                testDataSeeder.seedTestTransactions(100)
                _events.emit(TransactionListEvent.TestDataSeeded)
            } catch (e: Exception) {
                _events.emit(TransactionListEvent.Error(e.message ?: "Erro ao criar dados de teste"))
            }
        }
    }
}
