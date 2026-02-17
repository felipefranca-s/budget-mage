package com.budgetmage.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetmage.data.database.entity.CategoryEntity
import com.budgetmage.data.database.entity.TransactionType
import com.budgetmage.data.repository.CategoryRepository
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

data class CategoryListUiState(
    val isLoading: Boolean = true,
    val showAddEditDialog: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val categoryToDelete: CategoryEntity? = null,
    val transactionCountForDelete: Int = 0
)

sealed class CategoryListEvent {
    data class Error(val message: String) : CategoryListEvent()
    object CategorySaved : CategoryListEvent()
    object CategoryDeleted : CategoryListEvent()
}

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState())
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CategoryListEvent>()
    val events: SharedFlow<CategoryListEvent> = _events.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            categories.collect {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddEditDialog = true, editingCategory = null) }
    }

    fun showEditDialog(category: CategoryEntity) {
        _uiState.update { it.copy(showAddEditDialog = true, editingCategory = category) }
    }

    fun hideAddEditDialog() {
        _uiState.update { it.copy(showAddEditDialog = false, editingCategory = null) }
    }

    fun saveCategory(name: String, type: TransactionType) {
        val editingCategory = _uiState.value.editingCategory

        viewModelScope.launch {
            // Check for duplicate name
            val existing = categoryRepository.getCategoryByNameAndType(name.trim(), type)
            if (existing != null && existing.id != editingCategory?.id) {
                _events.emit(CategoryListEvent.Error("Já existe uma categoria com este nome"))
                return@launch
            }

            val category = if (editingCategory != null) {
                editingCategory.copy(name = name.trim(), type = type)
            } else {
                CategoryEntity(name = name.trim(), type = type)
            }

            val result = if (editingCategory != null) {
                categoryRepository.update(category)
            } else {
                categoryRepository.insert(category).map { }
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(showAddEditDialog = false, editingCategory = null) }
                    _events.emit(CategoryListEvent.CategorySaved)
                },
                onFailure = { e ->
                    _events.emit(CategoryListEvent.Error(e.message ?: "Erro ao salvar"))
                }
            )
        }
    }

    fun showDeleteConfirmation(category: CategoryEntity) {
        viewModelScope.launch {
            val count = categoryRepository.getTransactionCount(category.id).first()
            _uiState.update {
                it.copy(
                    categoryToDelete = category,
                    transactionCountForDelete = count
                )
            }
        }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(categoryToDelete = null, transactionCountForDelete = 0) }
    }

    fun deleteCategory() {
        val category = _uiState.value.categoryToDelete ?: return

        if (_uiState.value.transactionCountForDelete > 0) {
            viewModelScope.launch {
                _events.emit(CategoryListEvent.Error("Esta categoria está em uso e não pode ser excluída"))
            }
            hideDeleteConfirmation()
            return
        }

        viewModelScope.launch {
            categoryRepository.delete(category).fold(
                onSuccess = {
                    _uiState.update { it.copy(categoryToDelete = null, transactionCountForDelete = 0) }
                    _events.emit(CategoryListEvent.CategoryDeleted)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(categoryToDelete = null, transactionCountForDelete = 0) }
                    _events.emit(CategoryListEvent.Error(e.message ?: "Erro ao excluir"))
                }
            )
        }
    }
}
