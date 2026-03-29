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
    const val ADD_TRANSACTION = "transaction/add"
    const val EDIT_TRANSACTION = "transaction/edit/{transactionId}"
    const val CATEGORIES = "categories"
    const val ACCOUNTS = "accounts"

    fun editTransaction(transactionId: Long) = "transaction/edit/$transactionId"

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

    // Only show drawer on main screens (Dashboard, Transaction List)
    val showDrawer = currentRoute == Routes.DASHBOARD || currentRoute.startsWith(Routes.TRANSACTION_LIST_BASE)

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
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(Routes.DASHBOARD) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
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

            // Add Transaction
            composable(Routes.ADD_TRANSACTION) {
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
        }
    }
}

