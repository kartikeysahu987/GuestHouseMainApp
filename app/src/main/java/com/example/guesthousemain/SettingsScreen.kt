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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Now, let's create the Settings Screen
@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()

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
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Account Settings
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

        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Settings
        SettingsSection(
            title = "Preferences",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Change app appearance",
                    hasToggle = true
                ),
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "Select your preferred language"
                )
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
                    subtitle = "Get help or contact us"
                ),
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Learn more about the app"
                ),
                SettingsItem(
                    icon = Icons.Default.StarRate,
                    title = "Rate the App",
                    subtitle = "Tell us what you think"
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
            .background(Color(0xFFF5F3FF))
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
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val hasToggle: Boolean = false
)

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    var toggleState by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ },
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
                color = Color.Gray
            )
        }

        // Toggle or arrow
        if (item.hasToggle) {
            Switch(
                checked = toggleState,
                onCheckedChange = { toggleState = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}