package com.example.guesthousemain.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.guesthousemain.ui.screens.ContactScreen
import com.example.guesthousemain.ui.screens.HomeScreen
import com.example.guesthousemain.ui.screens.ReservationScreen
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageScreen(globalNavController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val bottomNavController = rememberNavController()

    // Define bottom navigation items.
    val items = listOf(
        BottomNavItem(route = "reservation", label = "Reservation", icon = Icons.Default.Room),
        BottomNavItem(route = "home", label = "Home", icon = Icons.Default.Home),
        BottomNavItem(route = "contact", label = "Contact", icon = Icons.Default.Info)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(modifier = Modifier.width(250.dp)) {
                ModalDrawerSheet {
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Use a plain white background.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White)
            )

            // Title banner at the top with black background and rounded bottom corners.
            AnimatedTitle(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )

            // Content area placed in between.
            NavHost(
                navController = bottomNavController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, bottom = 110.dp) // Adjust these paddings as needed.
            ) {
                composable("reservation") { ReservationScreen() }
                composable("home") { HomeScreen() }
                composable("contact") { ContactScreen() }
            }

            // Bottom navigation bar anchored at the bottom with additional downward offset.
            BottomNavigationBar(
                navController = bottomNavController,
                items = items,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 16.dp) // Shifts the bottom nav further downward.
            )

            // Hamburger menu button at the top-left, using white tint.
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open menu",
                    tint = Color.White // White icon for contrast on black banner.
                )
            }
        }
    }
}

@Composable
fun AnimatedTitle(modifier: Modifier = Modifier) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animate vertical offset and fade-in.
    val animatedOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 20.dp,
        animationSpec = tween(durationMillis = 1000)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = modifier
            .offset(y = animatedOffsetY)
            .alpha(animatedAlpha)
            .fillMaxWidth()
            .background(color = Color.Black, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Guest House Portal",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp) // Fixed height for proper proportion.
    ) {
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
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
