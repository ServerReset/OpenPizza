package com.openpizza.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openpizza.app.ui.screens.*
import com.openpizza.app.ui.theme.OpenPizzaTheme
import com.openpizza.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenPizzaTheme {
                OpenPizzaApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    data object Home : Screen("home", "Home", { Icon(Icons.Filled.Home, contentDescription = "Home") })
    data object Stores : Screen("stores", "Stores", { Icon(Icons.Filled.LocationOn, contentDescription = "Stores") })
    data object Menu : Screen("menu", "Menu", { Icon(Icons.Filled.MenuBook, contentDescription = "Menu") })
    data object Builder : Screen("builder", "Build", { Icon(Icons.Filled.Build, contentDescription = "Builder") })
    data object Cart : Screen("cart", "Cart", { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") })
    data object Tracking : Screen("tracking", "Track", { Icon(Icons.Filled.TrackChanges, contentDescription = "Track") })
    data object Profile : Screen("profile", "Profile", { Icon(Icons.Filled.Person, contentDescription = "Profile") })

    companion object {
        val bottomNavItems = listOf(Home, Menu, Builder, Cart, Tracking)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenPizzaApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val cart by viewModel.cart.collectAsState()
    val cartItemCount = cart.sumOf { it.qty }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "OpenPizza",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if (screen == Screen.Cart && cartItemCount > 0) {
                                    Badge { Text(cartItemCount.toString()) }
                                }
                            }) {
                                screen.icon()
                            }
                        },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Stores.route) {
                StoreScreen(
                    viewModel = viewModel,
                    onStoreSelected = {
                        navController.navigate(Screen.Builder.route)
                    }
                )
            }
            composable(Screen.Menu.route) {
                MenuScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Builder.route) {
                BuilderScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Tracking.route) {
                TrackingScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
