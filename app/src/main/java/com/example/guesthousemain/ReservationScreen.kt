package com.example.guesthousemain.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.CreateReservationRequest
import com.example.guesthousemain.network.CreateReservationResponse
import com.example.guesthousemain.network.Reservation
import com.example.guesthousemain.util.SessionManager
import com.example.guesthousemain.ui.theme.MyAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// Custom background composable with a gradient and optional image overlay.
// Replace the commented Image composable with your custom image if available.
@Composable
fun CustomBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Uncomment and replace with your custom image asset if needed:
        // Image(
        //     painter = painterResource(id = R.drawable.custom_background),
        //     contentDescription = "Background Image",
        //     modifier = Modifier.fillMaxSize(),
        //     contentScale = ContentScale.Crop
        // )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ReservationScreen() {
    MyAppTheme {
        // Wrap the whole UI in a custom background for added depth.
        CustomBackground {
            val tabTitles = listOf("Reservation Form", "Approved", "Pending", "Rejected")
            var selectedTabIndex by remember { mutableStateOf(0) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Reservation Dashboard",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.shadow(4.dp)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Tab navigation with custom indicator and rounded corners.
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                    .background(
                                        MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .background(
                                        if (selectedTabIndex == index)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                    }

                    // Smooth animated content switching for an enhanced user experience.
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            ContentTransform(
                                targetContentEnter = fadeIn(animationSpec = tween(300)),
                                initialContentExit = fadeOut(animationSpec = tween(300))
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        when (index) {
                            0 -> ReservationFormContent()
                            1 -> ApprovedRequestContent()
                            2 -> PendingRequestContent()
                            3 -> RejectedRequestContent()
                        }
                    }
                }
            }
        }
    }
}

// Approved reservations content with clear header and icon.
@Composable
fun ApprovedRequestContent() {
    ReservationListContent(
        fetchReservations = {
            ApiService.authService.getApprovedReservations(
                SessionManager.accessToken,
                SessionManager.refreshToken
            )
        },
        emptyMessage = "No approved reservations found.",
        title = "Approved Reservations",
        icon = Icons.Outlined.CheckCircle
    )
}

// Pending reservations content.
@Composable
fun PendingRequestContent() {
    ReservationListContent(
        fetchReservations = {
            ApiService.authService.getPendingReservations(
                SessionManager.accessToken,
                SessionManager.refreshToken
            )
        },
        emptyMessage = "No pending reservations found.",
        title = "Pending Reservations",
        icon = Icons.Outlined.HourglassEmpty
    )
}

// Rejected reservations content.
@Composable
fun RejectedRequestContent() {
    ReservationListContent(
        fetchReservations = {
            ApiService.authService.getRejectedReservations(
                SessionManager.accessToken,
                SessionManager.refreshToken
            )
        },
        emptyMessage = "No rejected reservations found.",
        title = "Rejected Reservations",
        icon = Icons.Outlined.Cancel
    )
}

// Reservation form section with a card and modern styling.
@Composable
fun ReservationFormContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .shadow(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with an icon and styled text.
                Text(
                    text = "📝 Make a Reservation",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Divider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
                Spacer(modifier = Modifier.height(12.dp))
                ReservationFormScreen()
            }
        }
    }
}

// Reusable list component to display reservations with loading animations and accessible content.
@Composable
fun ReservationListContent(
    fetchReservations: () -> Call<List<Reservation>>,
    emptyMessage: String,
    title: String,
    icon: ImageVector
) {
    val context = LocalContext.current
    var reservations by remember { mutableStateOf<List<Reservation>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        fetchReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(
                call: Call<List<Reservation>>,
                response: Response<List<Reservation>>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    reservations = response.body()
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Failure: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section header with icon and title.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$title Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            reservations?.let { list ->
                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = emptyMessage, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(list) { reservation ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { /* Add interaction if needed */ },
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "ID: ${reservation.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Guest Email: ${reservation.guestEmail}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Status: ${reservation.status}",
                                        fontSize = 14.sp,
                                        color = when (reservation.status) {
                                            "Approved" -> Color.Green
                                            "Pending" -> Color.Yellow
                                            "Rejected" -> Color.Red
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No data.", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Reservation form with scrollable fields, subtle button animations, and form validation.
@Composable
fun ReservationFormScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var guestName by remember { mutableStateOf("") }
    var numberOfGuests by remember { mutableStateOf("") }
    var numberOfRooms by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var arrivalDate by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var applicant by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Reservation Form",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = guestName,
            onValueChange = { guestName = it },
            label = { Text("Guest Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = numberOfGuests,
            onValueChange = { numberOfGuests = it },
            label = { Text("Number of Guests") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = numberOfRooms,
            onValueChange = { numberOfRooms = it },
            label = { Text("Number of Rooms") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = roomType,
            onValueChange = { roomType = it },
            label = { Text("Room Type") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = purpose,
            onValueChange = { purpose = it },
            label = { Text("Purpose") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = arrivalDate,
            onValueChange = { arrivalDate = it },
            label = { Text("Arrival Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = arrivalTime,
            onValueChange = { arrivalTime = it },
            label = { Text("Arrival Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = departureDate,
            onValueChange = { departureDate = it },
            label = { Text("Departure Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = departureTime,
            onValueChange = { departureTime = it },
            label = { Text("Departure Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (A/B/C/D)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            label = { Text("Payment Source") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicant,
            onValueChange = { applicant = it },
            label = { Text("Applicant (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (guestName.isBlank() || numberOfGuests.isBlank() || numberOfRooms.isBlank() ||
                    roomType.isBlank() || purpose.isBlank() || arrivalDate.isBlank() ||
                    arrivalTime.isBlank() || departureDate.isBlank() || departureTime.isBlank() ||
                    address.isBlank() || category.isBlank() || source.isBlank()
                ) {
                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val request = CreateReservationRequest(
                    numberOfGuests = numberOfGuests.toIntOrNull() ?: 0,
                    numberOfRooms = numberOfRooms.toIntOrNull() ?: 0,
                    roomType = roomType,
                    purpose = purpose,
                    guestName = guestName,
                    arrivalDate = arrivalDate,
                    arrivalTime = arrivalTime,
                    departureDate = departureDate,
                    departureTime = departureTime,
                    address = address,
                    category = category,
                    source = source,
                    applicant = if (applicant.isNotBlank()) applicant else null
                )
                isLoading = true
                ApiService.authService.createReservation(
                    SessionManager.accessToken,
                    SessionManager.refreshToken,
                    request
                ).enqueue(object : Callback<CreateReservationResponse> {
                    override fun onResponse(
                        call: Call<CreateReservationResponse>,
                        response: Response<CreateReservationResponse>
                    ) {
                        isLoading = false
                        if (response.isSuccessful) {
                            Toast.makeText(context, response.body()?.message ?: "Reservation submitted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<CreateReservationResponse>, t: Throwable) {
                        isLoading = false
                        Toast.makeText(context, "Failure: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Submit Reservation")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
