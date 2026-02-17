package com.budgetmage.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.AccountEntity
import com.budgetmage.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountListUiState(
    val isLoading: Boolean = true,
    val showAddEditDialog: Boolean = false,
    val editingAccount: AccountEntity? = null,
    val accountToDelete: AccountEntity? = null,
    val transactionCountForDelete: Int = 0
)

sealed class AccountListEvent {
    data class Error(val message: String) : AccountListEvent()
    object AccountSaved : AccountListEvent()
    object AccountDeleted : AccountListEvent()
}

@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountListUiState())
    val uiState: StateFlow<AccountListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AccountListEvent>()
    val events: SharedFlow<AccountListEvent> = _events.asSharedFlow()

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            accounts.collect {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddEditDialog = true, editingAccount = null) }
    }

    fun showEditDialog(account: AccountEntity) {
        _uiState.update { it.copy(showAddEditDialog = true, editingAccount = account) }
    }

    fun hideAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, editingAccount = null) }
    }

    fun saveAccount(code: String, name: String) {
        val editingAccount = _uiState.value.editingAccount

        viewModelScope.launch {
            // Check for duplicate code
            val existing = accountRepository.getAccountByCode(code.trim().uppercase())
            if (existing != null && existing.id != editingAccount?.id) {
                _events.emit(AccountListEvent.Error("Já existe uma conta com este código"))
                return@launch
            }

            val account = if (editingAccount != null) {
                editingAccount.copy(
                    code = code.trim().uppercase(),
                    name = name.trim()
                )
            } else {
                AccountEntity(
                    code = code.trim().uppercase(),
                    name = name.trim()
                )
            }

            val result = if (editingAccount != null) {
                accountRepository.update(account)
            } else {
                accountRepository.insert(account).map { }
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(showAddEditDialog = false, editingAccount = null) }
                    _events.emit(AccountListEvent.AccountSaved)
                },
                onFailure = { e ->
                    _events.emit(AccountListEvent.Error(e.message ?: "Erro ao salvar"))
                }
            )
        }
    }

    fun showDeleteConfirmation(account: AccountEntity) {
        viewModelScope.launch {
            val count = accountRepository.getTransactionCount(account.id).first()
            _uiState.update {
                it.copy(
                    accountToDelete = account,
                    transactionCountForDelete = count
                )
            }
        }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(accountToDelete = null, transactionCountForDelete = 0) }
    }

    fun deleteAccount() {
        val account = _uiState.value.accountToDelete ?: return

        if (_uiState.value.transactionCountForDelete > 0) {
            viewModelScope.launch {
                _events.emit(AccountListEvent.Error("Esta conta está em uso e não pode ser excluída"))
            }
            hideDeleteConfirmation()
            return
        }

        viewModelScope.launch {
            accountRepository.delete(account).fold(
                onSuccess = {
                    _uiState.update { it.copy(accountToDelete = null, transactionCountForDelete = 0) }
                    _events.emit(AccountListEvent.AccountDeleted)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(accountToDelete = null, transactionCountForDelete = 0) }
                    _events.emit(AccountListEvent.Error(e.message ?: "Erro ao excluir"))
                }
            )
        }
    }
}
