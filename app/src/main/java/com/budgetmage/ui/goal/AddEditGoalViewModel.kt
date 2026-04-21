package com.budgetmage.ui.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.GoalEntity
import com.budgetmage.data.repository.GoalRepository
import com.budgetmage.util.CurrencyFormatter
import com.budgetmage.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditGoalUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val goalId: Long? = null,
    val name: String = "",
    val amountInput: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val hasEndDate: Boolean = false,
    val endDate: LocalDate = LocalDate.now().plusMonths(12),
    val notes: String = "",
    val priorityInput: String = "",
    val nameError: String? = null,
    val amountError: String? = null,
    val dateError: String? = null,
    val priorityError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddEditGoalEvent {
    object GoalSaved : AddEditGoalEvent()
    object GoalDeleted : AddEditGoalEvent()
    data class Error(val message: String) : AddEditGoalEvent()
}

@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val goalId: Long? = savedStateHandle.get<Long>("goalId")

    private val _uiState = MutableStateFlow(AddEditGoalUiState())
    val uiState: StateFlow<AddEditGoalUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditGoalEvent>()
    val events: SharedFlow<AddEditGoalEvent> = _events.asSharedFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            if (goalId != null && goalId > 0) {
                val g = goalRepository.getGoalById(goalId)
                if (g != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            goalId = g.id,
                            name = g.name,
                            amountInput = (g.amountCents / 100.0).toString().replace(".", ","),
                            startDate = DateFormatter.epochDayToLocalDate(g.startDate),
                            hasEndDate = g.endDate != null,
                            endDate = g.endDate?.let { d -> DateFormatter.epochDayToLocalDate(d) }
                                ?: LocalDate.now().plusMonths(12),
                            notes = g.notes ?: "",
                            priorityInput = g.priority.toString()
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

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onPriorityChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(priorityInput = filtered, priorityError = null) }
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
        if (state.hasEndDate && state.endDate.isBefore(state.startDate)) {
            _uiState.update { it.copy(dateError = "Data final deve ser após a inicial") }
            return
        }
        val priority = state.priorityInput.toIntOrNull()
        if (priority == null || priority < 1 || priority > 999) {
            _uiState.update { it.copy(priorityError = "Prioridade deve ser entre 1 e 999") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val entity = GoalEntity(
                id = state.goalId ?: 0,
                name = state.name.trim(),
                amountCents = amountCents,
                startDate = DateFormatter.localDateToEpochDay(state.startDate),
                endDate = if (state.hasEndDate) DateFormatter.localDateToEpochDay(state.endDate) else null,
                notes = state.notes.trim().ifEmpty { null },
                priority = priority,
                createdAt = if (state.isEditMode) {
                    goalRepository.getGoalById(state.goalId!!)?.createdAt
                        ?: System.currentTimeMillis()
                } else System.currentTimeMillis()
            )

            val result = if (state.isEditMode) {
                goalRepository.updateGoal(entity)
            } else {
                goalRepository.insertGoal(entity).map { }
            }

            result.fold(
                onSuccess = { _events.emit(AddEditGoalEvent.GoalSaved) },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(AddEditGoalEvent.Error(e.message ?: "Erro ao salvar"))
                }
            )
        }
    }

    fun delete() {
        val id = _uiState.value.goalId ?: return
        viewModelScope.launch {
            goalRepository.deleteGoal(id).fold(
                onSuccess = { _events.emit(AddEditGoalEvent.GoalDeleted) },
                onFailure = { e ->
                    _events.emit(AddEditGoalEvent.Error(e.message ?: "Erro ao excluir"))
                }
            )
        }
    }
}
