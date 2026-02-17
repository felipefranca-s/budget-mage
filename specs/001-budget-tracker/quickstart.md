# Quickstart: Budget Mage

**Branch**: `001-budget-tracker` | **Date**: 2025-01-20

This guide describes how to set up, build, and test the Budget Mage Android app.

---

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or later
- **Android SDK**: API 34 (target), API 26 (minimum)
- **Gradle**: 8.2+ (bundled with project)

---

## Project Setup

### 1. Clone and Open

```bash
git clone <repository-url>
cd budget-mage
```

Open the project in Android Studio via `File → Open`.

### 2. Sync Gradle

Android Studio should automatically sync. If not:
- Click `File → Sync Project with Gradle Files`
- Or run: `./gradlew build`

### 3. Configure Emulator

Create an AVD (Android Virtual Device):
- Device: Pixel 6 (or similar)
- System Image: API 34 (or latest)
- Also create a tablet emulator for UI testing (e.g., Pixel Tablet)

---

## Build & Run

### Debug Build

```bash
./gradlew assembleDebug
```

Or click the green "Run" button in Android Studio.

### Release Build

```bash
./gradlew assembleRelease
```

Note: Release builds require signing configuration.

### Install on Device

```bash
./gradlew installDebug
```

---

## Testing

### Unit Tests

Run all unit tests:
```bash
./gradlew test
```

Run specific test class:
```bash
./gradlew test --tests "com.budgetmage.data.repository.TransactionRepositoryTest"
```

### Instrumentation Tests

Requires running emulator or connected device:
```bash
./gradlew connectedAndroidTest
```

### Test Coverage

```bash
./gradlew testDebugUnitTestCoverage
```

---

## Verification Scenarios

These scenarios verify the app meets specification requirements.

### Scenario 1: First Launch (FR-012, FR-013)

**Steps**:
1. Install app on fresh emulator (or clear app data)
2. Launch app

**Expected**:
- Dashboard shows with zero totals
- Default categories exist (8 expense, 4 income)
- Default "Dinheiro" account exists

**Verify**:
```
Navigate to Categories → See default categories
Navigate to Accounts → See "Dinheiro" account
```

---

### Scenario 2: Add Transaction (US1, SC-001)

**Steps**:
1. From Dashboard, tap FAB (floating action button)
2. Enter amount: 150.00
3. Select type: Expense
4. Select category: Alimentação
5. Select account: Dinheiro
6. Leave date as today (default)
7. Add description: "Almoço no restaurante"
8. Tap Save

**Expected**:
- Transaction saved successfully
- Returned to previous screen
- Dashboard updates to show R$ 150.00 expense

**Timing**: Complete in under 15 seconds from launch (SC-001)

---

### Scenario 3: Transaction Validation (FR-002, Edge Case)

**Steps**:
1. Start adding a new transaction
2. Try to save without entering amount
3. Try to enter negative amount
4. Try to enter amount > 999,999,999.99

**Expected**:
- Error shown for missing amount
- Negative amounts rejected
- Amounts over max rejected

---

### Scenario 4: Filter Transactions (US2, SC-007)

**Steps**:
1. Add 5 transactions with different dates, types, categories
2. Navigate to Transaction List
3. Apply date range filter (last 7 days)
4. Apply type filter (Expense only)
5. Apply category filter
6. Clear all filters

**Expected**:
- Each filter narrows results correctly
- Filters combine (AND logic)
- Results appear within 1 second (SC-007)

---

### Scenario 5: Dashboard Monthly View (US3, SC-003)

**Steps**:
1. Add transactions in current month:
   - 2 income transactions (total R$ 3000)
   - 5 expense transactions (total R$ 1500)
2. Navigate to Dashboard
3. Verify totals
4. Change to previous month
5. Change back to current month

**Expected**:
- Income: R$ 3000.00
- Expenses: R$ 1500.00
- Balance: R$ 1500.00
- Top categories shown
- Loads within 2 seconds (SC-003)

---

### Scenario 6: Category Management (US4, FR-011)

**Steps**:
1. Navigate to Categories
2. Add new expense category: "Assinaturas"
3. Edit category name to "Assinaturas Digitais"
4. Add a transaction using this category
5. Try to delete the category

**Expected**:
- Category created successfully
- Name updated everywhere
- Delete blocked with warning about existing transactions

---

### Scenario 7: Account Management (US5, FR-011)

**Steps**:
1. Navigate to Accounts
2. Add new account: code="NU", name="Nubank"
3. Edit account name to "Nubank Conta Corrente"
4. Add transaction using this account
5. Try to delete the account

**Expected**:
- Account created successfully
- Name updated on associated transactions
- Delete blocked with warning

---

### Scenario 8: Data Persistence (SC-006)

**Steps**:
1. Add 3 transactions
2. Force close the app (swipe from recent apps)
3. Relaunch app
4. Navigate to Transaction List

**Expected**:
- All 3 transactions still present
- No data loss

---

### Scenario 9: Dark Mode (FR-014)

**Steps**:
1. Set device to Light mode
2. Verify app uses light theme
3. Set device to Dark mode
4. Verify app uses dark theme

**Expected**:
- App follows system theme automatically
- All screens readable in both modes

---

### Scenario 10: Edit/Delete Transaction (FR-015)

**Steps**:
1. Add a transaction
2. Tap on it to view details
3. Tap Edit
4. Change amount and category
5. Save
6. Tap on transaction again
7. Delete it
8. Confirm deletion

**Expected**:
- Edit updates all fields
- Delete requires confirmation
- Transaction removed from list

---

## Performance Benchmarks

| Metric | Target | How to Measure |
|--------|--------|----------------|
| Add transaction from launch | < 15s | Stopwatch from tap to save |
| Transaction list load (1000 items) | < 1s | Profiler or visual |
| Dashboard load | < 2s | Profiler or visual |
| Filter results | < 1s | Visual responsiveness |

---

## Troubleshooting

### Build Failures

```bash
# Clean build
./gradlew clean build

# Invalidate caches
# Android Studio → File → Invalidate Caches / Restart
```

### Database Issues

```bash
# Clear app data on emulator
adb shell pm clear com.budgetmage
```

### Emulator Performance

- Enable hardware acceleration (HAXM or Hyper-V)
- Use x86_64 system images
- Allocate sufficient RAM (2GB+)

---

## Project Structure Reference

```
app/src/main/java/com/budgetmage/
├── data/           # Database, entities, repositories
├── di/             # Hilt modules
├── ui/             # Compose screens and ViewModels
└── util/           # Formatters and extensions
```

See [plan.md](./plan.md) for full structure details.
