package com.budgetmage.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgetmage.R
import com.budgetmage.ui.navigation.Routes
import java.time.YearMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    currentRoute: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )
        Divider(modifier = Modifier.padding(horizontal = 28.dp))
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_dashboard)) },
            selected = currentRoute == Routes.DASHBOARD,
            onClick = {
                scope.launch { drawerState.close() }
                if (currentRoute != Routes.DASHBOARD) {
                    onNavigate(Routes.DASHBOARD)
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_transactions)) },
            selected = currentRoute.startsWith(Routes.TRANSACTION_LIST_BASE),
            onClick = {
                scope.launch { drawerState.close() }
                val now = YearMonth.now()
                val startDate = now.atDay(1).toEpochDay()
                val endDate = now.atEndOfMonth().toEpochDay()
                onNavigate(
                    Routes.transactionListWithFilter(
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Payments, contentDescription = null) },
            label = { Text("Contas Fixas") },
            selected = currentRoute == Routes.PAYMENTS,
            onClick = {
                scope.launch { drawerState.close() }
                if (currentRoute != Routes.PAYMENTS) {
                    onNavigate(Routes.PAYMENTS)
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Flag, contentDescription = null) },
            label = { Text("Metas") },
            selected = currentRoute == Routes.GOALS,
            onClick = {
                scope.launch { drawerState.close() }
                if (currentRoute != Routes.GOALS) {
                    onNavigate(Routes.GOALS)
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Divider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Category, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_categories)) },
            selected = currentRoute == Routes.CATEGORIES,
            onClick = {
                scope.launch { drawerState.close() }
                if (currentRoute != Routes.CATEGORIES) {
                    onNavigate(Routes.CATEGORIES)
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_accounts)) },
            selected = currentRoute == Routes.ACCOUNTS,
            onClick = {
                scope.launch { drawerState.close() }
                if (currentRoute != Routes.ACCOUNTS) {
                    onNavigate(Routes.ACCOUNTS)
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
