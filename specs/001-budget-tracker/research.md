# Research: Budget Mage - Personal Finance Tracker

**Branch**: `001-budget-tracker` | **Date**: 2025-01-20

## Overview

This document captures research decisions for implementing the Budget Mage Android app. All technical choices align with the project constitution and prioritize simplicity, local-first data, and fast transaction entry.

---

## Decision 1: Monetary Value Representation

**Decision**: Use `Long` (cents) for storing amounts, `BigDecimal` for calculations

**Rationale**:
- Room/SQLite doesn't natively support `BigDecimal`
- Storing cents as `Long` avoids floating-point precision issues
- Convert to `BigDecimal` only when performing calculations or displaying
- Example: R$ 123.45 stored as `12345L`

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| `Double` | Floating-point precision errors in financial calculations |
| `String` | Requires parsing, inefficient for queries and sorting |
| `BigDecimal` with TypeConverter | Adds complexity, still converts to String internally |

**Implementation**:
```kotlin
// Entity stores cents as Long
@Entity
data class TransactionEntity(
    val amountCents: Long  // R$ 123.45 = 12345
)

// Extension for display
fun Long.toBigDecimal(): BigDecimal = BigDecimal(this).divide(BigDecimal(100))
fun BigDecimal.toCents(): Long = this.multiply(BigDecimal(100)).toLong()
```

---

## Decision 2: Navigation Architecture

**Decision**: Jetpack Compose Navigation with type-safe routes

**Rationale**:
- Single Activity architecture (constitution: Android conventions)
- Type-safe navigation using Kotlin serialization for arguments
- Deep linking not required (no external entry points)
- Simple back stack management

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| Multiple Activities | Violates single-activity pattern, harder state management |
| Fragment Navigation | Unnecessary complexity when using Compose |
| Custom navigation | Reinventing the wheel, harder to maintain |

**Routes**:
```kotlin
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object TransactionList : Screen("transactions")
    object AddTransaction : Screen("transaction/add")
    data class EditTransaction(val id: Long) : Screen("transaction/edit/{id}")
    object Categories : Screen("categories")
    object Accounts : Screen("accounts")
}
```

---

## Decision 3: State Management

**Decision**: StateFlow in ViewModels with UiState sealed classes

**Rationale**:
- Constitution requires MVVM with StateFlow
- Sealed classes provide exhaustive state handling
- Single source of truth per screen
- Survives configuration changes

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| LiveData | StateFlow is more Kotlin-idiomatic and supports Flow operators |
| Compose State only | Doesn't survive process death, harder to test |
| Redux/MVI | Over-engineering for simple app (violates Simplicity First) |

**Pattern**:
```kotlin
sealed class TransactionListUiState {
    object Loading : TransactionListUiState()
    data class Success(
        val transactions: List<TransactionItem>,
        val filters: FilterState
    ) : TransactionListUiState()
    data class Error(val message: String) : TransactionListUiState()
}
```

---

## Decision 4: Database Prepopulation

**Decision**: Use Room's `RoomDatabase.Callback` for initial data

**Rationale**:
- Constitution requires default categories and account on first launch
- Callback runs once when database is created
- No external files or assets needed
- Transactional insertion ensures consistency

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| Prepopulated database file | Harder to version, increases APK size |
| Lazy initialization on first access | Race conditions, scattered logic |
| Migration from version 0 | Unnecessarily complex |

**Implementation**:
```kotlin
private class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Insert default categories and account
        CoroutineScope(Dispatchers.IO).launch {
            // Insert default data
        }
    }
}
```

---

## Decision 5: Date Handling

**Decision**: Store dates as `Long` (epoch milliseconds), use `java.time` for display

**Rationale**:
- Room natively supports `Long`
- Efficient for range queries (date filters)
- `java.time.LocalDate` for user-facing operations (API 26+ guaranteed)
- Simple TypeConverter

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| `String` (ISO format) | Inefficient for range queries |
| `java.util.Date` | Deprecated, mutable, timezone issues |
| Separate year/month/day columns | Complicates queries |

