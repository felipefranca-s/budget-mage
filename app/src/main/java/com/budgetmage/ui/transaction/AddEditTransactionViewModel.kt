package com.budgetmage.ui.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.TransactionEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.repository.AccountRepository
import com.budgetmage.data.repository.CategoryRepository
import com.budgetmage.data.repository.PaymentRepository
import com.budgetmage.data.repository.TransactionRepository
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

data class AddEditTransactionUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val transactionId: Long? = null,
    val amountInput: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val description: String = "",
    val amountError: String? = null,
    val categoryError: String? = null,
    val accountError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddEditTransactionEvent {
    object TransactionSaved : AddEditTransactionEvent()
    data class Error(val message: String) : AddEditTransactionEvent()
}

private data class PaymentContext(
    val paymentId: Long,
    val yearMonth: Int
)

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")

    private val prefillAmountCents: Long = savedStateHandle.get<Long>("amountCents") ?: -1L
    private val prefillCategoryId: Long = savedStateHandle.get<Long>("categoryId") ?: -1L
    private val prefillAccountId: Long = savedStateHandle.get<Long>("accountId") ?: -1L
    private val prefillDescription: String? = savedStateHandle.get<String>("description")
    private val prefillPaymentId: Long = savedStateHandle.get<Long>("paymentId") ?: -1L
    private val prefillYearMonth: Int = savedStateHandle.get<Int>("yearMonth") ?: -1

    private val paymentContext: PaymentContext? =
        if (prefillPaymentId > 0 && prefillYearMonth > 0) {
            PaymentContext(prefillPaymentId, prefillYearMonth)
        } else null

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditTransactionEvent>()
    val events: SharedFlow<AddEditTransactionEvent> = _events.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            if (transactionId != null && transactionId > 0) {
                val transaction = transactionRepository.getTransactionEntityById(transactionId)
                if (transaction != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            transactionId = transaction.id,
                            amountInput = (transaction.amountCents / 100.0).toString()
                                .replace(".", ","),
                            selectedType = transaction.type,
                            selectedDate = DateFormatter.epochDayToLocalDate(transaction.date),
                            selectedCategoryId = transaction.categoryId,
                            selectedAccountId = transaction.accountId,
                            description = transaction.description ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        amountInput = if (prefillAmountCents > 0) {
                            CurrencyFormatter.formatCents(prefillAmountCents)
                                .replace("R$", "")
                                .trim()
                        } else it.amountInput,
                        selectedType = if (paymentContext != null) TransactionType.EXPENSE else it.selectedType,
                        selectedCategoryId = if (prefillCategoryId > 0) prefillCategoryId else it.selectedCategoryId,
                        selectedAccountId = if (prefillAccountId > 0) prefillAccountId else it.selectedAccountId,
                        description = prefillDescription ?: it.description
                    )
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        // Allow only valid decimal input
        val filtered = amount.filter { it.isDigit() || it == ',' || it == '.' }
        _uiState.update {
            it.copy(
                amountInput = filtered,
                amountError = null
            )
        }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update {
            it.copy(
                selectedType = type,
                selectedCategoryId = null // Reset category when type changes
            )
        }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onCategoryChange(categoryId: Long) {
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                categoryError = null
            )
        }
    }

    fun onAccountChange(accountId: Long) {
        _uiState.update {
            it.copy(
                selectedAccountId = accountId,
                accountError = null
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun saveTransaction() {
        val state = _uiState.value

        // Validate
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

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = state.transactionId ?: 0,
                accountId = state.selectedAccountId,
                categoryId = state.selectedCategoryId,
                type = state.selectedType,
                amountCents = amountCents,
                date = DateFormatter.localDateToEpochDay(state.selectedDate),
                description = state.description.takeIf { it.isNotBlank() },
                createdAt = if (state.isEditMode) {
                    transactionRepository.getTransactionEntityById(state.transactionId!!)?.createdAt
                        ?: System.currentTimeMillis()
                } else {
                    System.currentTimeMillis()
                },
                updatedAt = System.currentTimeMillis()
            )

            if (state.isEditMode) {
                transactionRepository.update(transaction).fold(
                    onSuccess = {
                        _events.emit(AddEditTransactionEvent.TransactionSaved)
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isSaving = false) }
                        _events.emit(AddEditTransactionEvent.Error(e.message ?: "Erro ao salvar"))
                    }
                )
            } else {
                transactionRepository.insert(transaction).fold(
                    onSuccess = { newId ->
                        paymentContext?.let { ctx ->
                            paymentRepository.markPaid(
                                paymentId = ctx.paymentId,
                                yearMonth = ctx.yearMonth,
                                transactionId = newId,
                                paidAt = DateFormatter.todayEpochDay()
                            )
                        }
                        _events.emit(AddEditTransactionEvent.TransactionSaved)
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isSaving = false) }
                        _events.emit(AddEditTransactionEvent.Error(e.message ?: "Erro ao salvar"))
                    }
                )
            }
        }
    }
}
