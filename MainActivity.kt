package com.ntvelop.goldengoosepda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.ntvelop.goldengoosepda.feature_auth.ui.LoginScreen
import com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel
import com.ntvelop.goldengoosepda.feature_tables.ui.TableMapScreen
import com.ntvelop.goldengoosepda.feature_tables.ui.SettingsDialog
import com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel
import com.ntvelop.goldengoosepda.feature_orders.ui.OrderScreen
import com.ntvelop.goldengoosepda.feature_orders.ui.OpenOrdersScreen
import com.ntvelop.goldengoosepda.feature_orders.ui.PaidOrdersScreen
import com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel
import com.ntvelop.goldengoosepda.feature_admin.ui.AdminLogsScreen
import com.ntvelop.goldengoosepda.feature_admin.ui.AdminTotalsScreen

import com.ntvelop.goldengoosepda.network.TokenManager
import com.ntvelop.goldengoosepda.network.SettingsManager
import com.ntvelop.goldengoosepda.ui.theme.GoldenGoosePDATheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenGoosePDATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoldenGooseAppNavigation(tokenManager, settingsManager)
                }
            }
        }
    }
}

@Composable
fun GoldenGooseAppNavigation(
    tokenManager: TokenManager,
    settingsManager: SettingsManager
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showSettingsDialog by remember { mutableStateOf(false) }
    val userRole = tokenManager.getRole() ?: "WAITER"
    val isAdmin = userRole == "ADMIN" || userRole == "MANAGER"

    val showDrawer = currentRoute != "login" && currentRoute != "setup"

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            if (showDrawer) {
                ModalDrawerSheet {
                    Spacer(Modifier.height(12.dp))
                    Text("GOLDEN GOOSE POS", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Χάρτης Τραπεζιών") },
                        selected = currentRoute == "table_map",
                        onClick = {
                            navController.navigate("table_map") { launchSingleTop = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    if (!isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("Ανοιχτές Παραγγελίες") },
                            selected = currentRoute == "open_orders",
                            onClick = {
                                navController.navigate("open_orders") { launchSingleTop = true }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Πληρωμένες Παραγγελίες") },
                            selected = currentRoute == "paid_orders",
                            onClick = {
                                navController.navigate("paid_orders") { launchSingleTop = true }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    
                    if (isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("Σύνολα (Admin)") },
                            selected = currentRoute == "admin_totals",
                            onClick = {
                                navController.navigate("admin_totals") { launchSingleTop = true }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Ιστορικό (Admin)") },
                            selected = currentRoute == "admin_logs",
                            onClick = {
                                navController.navigate("admin_logs") { launchSingleTop = true }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text("Ρυθμίσεις (Admin)") },
                            selected = currentRoute == "admin_settings",
                            onClick = {
                                navController.navigate("admin_settings") { launchSingleTop = true }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    if (!isAdmin) {
                        NavigationDrawerItem(
                            label = { Text("Ρυθμίσεις") },
                            selected = false,
                            onClick = {
                                showSettingsDialog = true
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    NavigationDrawerItem(
                        label = { Text("Αποσύνδεση", color = MaterialTheme.colorScheme.error) },
                        selected = false,
                        onClick = {
                            tokenManager.clear()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                val viewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate("table_map") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onSetupClick = {
                        navController.navigate("setup")
                    }
                )
            }
            composable("setup") {
                val viewModel: com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel = hiltViewModel()
                com.ntvelop.goldengoosepda.feature_setup.ui.SetupScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("table_map") {
                val viewModel: TableMapViewModel = hiltViewModel()
                val currentUserId = tokenManager.getUserId() ?: ""
                TableMapScreen(
                    viewModel = viewModel,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onTableSelected = { orderId ->
                        navController.navigate("order/$orderId")
                    },
                    isReadOnly = isAdmin,
                    currentUserId = currentUserId
                )
            }
            composable("order/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val viewModel: OrderViewModel = hiltViewModel()
                OrderScreen(
                    orderId = orderId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("open_orders") {
                OpenOrdersScreen(onBack = { navController.popBackStack() })
            }
            composable("paid_orders") {
                PaidOrdersScreen(onBack = { navController.popBackStack() })
            }
            composable("admin_totals") {
                AdminTotalsScreen(onBack = { navController.popBackStack() })
            }
            composable("admin_logs") {
                AdminLogsScreen(onBack = { navController.popBackStack() })
            }
            composable("admin_settings") {
                val viewModel: com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel = hiltViewModel()
                com.ntvelop.goldengoosepda.feature_admin.ui.AdminSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }


    if (showSettingsDialog) {
        SettingsDialog(
            initialName = settingsManager.getWaiterName() ?: "",
            onDismiss = { showSettingsDialog = false },
            onSave = {
                settingsManager.setWaiterName(it)
                showSettingsDialog = false
            }
        )
    }
}
