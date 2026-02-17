# Implementation Plan: Budget Mage - Personal Finance Tracker

**Branch**: `001-budget-tracker` | **Date**: 2025-01-20 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-budget-tracker/spec.md`

## Summary

Budget Mage is a personal finance tracking Android app that allows users to record income and expense transactions, organize them by categories and bank accounts, and view monthly spending summaries on a dashboard. The app prioritizes simplicity and speed of transaction entry, stores all data locally without requiring authentication, and follows Material Design 3 guidelines with Jetpack Compose UI.

## Technical Context

**Language/Version**: Kotlin 1.9+ (latest stable)
**Primary Dependencies**: Jetpack Compose, Room, Hilt, Material Design 3
**Storage**: Room with SQLite (local only, no network)
**Testing**: JUnit 5 + Compose UI testing + Espresso
**Target Platform**: Android 8.0+ (API 26+)
**Project Type**: Mobile (single Android app)
**Performance Goals**: Transaction entry < 15s from launch, list loads < 1s for 1000 items, dashboard < 2s
**Constraints**: Offline-only, no network permissions, local data persistence
**Scale/Scope**: Single user, ~5 screens, up to 10,000 transactions

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Simplicity First (YAGNI) | ✅ PASS | 5 screens only, single purpose per screen, no extra features |
| II. Local-First Data | ✅ PASS | Room/SQLite only, no network, no auth, no telemetry |
| III. Fast Transaction Entry | ✅ PASS | FAB on all screens, smart defaults, auto-focus amount |
| IV. Data Integrity | ✅ PASS | BigDecimal for amounts, Room transactions, delete confirmations |
| V. Android Platform Conventions | ✅ PASS | Compose + Material 3, MVVM, system theme, API 26+ |

**Gate Result**: All principles satisfied. Proceeding to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/001-budget-tracker/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (internal contracts for this app)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── java/com/budgetmage/
│   │   │   ├── BudgetMageApp.kt           # Application class with Hilt
│   │   │   ├── MainActivity.kt            # Single activity host
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── AppDatabase.kt     # Room database
│   │   │   │   │   ├── entity/            # Room entities
│   │   │   │   │   │   ├── TransactionEntity.kt
│   │   │   │   │   │   ├── CategoryEntity.kt
│   │   │   │   │   │   └── AccountEntity.kt
│   │   │   │   │   ├── dao/               # Data Access Objects
│   │   │   │   │   │   ├── TransactionDao.kt
│   │   │   │   │   │   ├── CategoryDao.kt
│   │   │   │   │   │   └── AccountDao.kt
│   │   │   │   │   └── converter/         # Type converters
│   │   │   │   │       └── Converters.kt
│   │   │   │   └── repository/
│   │   │   │       ├── TransactionRepository.kt
│   │   │   │       ├── CategoryRepository.kt
│   │   │   │       └── AccountRepository.kt
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt           # Hilt modules
│   │   │   ├── ui/
│   │   │   │   ├── navigation/
│   │   │   │   │   └── NavGraph.kt        # Navigation setup
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── dashboard/
│   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   └── DashboardViewModel.kt
│   │   │   │   ├── transaction/
│   │   │   │   │   ├── TransactionListScreen.kt
│   │   │   │   │   ├── TransactionListViewModel.kt
│   │   │   │   │   ├── AddEditTransactionScreen.kt
│   │   │   │   │   └── AddEditTransactionViewModel.kt
│   │   │   │   ├── category/
│   │   │   │   │   ├── CategoryListScreen.kt
│   │   │   │   │   ├── CategoryListViewModel.kt
│   │   │   │   │   └── AddEditCategoryDialog.kt
│   │   │   │   ├── account/
│   │   │   │   │   ├── AccountListScreen.kt
│   │   │   │   │   ├── AccountListViewModel.kt
│   │   │   │   │   └── AddEditAccountDialog.kt
│   │   │   │   └── components/
│   │   │   │       ├── TransactionItem.kt
│   │   │   │       ├── CategoryChip.kt
│   │   │   │       ├── MonthSelector.kt
│   │   │   │       ├── FilterBottomSheet.kt
│   │   │   │       └── ConfirmDeleteDialog.kt
│   │   │   └── util/
│   │   │       ├── CurrencyFormatter.kt
│   │   │       ├── DateFormatter.kt
│   │   │       └── Extensions.kt
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── values-night/
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   ├── test/                              # Unit tests
│   │   └── java/com/budgetmage/
│   │       ├── data/repository/
│   │       └── ui/
│   └── androidTest/                       # Instrumentation tests
│       └── java/com/budgetmage/
│           ├── data/database/
│           └── ui/
├── gradle/
└── settings.gradle.kts
```

**Structure Decision**: Single Android app following the constitution's prescribed structure. Uses feature-based package organization under `ui/` with shared components. Data layer follows Repository pattern with Room database.

## Complexity Tracking

> No violations to track. Design aligns with all constitution principles.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
