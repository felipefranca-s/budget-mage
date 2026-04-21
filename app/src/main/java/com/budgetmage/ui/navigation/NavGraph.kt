package com.budgetmage.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.budgetmage.ui.account.AccountListScreen
import com.budgetmage.ui.category.CategoryListScreen
import com.budgetmage.ui.components.AppDrawer
import com.budgetmage.ui.dashboard.DashboardScreen
import com.budgetmage.ui.goal.AddEditGoalScreen
import com.budgetmage.ui.goal.GoalListScreen
import com.budgetmage.ui.payment.AddEditPaymentScreen
import com.budgetmage.ui.payment.PaymentListScreen
import com.budgetmage.ui.transaction.AddEditTransactionScreen
import com.budgetmage.ui.transaction.TransactionListScreen
import java.time.YearMonth
import kotlinx.coroutines.launch

/**
 * Navigation route definitions.
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val TRANSACTION_LIST = "transactions?categoryId={categoryId}&startDate={startDate}&endDate={endDate}"
    const val TRANSACTION_LIST_BASE = "transactions"
    const val ADD_TRANSACTION = "transaction/add?amountCents={amountCents}&categoryId={categoryId}&accountId={accountId}&description={description}&paymentId={paymentId}&yearMonth={yearMonth}"
    const val ADD_TRANSACTION_BASE = "transaction/add"
    const val EDIT_TRANSACTION = "transaction/edit/{transactionId}"
    const val CATEGORIES = "categories"
    const val ACCOUNTS = "accounts"
    const val PAYMENTS = "payments"
    const val ADD_PAYMENT = "payment/add"
    const val EDIT_PAYMENT = "payment/edit/{paymentId}"
    const val GOALS = "goals"
    const val ADD_GOAL = "goal/add"
    const val EDIT_GOAL = "goal/edit/{goalId}"

    fun editTransaction(transactionId: Long) = "transaction/edit/$transactionId"

    fun editPayment(paymentId: Long) = "payment/edit/$paymentId"

    fun editGoal(goalId: Long) = "goal/edit/$goalId"

    fun addTransactionFromPayment(
        paymentId: Long,
        yearMonth: Int,
        amountCents: Long,
        categoryId: Long,
        accountId: Long,
        description: String
    ): String {
        val params = mutableListOf<String>()
        params.add("amountCents=$amountCents")
        params.add("categoryId=$categoryId")
        params.add("accountId=$accountId")
        params.add("description=$description")
        params.add("paymentId=$paymentId")
        params.add("yearMonth=$yearMonth")
        return "$ADD_TRANSACTION_BASE?${params.joinToString("&")}"
    }

    fun transactionListWithFilter(
        categoryId: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): String {
        val params = mutableListOf<String>()
        categoryId?.let { params.add("categoryId=$it") }
        startDate?.let { params.add("startDate=$it") }
        endDate?.let { params.add("endDate=$it") }
        return if (params.isEmpty()) TRANSACTION_LIST_BASE
        else "$TRANSACTION_LIST_BASE?${params.joinToString("&")}"
    }
}

/**
 * Main navigation graph for the app.
 */
@Composable
fun BudgetMageNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.DASHBOARD
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.DASHBOARD

    // Only show drawer on main screens (Dashboard, Transaction List, Payments, Goals)
    val showDrawer = currentRoute == Routes.DASHBOARD ||
        currentRoute.startsWith(Routes.TRANSACTION_LIST_BASE) ||
        currentRoute == Routes.PAYMENTS ||
        currentRoute == Routes.GOALS

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                drawerState = drawerState,
                scope = scope,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.DASHBOARD) {
                            inclusive = route == Routes.DASHBOARD
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Transaction List
            composable(
                route = Routes.TRANSACTION_LIST,
                arguments = listOf(
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("startDate") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("endDate") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                TransactionListScreen(
                    onAddClick = { navController.navigate(Routes.ADD_TRANSACTION) },
                    onTransactionClick = { id -> navController.navigate(Routes.editTransaction(id)) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            // Add Transaction (with optional prefill args from payment flow)
            composable(
                route = Routes.ADD_TRANSACTION,
                arguments = listOf(
                    navArgument("amountCents") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("categoryId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("description") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("paymentId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("yearMonth") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                AddEditTransactionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Transaction
            composable(
                route = Routes.EDIT_TRANSACTION,
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.LongType }
                )
            ) {
                AddEditTransactionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Dashboard
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
                    onNavigateToTransactions = { navController.navigate(Routes.TRANSACTION_LIST_BASE) },
                    onCategoryClick = { categoryId, selectedMonth ->
                        val startDate = selectedMonth.atDay(1).toEpochDay()
                        val endDate = selectedMonth.atEndOfMonth().toEpochDay()
                        navController.navigate(
                            Routes.transactionListWithFilter(
                                categoryId = categoryId,
                                startDate = startDate,
                                endDate = endDate
                            )
                        )
                    },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            // Categories
            composable(Routes.CATEGORIES) {
                CategoryListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Accounts
            composable(Routes.ACCOUNTS) {
                AccountListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Payments (Contas Fixas)
            composable(Routes.PAYMENTS) {
                PaymentListScreen(
                    onAddPayment = { navController.navigate(Routes.ADD_PAYMENT) },
                    onEditPayment = { id -> navController.navigate(Routes.editPayment(id)) },
                    onMarkAsPaid = { paymentId, yearMonth, amountCents, categoryId, accountId, description ->
                        navController.navigate(
                            Routes.addTransactionFromPayment(
                                paymentId = paymentId,
                                yearMonth = yearMonth,
                                amountCents = amountCents,
                                categoryId = categoryId,
                                accountId = accountId,
                                description = description
                            )
                        )
                    },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            composable(Routes.ADD_PAYMENT) {
                AddEditPaymentScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT_PAYMENT,
                arguments = listOf(
                    navArgument("paymentId") { type = NavType.LongType }
                )
            ) {
                AddEditPaymentScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Goals (Metas)
            composable(Routes.GOALS) {
                GoalListScreen(
                    onAddGoal = { navController.navigate(Routes.ADD_GOAL) },
                    onEditGoal = { id -> navController.navigate(Routes.editGoal(id)) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            composable(Routes.ADD_GOAL) {
                AddEditGoalScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT_GOAL,
                arguments = listOf(
                    navArgument("goalId") { type = NavType.LongType }
                )
            ) {
                AddEditGoalScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

