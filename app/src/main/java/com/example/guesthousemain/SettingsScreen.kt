package com.example.guesthousemain
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(bottomNavController: NavController,
                   drawerNavController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Get the ThemeManager
    val themeManager = LocalThemeManager.current

    // Collect the current theme state
    val isDarkTheme by themeManager.isDarkThemeFlow.collectAsState(initial = false)

    // Coroutine scope for theme updates
    val scope = rememberCoroutineScope()

    // State for dialogs
    var showRatingDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
                    color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))
//        Account Settings
//        SettingsSection(
//            title = "Account",
//            items = listOf(
//                SettingsItem(
//                    icon = Icons.Default.Person,
//                    title = "Edit Profile",
//                    subtitle = "Change your personal information"
//                ),
//                SettingsItem(
//                    icon = Icons.Default.Lock,
//                    title = "Change Password",
//                    subtitle = "Update your password"
//                ),
//                SettingsItem(
//                    icon = Icons.Default.Notifications,
//                    title = "Notifications",
//                    subtitle = "Manage notification preferences"
//                )
//            )
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Settings
        SettingsSection(
            title = "Preference",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Dark Theme",
                    subtitle = if (isDarkTheme) "Currently enabled" else "Currently disabled",
                    hasToggle = true,
                    isToggleOn = isDarkTheme,  // Pass the current theme state
                    onClick = {
                        // Toggle theme when clicked
                        scope.launch {
                            themeManager.setDarkTheme(!isDarkTheme)
                        }
                    }
                ),
//                SettingsItem(
//                    icon = Icons.Default.Language,
//                    title = "Language",
//                    subtitle = "Select your preferred language",
//                    onClick = { /* Handle language selection */ }
//                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Support & About
        SettingsSection(
            title = "Support & About",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help or contact us",
                    onClick = { showHelpDialog = true }
                ),
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Learn more about the app",
                    onClick = { showAboutDialog = true }
                ),
                SettingsItem(
                    icon = Icons.Default.StarRate,
                    title = "Rate the App",
                    subtitle = "Tell us what you think",
                    onClick = { showRatingDialog = true }
                )
            )
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Version info
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // Rating Dialog
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onRateSubmit = { rating ->
                // Here you would typically send the rating to your backend
                // For now, we'll just open the Play Store
//                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
//                    data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
//                    setPackage("com.android.vending")
//                }
//                if (playStoreIntent.resolveActivity(context.packageManager) != null) {
//                    context.startActivity(playStoreIntent)
//                }
//                showRatingDialog = false
            }
        )
    }

    // Help & Support Dialog
    if (showHelpDialog) {
        HelpSupportDialog(
            onDismiss = { showHelpDialog = false },
            onEmailSupportClick = {
                // Navigate to contact screen
                drawerNavController.navigate("none")
                bottomNavController.navigate("contact"){
                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                showHelpDialog = false
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        items.forEachIndexed { index, item ->
            SettingsItemRow(item = item)

            if (index < items.size - 1) {
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val hasToggle: Boolean = false,
    val isToggleOn: Boolean = false,
    val onClick: () -> Unit = {}
)

@Composable
private fun SettingsItemRow(item: SettingsItem) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Toggle or arrow
        if (item.hasToggle) {
            Switch(
                checked = item.isToggleOn,
                onCheckedChange = {
                    item.onClick()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRateSubmit: (Int) -> Unit
) {
    var selectedRating by remember { mutableStateOf(0) }
    var showThankYouMessage by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showThankYouMessage) {
                    // Rating UI
                    Text(
                        text = "Rate Our App",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "We would love to hear what you think about our app!",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Star rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Star $i",
                                tint = if (i <= selectedRating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedRating = i }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (selectedRating > 0) {
                                    // Instead of going to Play Store, show thank you message
                                    showThankYouMessage = true
                                    // Still call onRateSubmit to maintain the function contract
                                    onRateSubmit(selectedRating)
                                }
                            },
                            enabled = selectedRating > 0
                        ) {
                            Text("Submit")
                        }
                    }
                } else {
                    // Thank you message based on rating
                    val message = when (selectedRating) {
                        5 -> "Thanks for giving us 5 stars! We're thrilled you love the app."
                        4 -> "Thank you for your 4-star rating! We appreciate your support."
                        3 -> "Thanks for your 3-star feedback. We're constantly working to improve."
                        2 -> "Thank you for your 2-star feedback. We'll work hard to make it better."
                        1 -> "We appreciate your 1-star feedback and will address your concerns."
                        else -> "Thank you for your feedback!"
                    }

                    Icon(
                        imageVector = if (selectedRating >= 4) Icons.Default.ThumbUp else Icons.Default.Feedback,
                        contentDescription = "Feedback Icon",
                        tint = if (selectedRating >= 4) Color(0xFF4CAF50) else Color(0xFFFFA000),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit,
    onEmailSupportClick: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                SupportOption(
                    icon = Icons.Default.Email,
                    title = "Email Support",
                    subtitle = "Contact our support team",
                    onClick = onEmailSupportClick
                )

//                Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                SupportOption(
//                    icon = Icons.Default.Help,
//                    title = "FAQ",
//                    subtitle = "Frequently asked questions",
//                    onClick = {
//                        // Navigate to FAQ screen or open web page
//                        // For now, we'll just dismiss
//                        onDismiss()
//                    }
//                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                SupportOption(
                    icon = Icons.Default.Call,
                    title = "Call Support",
                    subtitle = "+91 7385429723",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:+91 7385429723")
                        }
                        context.startActivity(intent)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun SupportOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "About Guest House",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Guest House is your ultimate destination for booking comfortable accommodations for your stay in IIT Ropar.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "© 2025 Guest House Inc.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

//                AboutItem(
//                    title = "Privacy Policy",
//                    onClick = { /* Open privacy policy */ }
//                )
//
//                AboutItem(
//                    title = "Terms of Service",
//                    onClick = { /* Open terms of service */ }
//                )
//
//                AboutItem(
//                    title = "Licenses",
//                    onClick = { /* Open licenses */ }
//                )
//
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun AboutItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// You'll need to add this Contact Screen to your navigation
@Composable
fun ContactScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Contact Us",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Contact form or information can be added here
        Text(
            text = "We're here to help! Send us a message and we'll get back to you as soon as possible.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Add your contact form here
        // For example:
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Your Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Handle form submission */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Message")
        }
    }
}