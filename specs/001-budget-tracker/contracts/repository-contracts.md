# Repository Contracts: Budget Mage

**Branch**: `001-budget-tracker` | **Date**: 2025-01-20

This document defines the internal contracts between the UI layer (ViewModels) and the data layer (Repositories). Since Budget Mage is a local-only Android app, there are no external API endpoints. Instead, we define repository interfaces that serve as the contract between layers.

---

## TransactionRepository

### Operations

#### `getTransactions(filter: TransactionFilter): Flow<List<TransactionWithDetails>>`

Returns a reactive stream of transactions matching the filter criteria.

**Input**:
```kotlin
data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
```

**Output**:
```kotlin
data class TransactionWithDetails(
    val id: Long,
    val accountId: Long,
    val accountName: String,
    val categoryId: Long,
    val categoryName: String,
    val type: TransactionType,
    val amountCents: Long,
    val date: LocalDate,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

**Behavior**:
- Returns all transactions if filter has all nulls
- Results sorted by date descending, then createdAt descending
- Flow emits new list whenever underlying data changes

---

#### `getTransactionById(id: Long): Flow<TransactionWithDetails?>`

Returns a single transaction by ID, or null if not found.

---

#### `insertTransaction(transaction: NewTransaction): Result<Long>`

Creates a new transaction.

**Input**:
```kotlin
data class NewTransaction(
    val accountId: Long,
    val categoryId: Long,
    val type: TransactionType,
    val amountCents: Long,
    val date: LocalDate,
    val description: String?
)
```

**Output**: `Result<Long>` containing the new transaction ID on success

**Validation**:
- `amountCents` must be > 0
- `accountId` must exist
- `categoryId` must exist and match `type`

**Errors**:
- `InvalidAmountError`: amount <= 0 or > max
- `AccountNotFoundError`: accountId doesn't exist
- `CategoryNotFoundError`: categoryId doesn't exist
- `TypeMismatchError`: category type doesn't match transaction type

---

#### `updateTransaction(id: Long, transaction: NewTransaction): Result<Unit>`

Updates an existing transaction.

**Validation**: Same as insert, plus transaction must exist

**Errors**: Same as insert, plus `TransactionNotFoundError`

---

#### `deleteTransaction(id: Long): Result<Unit>`

Deletes a transaction by ID.

**Errors**: `TransactionNotFoundError` if ID doesn't exist

---

#### `getMonthSummary(yearMonth: YearMonth): Flow<MonthSummary>`

Returns monthly aggregation for dashboard.

**Output**:
```kotlin
data class MonthSummary(
    val yearMonth: YearMonth,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long
) {
    val balanceCents: Long get() = totalIncomeCents - totalExpenseCents
}
```

---

#### `getTopExpenseCategories(yearMonth: YearMonth, limit: Int): Flow<List<CategoryTotal>>`

Returns top expense categories for the month.

**Output**:
```kotlin
data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val totalCents: Long
)
```

---

## CategoryRepository

### Operations

#### `getCategories(type: TransactionType?): Flow<List<Category>>`

Returns all categories, optionally filtered by type.

**Output**:
```kotlin
data class Category(
    val id: Long,
    val name: String,
    val type: TransactionType,
    val createdAt: Instant
)
```

---

#### `getCategoryById(id: Long): Flow<Category?>`

Returns a single category by ID.

---

#### `insertCategory(name: String, type: TransactionType): Result<Long>`

Creates a new category.

**Validation**:
- `name` must be non-empty and <= 50 chars
- `name` + `type` combination must be unique

**Errors**:
- `InvalidNameError`: empty or too long
- `DuplicateCategoryError`: name+type already exists

---

#### `updateCategory(id: Long, name: String): Result<Unit>`

Updates a category's name (type cannot be changed).

**Validation**: Same as insert

**Errors**: Same as insert, plus `CategoryNotFoundError`

---

#### `deleteCategory(id: Long): Result<Unit>`

Deletes a category if no transactions use it.

**Errors**:
- `CategoryNotFoundError`: ID doesn't exist
- `CategoryInUseError`: transactions reference this category

---

#### `getCategoryUsageCount(id: Long): Flow<Int>`

Returns count of transactions using this category.

---

## AccountRepository

### Operations

#### `getAccounts(): Flow<List<Account>>`

Returns all bank accounts.

**Output**:
```kotlin
data class Account(
    val id: Long,
    val code: String,
    val name: String,
    val createdAt: Instant
)
```

---

#### `getAccountById(id: Long): Flow<Account?>`

Returns a single account by ID.

---

#### `insertAccount(code: String, name: String): Result<Long>`

Creates a new bank account.

**Validation**:
- `code` must be non-empty, <= 20 chars, unique
- `name` must be non-empty, <= 100 chars

**Errors**:
- `InvalidCodeError`: empty or too long
- `InvalidNameError`: empty or too long
- `DuplicateAccountCodeError`: code already exists

---

#### `updateAccount(id: Long, code: String, name: String): Result<Unit>`

Updates an existing account.

**Validation**: Same as insert

**Errors**: Same as insert, plus `AccountNotFoundError`

---

#### `deleteAccount(id: Long): Result<Unit>`

Deletes an account if no transactions use it.

**Errors**:
- `AccountNotFoundError`: ID doesn't exist
- `AccountInUseError`: transactions reference this account

---

#### `getAccountUsageCount(id: Long): Flow<Int>`

Returns count of transactions using this account.

---

## Error Types

```kotlin
sealed class DataError {
    // Transaction errors
    data class InvalidAmountError(val message: String) : DataError()
    data class TransactionNotFoundError(val id: Long) : DataError()
    data class TypeMismatchError(val message: String) : DataError()

    // Category errors
    data class CategoryNotFoundError(val id: Long) : DataError()
    data class DuplicateCategoryError(val name: String, val type: TransactionType) : DataError()
    data class CategoryInUseError(val id: Long, val usageCount: Int) : DataError()

    // Account errors
    data class AccountNotFoundError(val id: Long) : DataError()
    data class DuplicateAccountCodeError(val code: String) : DataError()
    data class AccountInUseError(val id: Long, val usageCount: Int) : DataError()

    // General errors
    data class InvalidNameError(val message: String) : DataError()
    data class InvalidCodeError(val message: String) : DataError()
}
```

---

## Flow Behavior Notes

- All `Flow` returns are cold flows from Room
- Flows emit immediately with current data, then on every change
- ViewModels should use `stateIn` with `SharingStarted.WhileSubscribed(5000)` to handle configuration changes efficiently
