package com.example.guesthousemain

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guesthousemain.network.Notification
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigateBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val accessToken  = viewModel.accessToken
    val refreshToken = viewModel.refreshToken
    val isDarkTheme  = viewModel.isDarkTheme

    // Theme colors
    val backgroundGradient = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF102027),
                Color(0xFF263238),
                Color(0xFF102027)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE3F2FD),
                Color(0xFFBBDEFB),
                Color(0xFFE1F5FE)
            )
        )
    }

    val cardBackgroundColor = if (isDarkTheme) {
        Color(0xFF263238).copy(alpha = 0.9f)
    } else {
        Color.White.copy(alpha = 0.9f)
    }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val primaryColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3)
    val secondaryTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray

    // Fetch notifications when the screen loads
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchNotifications()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            SmallTopAppBar(
                title = { Text("Notifications", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "An error occurred",
                        color = Color.Red
                    )
                }
            } else if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "No Notifications",
                            modifier = Modifier.size(64.dp),
                            tint = secondaryTextColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications yet",
                            color = secondaryTextColor,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            cardBackgroundColor = cardBackgroundColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onMarkAsRead = { notificationId ->
                                viewModel.markAsRead(notificationId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    cardBackgroundColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    primaryColor: Color,
    onMarkAsRead: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val formattedDate = try {
        val date = notification.createdAt?.let { dateFormat.parse(it) }
        date?.let {
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(it)
        } ?: "Unknown date"
    } catch (e: Exception) {
        "Unknown date"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = notification.sender ?: "Notification",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Message
                Text(
                    text = notification.message ?: "",
                    color = textColor,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = formattedDate,
                        color = secondaryTextColor,
                        fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun NotificationBadge(
    notificationCount: Int,
    modifier: Modifier = Modifier
) {
    if (notificationCount > 0) {
        Box(
            modifier = modifier
                .size(16.dp)
                .background(
                    color = Color.Red,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
fun NavDrawerItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    notificationCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Spacer(modifier = Modifier.width(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
        )

        if (notificationCount > 0) {
            Spacer(modifier = Modifier.weight(1f))
            NotificationBadge(notificationCount = notificationCount)
        }
    }
}