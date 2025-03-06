package com.example.guesthousemain.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.guesthousemain.ui.screens.ContactScreen
import com.example.guesthousemain.ui.screens.HomeScreen
import com.example.guesthousemain.ui.screens.ReservationScreen
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageScreen(globalNavController: NavHostController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Create a separate NavController for bottom navigation.
    val bottomNavController = rememberNavController()

    // List of bottom nav items.
    val items = listOf(
        BottomNavItem(
            route = "reservation",
            label = "Reservation",
            icon = Icons.Default.Room
        ),
        BottomNavItem(
            route = "home",
            label = "Home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = "contact",
            label = "Contact",
            icon = Icons.Default.Info
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Wrap drawer content in a fixed width box.
            Box(modifier = Modifier.width(250.dp)) {
                ModalDrawerSheet {
                    // Optional header.
                    Text(
                        text = "Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider()
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            // Clear session tokens and navigate to login.
                            SessionManager.clearTokens(context)
                            scope.launch { drawerState.close() }
                            globalNavController.navigate("login") {
                                popUpTo(globalNavController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Guest House Portal") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open menu"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = bottomNavController, items = items)
            }
        ) { innerPadding ->
            // Bottom navigation content using bottomNavController.
            NavHost(
                navController = bottomNavController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("reservation") { ReservationScreen() }
                composable("home") { HomeScreen() }
                composable("contact") { ContactScreen() }
            }
        }
    }
}

// Data class to hold bottom nav item info.
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// Custom bottom navigation bar composable.
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = { Text(text = item.label) }
            )
        }
    }
}
