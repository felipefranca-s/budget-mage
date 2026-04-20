package com.budgetmage.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.PaymentEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.repository.AccountRepository
import com.budgetmage.data.repository.CategoryRepository
import com.budgetmage.data.repository.PaymentRepository
import com.budgetmage.util.CurrencyFormatter
import com.budgetmage.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditPaymentUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val paymentId: Long? = null,
    val name: String = "",
    val amountInput: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val hasEndDate: Boolean = false,
    val endDate: LocalDate = LocalDate.now().plusMonths(12),
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val notes: String = "",
    val nameError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val accountError: String? = null,
    val dateError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddEditPaymentEvent {
    object PaymentSaved : AddEditPaymentEvent()
    object PaymentDeleted : AddEditPaymentEvent()
    data class Error(val message: String) : AddEditPaymentEvent()
}

@HiltViewModel
class AddEditPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository
) : ViewModel() {

    private val paymentId: Long? = savedStateHandle.get<Long>("paymentId")

    private val _uiState = MutableStateFlow(AddEditPaymentUiState())
    val uiState: StateFlow<AddEditPaymentUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditPaymentEvent>()
    val events: SharedFlow<AddEditPaymentEvent> = _events.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository
        .getCategoriesByType(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            if (paymentId != null && paymentId > 0) {
                val p = paymentRepository.getPaymentById(paymentId)
                if (p != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            paymentId = p.id,
                            name = p.name,
                            amountInput = (p.amountCents / 100.0).toString().replace(".", ","),
                            startDate = DateFormatter.epochDayToLocalDate(p.startDate),
                            hasEndDate = p.endDate != null,
                            endDate = p.endDate?.let { d -> DateFormatter.epochDayToLocalDate(d) }
                                ?: LocalDate.now().plusMonths(12),
                            selectedCategoryId = p.categoryId,
                            selectedAccountId = p.accountId,
                            notes = p.notes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == ',' || it == '.' }
        _uiState.update { it.copy(amountInput = filtered, amountError = null) }
    }

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date, dateError = null) }
    }

    fun onHasEndDateChange(value: Boolean) {
        _uiState.update { it.copy(hasEndDate = value, dateError = null) }
    }

    fun onEndDateChange(date: LocalDate) {
        _uiState.update { it.copy(endDate = date, dateError = null) }
    }

    fun onCategoryChange(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, categoryError = null) }
    }

    fun onAccountChange(accountId: Long) {
        _uiState.update { it.copy(selectedAccountId = accountId, accountError = null) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Informe um nome") }
            return
        }
        val amountCents = CurrencyFormatter.parseDecimalToCents(state.amountInput)
        if (amountCents == null || amountCents <= 0) {
            _uiState.update { it.copy(amountError = "Valor inválido") }
            return
        }
        if (state.selectedCategoryId == null) {
            _uiState.update { it.copy(categoryError = "Selecione uma categoria") }
            return
        }
        if (state.selectedAccountId == null) {
            _uiState.update { it.copy(accountError = "Selecione uma conta") }
            return
        }
        if (state.hasEndDate && state.endDate.isBefore(state.startDate)) {
            _uiState.update { it.copy(dateError = "Data final deve ser após a inicial") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val entity = PaymentEntity(
                id = state.paymentId ?: 0,
                name = state.name.trim(),
                amountCents = amountCents,
                startDate = DateFormatter.localDateToEpochDay(state.startDate),
                endDate = if (state.hasEndDate) DateFormatter.localDateToEpochDay(state.endDate) else null,
                categoryId = state.selectedCategoryId,
                accountId = state.selectedAccountId,
                notes = state.notes.trim().ifEmpty { null },
                createdAt = if (state.isEditMode) {
                    paymentRepository.getPaymentById(state.paymentId!!)?.createdAt
                        ?: System.currentTimeMillis()
                } else System.currentTimeMillis()
            )

            val result = if (state.isEditMode) {
                paymentRepository.updatePayment(entity)
            } else {
                paymentRepository.insertPayment(entity).map { }
            }

            result.fold(
                onSuccess = { _events.emit(AddEditPaymentEvent.PaymentSaved) },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(AddEditPaymentEvent.Error(e.message ?: "Erro ao salvar"))
                }
            )
        }
    }

    fun delete() {
        val id = _uiState.value.paymentId ?: return
        viewModelScope.launch {
            paymentRepository.deletePayment(id).fold(
                onSuccess = { _events.emit(AddEditPaymentEvent.PaymentDeleted) },
                onFailure = { e ->
                    _events.emit(AddEditPaymentEvent.Error(e.message ?: "Erro ao excluir"))
                }
            )
        }
    }
}
