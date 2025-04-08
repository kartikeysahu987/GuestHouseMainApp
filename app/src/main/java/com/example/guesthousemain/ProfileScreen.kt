package com.example.guesthousemain
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp



// First, let's create the Profile Screen
@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Info
        Text(
            text = "Guest User",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

//        Text(
//            text = "guest@example.com",
//            style = MaterialTheme.typography.bodyLarge,
//            color = Color.Gray
//        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Information
        ProfileInfoSection()

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Bookings
        //RecentBookingsSection()
    }
}

@Composable
private fun ProfileInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F3FF))
            .padding(16.dp)
    ) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileInfoItem(
            icon = Icons.Default.Phone,
            title = "Phone",
            value = "+91 98765 43210"
        )
        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color.LightGray.copy(alpha = 0.5f)
        )
        ProfileInfoItem(
            icon = Icons.Default.Mail,
            title = "Email",
            value = "guest@gmail.com"
        )

//        Divider(
//            modifier = Modifier.padding(vertical = 12.dp),
//            color = Color.LightGray.copy(alpha = 0.5f)
//        )
//
//        ProfileInfoItem(
//            icon = Icons.Default.Home,
//            title = "Address",
//            value = "IIT Ropar, Rupnagar, Punjab"
//        )
//
//        Divider(
//            modifier = Modifier.padding(vertical = 12.dp),
//            color = Color.LightGray.copy(alpha = 0.5f)
//        )
//
//        ProfileInfoItem(
//            icon = Icons.Default.Work,
//            title = "Department",
//            value = "Computer Science"
//        )
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

//@Composable
//private fun RecentBookingsSection() {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(12.dp))
//            .background(Color(0xFFF5F3FF))
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Recent Bookings",
//            style = MaterialTheme.typography.titleMedium.copy(
//                fontWeight = FontWeight.SemiBold
//            ),
//            color = MaterialTheme.colorScheme.primary
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Sample booking items
//        BookingItem(
//            roomType = "Deluxe Room",
//            date = "April 15-17, 2025",
//            status = "Confirmed"
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        BookingItem(
//            roomType = "Standard Room",
//            date = "March 20-22, 2025",
//            status = "Completed"
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // View all bookings button
//        TextButton(
//            onClick = { /* Navigate to bookings history */ },
//            modifier = Modifier.align(Alignment.End)
//        ) {
//            Text(
//                text = "View All",
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Icon(
//                imageVector = Icons.Default.KeyboardArrowRight,
//                contentDescription = "View All Bookings",
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(16.dp)
//            )
//        }
//    }
//}
//
//@Composable
//private fun BookingItem(
//    roomType: String,
//    date: String,
//    status: String
//) {
//    val statusColor = when(status) {
//        "Confirmed" -> Color(0xFF22C55E) // Green
//        "Pending" -> Color(0xFFF59E0B) // Amber
//        else -> Color(0xFF6B7280) // Gray
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(8.dp))
//            .background(Color.White)
//            .padding(12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Room icon
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(RoundedCornerShape(8.dp))
//                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.Hotel,
//                contentDescription = "Room",
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        // Booking details
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = roomType,
//                style = MaterialTheme.typography.bodyLarge.copy(
//                    fontWeight = FontWeight.Medium
//                )
//            )
//
//            Text(
//                text = date,
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray
//            )
//        }
//
//        // Status chip
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(16.dp))
//                .background(statusColor.copy(alpha = 0.1f))
//                .padding(horizontal = 12.dp, vertical = 6.dp)
//        ) {
//            Text(
//                text = status,
//                style = MaterialTheme.typography.bodySmall.copy(
//                    fontWeight = FontWeight.Medium
//                ),
//                color = statusColor
//            )
//        }
//    }
//}