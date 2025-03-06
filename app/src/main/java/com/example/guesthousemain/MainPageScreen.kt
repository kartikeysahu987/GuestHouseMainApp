package com.example.guesthousemain.ui

import ContactScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.guesthousemain.R
import com.example.guesthousemain.ui.screens.*
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun GuestHouseApp(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF7E22CE), // Purple primary color
            surface = Color(0xFFF5F3FF), // Light purple surface
            background = Color.White,
            onBackground = Color(0xFF1A1A1A)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF7E22CE), // Purple primary color
            surface = Color(0xFFF5F3FF), // Light purple surface
            background = Color.White,
            onBackground = Color(0xFF1A1A1A)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageScreen(globalNavController: NavHostController) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem("reservation", "Reservation", Icons.Outlined.LocationOn, Color(0xFF9CA3AF)),
        BottomNavItem("home", "Home", Icons.Filled.Home, Color(0xFF7E22CE)),
        BottomNavItem("contact", "Contact", Icons.Outlined.Info, Color(0xFF9CA3AF))
    )

    MaterialTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                DrawerContent(
                    onLogout = {
                        SessionManager.clearTokens(context)
                        scope.launch { drawerState.close() }
                        globalNavController.navigate("login") {
                            popUpTo(globalNavController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { CleanMinimalistAppBar(scope, drawerState) },
                bottomBar = { ModernBottomNavigation(navController = bottomNavController, items = items) },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                NavHost(
                    navController = bottomNavController,
                    startDestination = "home",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("reservation") { ReservationScreen() }
                    composable("home") { HomeScreen(bottomNavController) }
                    composable("contact") { ContactScreen() }
                    // Add the missing route for reservation_form
                    composable("reservation_form") { ReservationFormScreen() }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(onLogout: () -> Unit) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.background,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo or icon could go here
                Image(
                    painter = painterResource(id = R.drawable.iit_ropar_logo),
                    contentDescription = "IIT Ropar Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = "Guest House",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = { },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CleanMinimalistAppBar(scope: CoroutineScope, drawerState: DrawerState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Menu icon
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF1A1A1A)
                )
            }

            // Title
            Text(
                text = "Guest House",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                color = Color(0xFF1A1A1A)
            )

            // Notification icon
            IconButton(
                onClick = { },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1A1A1A)
                )
            }
        }

        // Bottom divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}

@Composable
fun ModernBottomNavigation(navController: NavHostController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F3FF))
    ) {
        // Navigation items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                val itemColor = if (selected) item.selectedColor else Color(0xFF9CA3AF)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = itemColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom progress indicator
        LinearProgressIndicator(
            progress = { 0.3f }, // This is just for visual effect
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color = Color(0xFFDDDDDD),
            trackColor = Color.Transparent
        )
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    // Using your existing images from the drawable folder
    val imageResources = listOf(
        R.drawable.beas,
        R.drawable.cs,
        R.drawable.dining_hall,
        R.drawable.ee,
        R.drawable.ele,
        R.drawable.entrance,
        R.drawable.gh,
        R.drawable.guest,
        R.drawable.guesthouse_background,
        R.drawable.hs,
        R.drawable.lhc,
        R.drawable.lib
    )

    // State for tracking current image
    var currentImageIndex by remember { mutableStateOf(0) }

    // Auto-scroll functionality
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(2000) // 3 seconds delay before auto-scrolling
            currentImageIndex = (currentImageIndex + 1) % imageResources.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Image Carousel with actual images
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Display the current image
            Image(
                painter = painterResource(id = imageResources[currentImageIndex]),
                contentDescription = "Guest House Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Left and right navigation arrows
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                IconButton(
                    onClick = {
                        currentImageIndex = if (currentImageIndex > 0)
                            currentImageIndex - 1 else imageResources.size - 1
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Image",
                        tint = Color.White
                    )
                }

                // Next button
                IconButton(
                    onClick = {
                        currentImageIndex = (currentImageIndex + 1) % imageResources.size
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Image",
                        tint = Color.White
                    )
                }
            }

            // Semi-transparent overlay at the bottom with image info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = getImageTitle(currentImageIndex),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Carousel indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(imageResources.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == currentImageIndex) MaterialTheme.colorScheme.primary
                            else Color(0xFFD1D5DB)
                        )
                        .clickable { currentImageIndex = index }
                )
            }
        }

        // Welcome text
        Text(
            text = "Welcome to IIT Ropar Guest House",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Enjoy a comfortable stay with modern amenities, great dining, and friendly staff. Whether you're here for a conference, family vacation, or just passing through, we strive to make your experience memorable and relaxing.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rooms card
            FeatureCard(
                icon = Icons.Outlined.LocationOn,
                title = "Rooms",
                description = "Comfortable rooms for every budget.",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Events card
            FeatureCard(
                icon = Icons.Outlined.DateRange,
                title = "Events",
                description = "Conference halls & event spaces.",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // About card
            FeatureCard(
                icon = Icons.Outlined.Info,
                title = "About",
                description = "Learn more about our story.",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Book Now button - CORRECTED to navigate to the existing route


        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Add a stub for the ReservationFormScreen that was missing
@Composable
fun ReservationFormScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reservation Form",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This form will be implemented soon.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Helper function to get image titles
@Composable
fun getImageTitle(index: Int): String {
    return when(index) {
        0 -> "Beas Hostel"
        1 -> "Computer Science Department"
        2 -> "Dining Hall"
        3 -> "Electrical Engineering Department"
        4 -> "Electronics Building"
        5 -> "Campus Entrance"
        6 -> "Guest House"
        7 -> "Guest Accommodation"
        8 -> "Guest House Overview"
        9 -> "Hostels Area"
        10 -> "Lecture Hall Complex"
        11 -> "Library"
        else -> "IIT Ropar Campus"
    }
}

@Composable
fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F3FF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedColor: Color
)