package com.budgetmage.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.PaymentWithStatus
import com.budgetmage.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class PaymentListUiState(
    val selectedMonth: YearMonth = YearMonth.now()
)

sealed class PaymentListEvent {
    data class Error(val message: String) : PaymentListEvent()
}

@HiltViewModel
class PaymentListViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentListUiState())
    val uiState: StateFlow<PaymentListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PaymentListEvent>()
    val events: SharedFlow<PaymentListEvent> = _events.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val payments: StateFlow<List<PaymentWithStatus>> = _uiState
        .flatMapLatest { state ->
            paymentRepository.getActivePaymentsForMonth(state.selectedMonth)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedYearMonthEncoded: Int
        get() = PaymentRepository.encodeYearMonth(_uiState.value.selectedMonth)

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

    fun unmarkPaid(paymentId: Long) {
        viewModelScope.launch {
            val ym = PaymentRepository.encodeYearMonth(_uiState.value.selectedMonth)
            paymentRepository.unmarkPaid(paymentId, ym).onFailure { e ->
                _events.emit(PaymentListEvent.Error(e.message ?: "Erro"))
            }
        }
    }
}