**TypeConverter**:
```kotlin
@TypeConverter
fun fromTimestamp(value: Long?): LocalDate? =
    value?.let { LocalDate.ofEpochDay(it) }

@TypeConverter
fun dateToTimestamp(date: LocalDate?): Long? =
    date?.toEpochDay()
```

---

## Decision 6: Transaction Type Enum

**Decision**: Use Kotlin `enum class` with Room TypeConverter

**Rationale**:
- Type safety at compile time
- Clear distinction between income and expense
- Simple ordinal storage in database
- Exhaustive when expressions

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| Boolean `isIncome` | Less readable, no room for future types |
| String | No type safety, typo-prone |
| Int constants | No compile-time safety |

**Implementation**:
```kotlin
enum class TransactionType {
    INCOME,
    EXPENSE
}
```

---

## Decision 7: Dependency Injection

**Decision**: Hilt with minimal module setup

**Rationale**:
- Constitution allows Hilt "if needed"
- ViewModels require injection of repositories
- Repositories require injection of DAOs
- Reduces boilerplate while maintaining testability

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| Manual DI | More boilerplate, error-prone |
| Koin | Less compile-time safety than Hilt |
| No DI | Hard to test, tight coupling |

**Scope**:
- Single `@Module` for database and repositories
- `@HiltViewModel` for all ViewModels
- Keep it minimal - no unnecessary abstractions

---

## Decision 8: Default Categories

**Decision**: Provide 8 expense and 4 income categories in Portuguese

**Rationale**:
- User is Brazilian (Portuguese interface)
- Categories cover common personal finance use cases
- Users can add/edit categories later (P4 user story)

**Default Expense Categories**:
1. Alimentação (Food)
2. Transporte (Transportation)
3. Moradia (Housing)
4. Utilidades (Utilities - water, electricity, internet)
5. Saúde (Health)
6. Lazer (Entertainment)
7. Educação (Education)
8. Outros (Other)

**Default Income Categories**:
1. Salário (Salary)
2. Investimentos (Investments)
3. Freelance (Freelance)
4. Outros (Other)

---

## Decision 9: Dashboard Aggregation

**Decision**: Compute aggregations via Room SQL queries, not in-memory

**Rationale**:
- Performance: SQLite handles aggregation efficiently
- Memory: Don't load all transactions into memory
- Reactivity: Use Flow to observe changes

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| Load all, compute in Kotlin | Memory issues with large datasets |
| Cached values table | Adds complexity, sync issues |

**Key Queries**:
```sql
-- Monthly totals
SELECT
    SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END) as totalIncome,
    SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END) as totalExpense
FROM transactions
WHERE date >= :startOfMonth AND date < :endOfMonth

-- Top expense categories
SELECT c.name, SUM(t.amountCents) as total
FROM transactions t
JOIN categories c ON t.categoryId = c.id
WHERE t.type = 'EXPENSE' AND t.date >= :start AND t.date < :end
GROUP BY t.categoryId
ORDER BY total DESC
LIMIT 5
```

---

## Decision 10: Filter Persistence

**Decision**: Keep filters in ViewModel state only, don't persist to database

**Rationale**:
- Filters are transient UI state
- Users expect fresh view on app restart
- Simplifies implementation (constitution: Simplicity First)

**Alternatives Considered**:
| Alternative | Rejected Because |
|-------------|------------------|
| SharedPreferences | Unnecessary persistence, adds complexity |
| DataStore | Over-engineering for transient state |

---

## Summary

All decisions prioritize:
1. **Simplicity** - Minimal abstractions, standard Android patterns
2. **Performance** - Efficient storage and queries
3. **Correctness** - Type safety, proper decimal handling
4. **Testability** - Clear separation, DI support
