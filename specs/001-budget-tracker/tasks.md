# Tasks: Budget Mage - Personal Finance Tracker

**Input**: Design documents from `/specs/001-budget-tracker/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Not explicitly requested in specification. Tests omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

- **Mobile (Android)**: `app/src/main/java/com/budgetmage/`
- **Resources**: `app/src/main/res/`
- **Tests**: `app/src/test/` (unit), `app/src/androidTest/` (instrumentation)

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Create Android project structure and configure dependencies

- [x] T001 Create Android project with Gradle Kotlin DSL in app/build.gradle.kts
- [x] T002 Configure Hilt dependency injection in app/build.gradle.kts and project build.gradle.kts
- [x] T003 [P] Configure Room database dependencies in app/build.gradle.kts
- [x] T004 [P] Configure Jetpack Compose dependencies in app/build.gradle.kts
- [x] T005 [P] Configure Navigation Compose dependency in app/build.gradle.kts
- [x] T006 Create package structure under app/src/main/java/com/budgetmage/ (data/, di/, ui/, util/)
- [x] T007 Create AndroidManifest.xml with application configuration in app/src/main/AndroidManifest.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database & Entities

- [x] T008 Create TransactionType enum in app/src/main/java/com/budgetmage/data/database/entity/TransactionType.kt
- [x] T009 [P] Create AccountEntity Room entity in app/src/main/java/com/budgetmage/data/database/entity/AccountEntity.kt
- [x] T010 [P] Create CategoryEntity Room entity in app/src/main/java/com/budgetmage/data/database/entity/CategoryEntity.kt
- [x] T011 Create TransactionEntity Room entity with foreign keys in app/src/main/java/com/budgetmage/data/database/entity/TransactionEntity.kt
- [x] T012 Create Converters for Room type conversion in app/src/main/java/com/budgetmage/data/database/converter/Converters.kt

### DAOs

- [x] T013 [P] Create AccountDao interface in app/src/main/java/com/budgetmage/data/database/dao/AccountDao.kt
- [x] T014 [P] Create CategoryDao interface in app/src/main/java/com/budgetmage/data/database/dao/CategoryDao.kt
- [x] T015 Create TransactionDao interface with filtering queries in app/src/main/java/com/budgetmage/data/database/dao/TransactionDao.kt

### Database & Prepopulation

- [x] T016 Create AppDatabase with prepopulation callback in app/src/main/java/com/budgetmage/data/database/AppDatabase.kt

### Repositories

- [x] T017 [P] Create AccountRepository in app/src/main/java/com/budgetmage/data/repository/AccountRepository.kt
- [x] T018 [P] Create CategoryRepository in app/src/main/java/com/budgetmage/data/repository/CategoryRepository.kt
- [x] T019 Create TransactionRepository in app/src/main/java/com/budgetmage/data/repository/TransactionRepository.kt

### Dependency Injection

- [x] T020 Create AppModule with Hilt providers in app/src/main/java/com/budgetmage/di/AppModule.kt

### Application & Activity

- [x] T021 Create BudgetMageApp Application class with @HiltAndroidApp in app/src/main/java/com/budgetmage/BudgetMageApp.kt
- [x] T022 Create MainActivity as single-activity host in app/src/main/java/com/budgetmage/MainActivity.kt

### Theme & Utilities

- [x] T023 [P] Create Color.kt with Material 3 color scheme in app/src/main/java/com/budgetmage/ui/theme/Color.kt
- [x] T024 [P] Create Type.kt with typography definitions in app/src/main/java/com/budgetmage/ui/theme/Type.kt
- [x] T025 Create Theme.kt with light/dark theme support in app/src/main/java/com/budgetmage/ui/theme/Theme.kt
- [x] T026 [P] Create CurrencyFormatter utility in app/src/main/java/com/budgetmage/util/CurrencyFormatter.kt
- [x] T027 [P] Create DateFormatter utility in app/src/main/java/com/budgetmage/util/DateFormatter.kt
- [x] T028 [P] Create Extensions.kt with BigDecimal/Long conversions in app/src/main/java/com/budgetmage/util/Extensions.kt

### Navigation

- [x] T029 Create NavGraph with route definitions in app/src/main/java/com/budgetmage/ui/navigation/NavGraph.kt

### Resources

- [x] T030 [P] Create strings.xml with Portuguese strings in app/src/main/res/values/strings.xml
- [x] T031 [P] Create themes.xml for light theme in app/src/main/res/values/themes.xml
- [x] T032 [P] Create themes.xml for dark theme in app/src/main/res/values-night/themes.xml

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Record a Transaction (Priority: P1) 🎯 MVP

**Goal**: Users can add income and expense transactions with all required fields

**Independent Test**: Add a transaction with account, date, type, amount, category, description and verify it appears in a basic list

### Shared Components

- [x] T033 [US1] Create TransactionItem composable in app/src/main/java/com/budgetmage/ui/components/TransactionItem.kt
- [x] T034 [US1] Create ConfirmDeleteDialog composable in app/src/main/java/com/budgetmage/ui/components/ConfirmDeleteDialog.kt

### Add/Edit Transaction Screen

- [x] T035 [US1] Create AddEditTransactionViewModel in app/src/main/java/com/budgetmage/ui/transaction/AddEditTransactionViewModel.kt
- [x] T036 [US1] Create AddEditTransactionScreen composable in app/src/main/java/com/budgetmage/ui/transaction/AddEditTransactionScreen.kt

### Basic Transaction List (for verification)

- [x] T037 [US1] Create TransactionListViewModel with basic list loading in app/src/main/java/com/budgetmage/ui/transaction/TransactionListViewModel.kt
- [x] T038 [US1] Create TransactionListScreen with FAB and basic list in app/src/main/java/com/budgetmage/ui/transaction/TransactionListScreen.kt

### Navigation Integration

- [x] T039 [US1] Wire transaction screens into NavGraph in app/src/main/java/com/budgetmage/ui/navigation/NavGraph.kt

**Checkpoint**: User Story 1 complete - can add transactions and see them in a list

---

## Phase 4: User Story 2 - View and Filter Transactions (Priority: P2)

**Goal**: Users can view transaction history with filters by date, type, category, account

**Independent Test**: Add transactions with different attributes, apply filters, verify correct results

### Filter Components

- [x] T040 [US2] Create FilterBottomSheet composable in app/src/main/java/com/budgetmage/ui/components/FilterBottomSheet.kt
- [x] T041 [US2] Create CategoryChip composable in app/src/main/java/com/budgetmage/ui/components/CategoryChip.kt

### Enhanced Transaction List

- [x] T042 [US2] Add filter state and logic to TransactionListViewModel in app/src/main/java/com/budgetmage/ui/transaction/TransactionListViewModel.kt
- [x] T043 [US2] Add filter UI and transaction details to TransactionListScreen in app/src/main/java/com/budgetmage/ui/transaction/TransactionListScreen.kt

### Edit/Delete Transaction

- [x] T044 [US2] Add edit functionality to AddEditTransactionViewModel in app/src/main/java/com/budgetmage/ui/transaction/AddEditTransactionViewModel.kt
- [x] T045 [US2] Add delete confirmation flow in TransactionListScreen in app/src/main/java/com/budgetmage/ui/transaction/TransactionListScreen.kt

**Checkpoint**: User Story 2 complete - can filter and manage transactions

---

## Phase 5: User Story 3 - View Monthly Dashboard (Priority: P3)

**Goal**: Users see monthly summary with totals and top expense categories

**Independent Test**: Add transactions in current month, verify dashboard shows correct totals and category breakdown

### Dashboard Components

- [x] T046 [US3] Create MonthSelector composable in app/src/main/java/com/budgetmage/ui/components/MonthSelector.kt

### Dashboard Screen

- [x] T047 [US3] Create DashboardViewModel with aggregation queries in app/src/main/java/com/budgetmage/ui/dashboard/DashboardViewModel.kt
- [x] T048 [US3] Create DashboardScreen with summary cards and top categories in app/src/main/java/com/budgetmage/ui/dashboard/DashboardScreen.kt

### Navigation Integration

- [x] T049 [US3] Set Dashboard as home screen and add navigation from category tap in app/src/main/java/com/budgetmage/ui/navigation/NavGraph.kt

**Checkpoint**: User Story 3 complete - dashboard shows monthly financial summary

---

## Phase 6: User Story 4 - Manage Categories (Priority: P4)

**Goal**: Users can create, edit, and delete custom categories

**Independent Test**: Create new category, edit its name, verify it appears in transaction form, test delete with usage warning

### Category Management Screen

- [x] T050 [US4] Create CategoryListViewModel in app/src/main/java/com/budgetmage/ui/category/CategoryListViewModel.kt
- [x] T051 [US4] Create AddEditCategoryDialog composable in app/src/main/java/com/budgetmage/ui/category/AddEditCategoryDialog.kt
- [x] T052 [US4] Create CategoryListScreen with CRUD operations in app/src/main/java/com/budgetmage/ui/category/CategoryListScreen.kt

### Navigation Integration

- [x] T053 [US4] Add category management route to NavGraph in app/src/main/java/com/budgetmage/ui/navigation/NavGraph.kt

**Checkpoint**: User Story 4 complete - can manage custom categories

---

## Phase 7: User Story 5 - Manage Bank Accounts (Priority: P5)

**Goal**: Users can create, edit, and delete bank accounts

**Independent Test**: Create new account, edit its details, verify it appears in transaction form, test delete with usage warning

### Account Management Screen

- [x] T054 [US5] Create AccountListViewModel in app/src/main/java/com/budgetmage/ui/account/AccountListViewModel.kt
- [x] T055 [US5] Create AddEditAccountDialog composable in app/src/main/java/com/budgetmage/ui/account/AddEditAccountDialog.kt
- [x] T056 [US5] Create AccountListScreen with CRUD operations in app/src/main/java/com/budgetmage/ui/account/AccountListScreen.kt

### Navigation Integration

- [x] T057 [US5] Add account management route to NavGraph in app/src/main/java/com/budgetmage/ui/navigation/NavGraph.kt

**Checkpoint**: User Story 5 complete - can manage bank accounts

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements affecting multiple user stories

- [x] T058 Add navigation menu/drawer to access all screens from MainActivity in app/src/main/java/com/budgetmage/MainActivity.kt
- [x] T059 Add empty state messages to all list screens (transactions, categories, accounts)
- [x] T060 Add loading indicators to all screens during data operations
- [x] T061 Verify FAB accessibility from all main screens for quick transaction entry
- [x] T062 Run quickstart.md validation scenarios to verify all acceptance criteria
- [x] T063 Create .gitignore with Android-specific patterns in project root

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - can start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 - BLOCKS all user stories
- **Phases 3-7 (User Stories)**: All depend on Phase 2 completion
  - US1 (P1): No dependencies on other stories
  - US2 (P2): Builds on US1 components (TransactionListScreen)
  - US3 (P3): Independent, uses shared repositories
  - US4 (P4): Independent, simple CRUD
  - US5 (P5): Independent, simple CRUD
- **Phase 8 (Polish)**: Depends on all user stories being complete

### User Story Dependencies

```
Phase 2 (Foundation)
       │
       ├──► US1 (Record Transaction) ──► US2 (View/Filter)
       │
       ├──► US3 (Dashboard) ─────────────────────────────►┐
       │                                                   │
       ├──► US4 (Categories) ─────────────────────────────►├──► Phase 8 (Polish)
       │                                                   │
       └──► US5 (Accounts) ───────────────────────────────►┘
```

### Parallel Opportunities

**Phase 1**:
- T003, T004, T005 can run in parallel (dependency configs)

**Phase 2**:
- T009, T010 can run in parallel (Account, Category entities)
- T013, T014 can run in parallel (Account, Category DAOs)
- T017, T018 can run in parallel (Account, Category repositories)
- T023, T024 can run in parallel (Color, Type theme files)
- T026, T027, T028 can run in parallel (utilities)
- T030, T031, T032 can run in parallel (resource files)

**User Stories** (after Phase 2):
- US3, US4, US5 can run in parallel (independent features)

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test adding and viewing transactions
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 → MVP: Can add transactions
3. Add US2 → Can filter and manage transactions
4. Add US3 → Dashboard with insights
5. Add US4 → Custom categories
6. Add US5 → Multiple accounts
7. Polish → Final refinements

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story
- Each user story independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Portuguese strings for Brazilian user (per research.md)
