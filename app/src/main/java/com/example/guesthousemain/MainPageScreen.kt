package com.example.guesthousemain.ui

import ContactScreen
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.guesthousemain.ProfileScreen
import com.example.guesthousemain.R
import com.example.guesthousemain.SettingsScreen
import com.example.guesthousemain.ui.screens.*
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.guesthousemain.NotificationScreen
import com.example.guesthousemain.NotificationViewModel

@Composable
fun GuestHouseApp(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF9F70D0), // Lighter purple for dark theme
            onPrimary = Color.White,
            surface = Color(0xFF2D2D3A), // Dark purple surface
            onSurface = Color(0xFFE1E1E1),
            background = Color(0xFF121212), // Dark background
            onBackground = Color(0xFFE1E1E1),
            secondary = Color(0xFF7E22CE),
            onSecondary = Color.White,
            surfaceVariant = Color(0xFF3D3D4E),
            onSurfaceVariant = Color(0xFFE1E1E1),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF7E22CE), // Purple primary color
            onPrimary = Color.White,
            surface = Color(0xFFF5F3FF), // Light purple surface
            onSurface = Color(0xFF1A1A1A),
            background = Color.White,
            onBackground = Color(0xFF1A1A1A),
            secondary = Color(0xFF9F70D0),
            onSecondary = Color.White,
            surfaceVariant = Color(0xFFEEEAF4),
            onSurfaceVariant = Color(0xFF1A1A1A),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageScreen(globalNavController: NavHostController,notificationViewModel: NotificationViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val bottomNavController = rememberNavController()

    // Create a nested NavController for the drawer navigation
    val drawerNavController = rememberNavController()

    val items = listOf(
        BottomNavItem("reservation", "Reservation", Icons.Outlined.LocationOn, MaterialTheme.colorScheme.primary),
        BottomNavItem("home", "Home", Icons.Filled.Home, MaterialTheme.colorScheme.primary),
        BottomNavItem("contact", "Contact", Icons.Outlined.Info, MaterialTheme.colorScheme.primary)
    )

    MaterialTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                DrawerContent(
                    navController = drawerNavController,
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    },
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
                topBar = { CleanMinimalistAppBar(scope, drawerState, bottomNavController) },
                bottomBar = { ModernBottomNavigation(bottomNavController = bottomNavController,
                    drawerNavController = drawerNavController, items = items) },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                // This is for the main content (bottom navigation)
                Box(modifier = Modifier.padding(innerPadding)) {
                    // First NavHost for bottom navigation
                    NavHost(
                        navController = bottomNavController,
                        startDestination = "home"
                    ) {
                        composable("reservation") { ReservationScreen() }
                        composable("home") { HomeScreen(bottomNavController) }
                        composable("contact") { ContactScreen() }
                        composable("reservation_form") { ReservationFormScreen() }
                        composable("notifications") {
                            NotificationScreen(navigateBack = {bottomNavController.popBackStack() },
                                viewModel = notificationViewModel
                            )
                        }
                    }

                    // Second NavHost for drawer navigation (profile & settings)
                    // This will overlay the main content when active
                    NavHost(
                        navController = drawerNavController,
                        startDestination = "none" // Empty start destination
                    ) {
                        composable("none") { /* Empty screen */ }
                        composable("profile") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                ProfileScreen()
                            }
                        }
                        composable("settings") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                SettingsScreen(bottomNavController = bottomNavController,drawerNavController = drawerNavController)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Updated DrawerContent with navigation and theme support
@Composable
private fun DrawerContent(
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit
) {
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
                // Logo or icon
                Image(
                    painter = painterResource(id = R.drawable.iit_ropar_logo),
                    contentDescription = "IIT Ropar Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = "Guest House",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = {
                navController.navigate("profile")
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = {
                navController.navigate("settings")
                onCloseDrawer()
            },
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
private fun CleanMinimalistAppBar(scope: CoroutineScope, drawerState: DrawerState,bottomNavController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Title
            Text(
                text = "Guest House",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Notification icon
                IconButton(
                    onClick = {bottomNavController.navigate("notifications")},
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

        // Bottom divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            thickness = 1.dp
        )
    }
}

@Composable
fun ModernBottomNavigation(
    bottomNavController: NavHostController,
    drawerNavController: NavHostController, // New parameter for the overlay/drawer navigation
    items: List<BottomNavItem>
) {
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                val itemColor = if (selected) item.selectedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = {
                            // First clear any overlay in the drawer nav
                            if (drawerNavController.currentBackStackEntry?.destination?.route != "none") {
                                drawerNavController.navigate("none") {
                                    popUpTo("none") { inclusive = true }
                                }
                            }
                            // Then navigate the bottom nav host
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
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

        // Bottom progress indicator (for visual effect)
        LinearProgressIndicator(
            progress = { 0.3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
            trackColor = Color.Transparent
        )
    }
}

@Composable
fun WelcomeSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Main Heading
        Text(
            text = "Welcome to IIT Ropar's Guest House",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Subheading: The Guest House
        Text(
            text = "The Guest House",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Paragraph about the Guest House
        Text(
            text = "Nestled within the greenery of IIT Ropar's campus, the guest house offers a welcoming retreat for visitors. With its modern design blending seamlessly with the serene environment, it provides a comfortable and inviting atmosphere. Each room is well-appointed, combining tasteful decor with cozy furnishings for a relaxing stay. Whether guests are enjoying a peaceful walk through the gardens or focusing on academic endeavors, the guest house ensures a pleasant experience filled with comfort and hospitality.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Subheading: The Campus
        Text(
            text = "The Campus",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Paragraph about the Campus
        Text(
            text = "Nestled in the heart of Punjab's Rupnagar district, the campus of the Indian Institute of Technology, Ropar, is a blend of modern architecture and natural beauty spread across 500 acres. Walking through its green pathways, surrounded by lush trees and colorful flora, offers a sense of tranquility away from the outside world. The campus boasts sleek, contemporary buildings housing cutting-edge facilities for academics, research, and student life. Recreational spaces provide areas for students to relax and socialize amidst the serene surroundings. Sustainability efforts are evident throughout, showcasing a commitment to environmental conservation. From academic pursuits to cultural events, the campus buzzes with activity, creating a vibrant community where learning and growth thrive.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    // Get current context for launching intents
    val context = LocalContext.current

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
            kotlinx.coroutines.delay(2000) // 2 seconds delay before auto-scrolling
            currentImageIndex = (currentImageIndex + 1) % imageResources.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
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
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        .clickable { currentImageIndex = index }
                )
            }
        }

        WelcomeSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Feature cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Location card - Now with click functionality to open Google Maps
            FeatureCard(
                icon = Icons.Outlined.LocationOn,
                title = "Location",
                description = "Indian Institute of Technology, Ropar.",
                modifier = Modifier.weight(1f),
                onClick = {
                    // Open Google Maps with IIT Ropar's coordinates
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:30.9716,76.5305?q=IIT+Ropar"))
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Fallback to browser if Google Maps app is not installed
                        val browserIntent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=IIT+Ropar"))
                        context.startActivity(browserIntent)
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Events card
            FeatureCard(
                icon = Icons.Outlined.DateRange,
                title = "Events",
                description = "Conference halls & event spaces.",
                modifier = Modifier.weight(1f),
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.iitrpr.ac.in/about-iit-ropar"))
                    context.startActivity(browserIntent)
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // About card
            FeatureCard(
                icon = Icons.Outlined.Info,
                title = "About",
                description = "Learn more about us.",
                modifier = Modifier.weight(1f),
                onClick = {
                    // Open IIT Ropar website in browser
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.iitrpr.ac.in/about-iit-ropar"))
                    context.startActivity(browserIntent)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Default empty click handler
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick), // Make the entire card clickable
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// Add a stub for the ReservationFormScreen that was missing
@Composable
fun ReservationFormScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reservation Form",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This form will be implemented soon.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
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
    val icon: ImageVector,
    val selectedColor: Color
)