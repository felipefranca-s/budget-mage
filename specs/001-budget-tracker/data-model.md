# Data Model: Budget Mage

**Branch**: `001-budget-tracker` | **Date**: 2025-01-20

## Entity Relationship Diagram

```
┌─────────────────────┐       ┌─────────────────────┐
│      Account        │       │      Category       │
├─────────────────────┤       ├─────────────────────┤
│ id: Long (PK)       │       │ id: Long (PK)       │
│ code: String        │       │ name: String        │
│ name: String        │       │ type: TransactionType│
│ createdAt: Long     │       │ createdAt: Long     │
└─────────────────────┘       └─────────────────────┘
          │                             │
          │ 1                           │ 1
          │                             │
          ▼ *                           ▼ *
┌─────────────────────────────────────────────────────┐
│                    Transaction                       │
├─────────────────────────────────────────────────────┤
│ id: Long (PK)                                        │
│ accountId: Long (FK → Account)                       │
│ categoryId: Long (FK → Category)                     │
│ type: TransactionType                                │
│ amountCents: Long                                    │
│ date: Long (epoch day)                               │
│ description: String                                  │
│ createdAt: Long                                      │
│ updatedAt: Long                                      │
└─────────────────────────────────────────────────────┘
```

---

## Entities

### Account

Represents a bank account or cash container where money is held.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, auto-generated | Unique identifier |
| `code` | String | NOT NULL, max 20 chars | Short code (e.g., "BB", "NUBANK") |
| `name` | String | NOT NULL, max 100 chars | Display name (e.g., "Banco do Brasil") |
| `createdAt` | Long | NOT NULL | Epoch millis when created |

**Validation Rules**:
- `code` must be non-empty and unique
- `name` must be non-empty

**Default Data**:
- Account(code="CASH", name="Dinheiro")

---

### Category

Represents a classification for transactions, separated by type.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, auto-generated | Unique identifier |
| `name` | String | NOT NULL, max 50 chars | Display name |
| `type` | TransactionType | NOT NULL | INCOME or EXPENSE |
| `createdAt` | Long | NOT NULL | Epoch millis when created |

**Validation Rules**:
- `name` must be non-empty
- `name` + `type` combination must be unique

**Default Data** (Expense):
- Alimentação, Transporte, Moradia, Utilidades, Saúde, Lazer, Educação, Outros

**Default Data** (Income):
- Salário, Investimentos, Freelance, Outros

---

### Transaction

Represents a financial movement (income or expense).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, auto-generated | Unique identifier |
| `accountId` | Long | FK → Account, NOT NULL | Associated bank account |
| `categoryId` | Long | FK → Category, NOT NULL | Associated category |
| `type` | TransactionType | NOT NULL | INCOME or EXPENSE |
| `amountCents` | Long | NOT NULL, > 0 | Amount in cents (R$ 123.45 = 12345) |
| `date` | Long | NOT NULL | Epoch day (days since 1970-01-01) |
| `description` | String | max 500 chars | Optional long description |
| `createdAt` | Long | NOT NULL | Epoch millis when created |
| `updatedAt` | Long | NOT NULL | Epoch millis when last updated |

**Validation Rules**:
- `amountCents` must be positive (> 0)
- `amountCents` max: 99999999999 (R$ 999,999,999.99)
- `date` defaults to today if not specified
- `type` must match the category's type
- `accountId` must reference an existing account
- `categoryId` must reference an existing category

**Cascade Rules**:
- Account deletion: RESTRICT (must reassign or delete transactions first)
- Category deletion: RESTRICT (must reassign or delete transactions first)

---

## Enums

### TransactionType

```kotlin
enum class TransactionType {
    INCOME,   // Money coming in
    EXPENSE   // Money going out
}
```

---

## Indexes

| Table | Index Name | Columns | Purpose |
|-------|------------|---------|---------|
| Transaction | `idx_transaction_date` | `date` | Date range queries |
| Transaction | `idx_transaction_type` | `type` | Filter by income/expense |
| Transaction | `idx_transaction_category` | `categoryId` | Filter by category |
| Transaction | `idx_transaction_account` | `accountId` | Filter by account |
| Category | `idx_category_type` | `type` | Filter categories by type |

---

## Room Implementation Notes

### TypeConverters

```kotlin
class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)
}
```

### Foreign Key Constraints

```kotlin
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("date"),
        Index("type"),
        Index("categoryId"),
        Index("accountId")
    ]
)
data class TransactionEntity(...)
```

---

## Query Patterns

### Monthly Dashboard Summary

```sql
SELECT
    SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END) as totalIncome,
    SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END) as totalExpense
FROM transactions
WHERE date >= :startDay AND date < :endDay
```

### Top Expense Categories

```sql
SELECT c.id, c.name, SUM(t.amountCents) as total
FROM transactions t
INNER JOIN categories c ON t.categoryId = c.id
WHERE t.type = 'EXPENSE'
  AND t.date >= :startDay
  AND t.date < :endDay
GROUP BY t.categoryId
ORDER BY total DESC
LIMIT :limit
```

### Filtered Transaction List

```sql
SELECT t.*, c.name as categoryName, a.name as accountName
FROM transactions t
INNER JOIN categories c ON t.categoryId = c.id
INNER JOIN accounts a ON t.accountId = a.id
WHERE (:type IS NULL OR t.type = :type)
  AND (:categoryId IS NULL OR t.categoryId = :categoryId)
  AND (:accountId IS NULL OR t.accountId = :accountId)
  AND (:startDate IS NULL OR t.date >= :startDate)
  AND (:endDate IS NULL OR t.date <= :endDate)
ORDER BY t.date DESC, t.createdAt DESC
```

---

## Migration Strategy

### Version 1 (Initial)

- Create all tables with constraints
- Prepopulate default categories and account via `RoomDatabase.Callback`

### Future Migrations

- Always preserve user data
- Add columns with defaults, never remove
- Test migrations with `MigrationTestHelper`
