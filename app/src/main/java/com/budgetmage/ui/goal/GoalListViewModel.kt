package com.budgetmage.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.GoalWithProgress
import com.budgetmage.data.repository.GoalRepository
import com.budgetmage.data.repository.PaymentRepository
import com.budgetmage.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

data class GoalListUiState(
    val selectedMonth: YearMonth = YearMonth.now()
)

data class GoalListTotals(
    val targetCents: Long = 0L,
    val achievedCents: Long = 0L
)

@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalListUiState())
    val uiState: StateFlow<GoalListUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<GoalWithProgress>> = _uiState
        .flatMapLatest { state ->
            combine(
                goalRepository.getActiveGoalsForMonth(state.selectedMonth),
                transactionRepository.getMonthSummary(state.selectedMonth),
                paymentRepository.getUnpaidTotalForMonth(state.selectedMonth)
            ) { activeGoals, summary, unpaidBills ->
                val pool = (summary.balanceCents - unpaidBills).coerceAtLeast(0L)
                var remaining = pool
                activeGoals.map { g ->
                    val achieved = minOf(remaining, g.amountCents)
                    remaining -= achieved
                    GoalWithProgress(
                        id = g.id,
                        name = g.name,
                        amountCents = g.amountCents,
                        startDate = g.startDate,
                        endDate = g.endDate,
                        notes = g.notes,
                        priority = g.priority,
                        createdAt = g.createdAt,
                        achievedCents = achieved
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totals: StateFlow<GoalListTotals> = goals
        .map { list ->
            GoalListTotals(
                targetCents = list.sumOf { it.amountCents },
                achievedCents = list.sumOf { it.achievedCents }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalListTotals())

    fun selectPreviousMonth() {
        _uiState.value = _uiState.value.copy(
            selectedMonth = _uiState.value.selectedMonth.minusMonths(1)
        )
    }

    fun selectNextMonth() {
        _uiState.value = _uiState.value.copy(
            selectedMonth = _uiState.value.selectedMonth.plusMonths(1)
        )
    }
}
