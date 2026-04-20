package com.budgetmage.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.CategoryTotal
import com.budgetmage.data.database.entity.MonthSummary
import com.budgetmage.data.repository.PaymentRepository
import com.budgetmage.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

data class DashboardUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val valuesHidden: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthSummary: StateFlow<MonthSummary> = _uiState
        .flatMapLatest { state ->
            transactionRepository.getMonthSummary(state.selectedMonth)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MonthSummary(0, 0)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryExpenses: StateFlow<List<CategoryTotal>> = _uiState
        .flatMapLatest { state ->
            transactionRepository.getAllExpenseCategories(state.selectedMonth)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val unpaidBillsTotal: StateFlow<Long> = _uiState
        .flatMapLatest { state ->
            paymentRepository.getUnpaidTotalForMonth(state.selectedMonth)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L
        )

    init {
        // Mark loading as false after initial data load
        viewModelScope.launch {
            monthSummary.collect {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectPreviousMonth() {
        _uiState.value = _uiState.value.copy(
            selectedMonth = _uiState.value.selectedMonth.minusMonths(1),
            isLoading = true
        )
    }

    fun selectNextMonth() {
        _uiState.value = _uiState.value.copy(
            selectedMonth = _uiState.value.selectedMonth.plusMonths(1),
            isLoading = true
        )
    }

    fun toggleValuesVisibility() {
        _uiState.value = _uiState.value.copy(
            valuesHidden = !_uiState.value.valuesHidden
        )
    }
}
