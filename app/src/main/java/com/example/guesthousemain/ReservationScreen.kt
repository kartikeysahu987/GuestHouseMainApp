package com.example.guesthousemain.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.Reservation
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

// Define modern hospitality industry-inspired colors
object HospitalityColors {
    val primary = Color(0xFF1A73E8) // Booking.com blue
    val secondary = Color(0xFF00BFA5) // Mint green like TripAdvisor
    val accent = Color(0xFFFFA000) // Amber like Expedia
    val background = Color(0xFFF8F9FA) // Light background
    val cardBackground = Color.White
    val approved = Color(0xFF4CAF50) // Green for approved
    val pending = Color(0xFFFF9800) // Orange for pending
    val rejected = Color(0xFFE53935) // Red for rejected
    val textPrimary = Color(0xFF202124) // Dark text
    val textSecondary = Color(0xFF5F6368) // Grey text
    val divider = Color(0xFFEEEEEE) // Light divider
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ReservationScreen() {
    val tabTitles = listOf(
        "Approved",
        "Pending",
        "Rejected",
        "New Request"
    )

    val tabIcons = listOf(
        Icons.Filled.CheckCircle,
        Icons.Filled.Pending,
        Icons.Filled.Error,
        Icons.Rounded.CalendarMonth
    )

    val tabColors = listOf(
        HospitalityColors.approved,
        HospitalityColors.pending,
        HospitalityColors.rejected,
        HospitalityColors.primary
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    // Track if a swipe is in progress to prevent multiple rapid swipes
    var isSwipeInProgress by remember { mutableStateOf(false) }

    // Animated rotation for refresh icon
    val refreshRotation = remember { Animatable(0f) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            refreshRotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            refreshRotation.stop()
            refreshRotation.snapTo(0f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reservation Dashboard",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HospitalityColors.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (!isRefreshing) {
                                isRefreshing = true
                                scope.launch {
                                    // Show loading state for at least 800ms for better UX
                                    delay(800)
                                    isRefreshing = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = refreshRotation.value
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HospitalityColors.background)
        ) {
            // Animated tab selection with indicator animation
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = HospitalityColors.cardBackground,
                contentColor = HospitalityColors.primary,
                divider = {
                    Divider(color = HospitalityColors.divider, thickness = 1.dp)
                },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .padding(horizontal = 16.dp),
                        height = 3.dp,
                        color = tabColors[selectedTabIndex]
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            AnimatedContent(
                                targetState = selectedTabIndex == index,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(150, 150)) with
                                            fadeOut(animationSpec = tween(150))
                                }
                            ) { isSelected ->
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = null,
                                tint = if (selectedTabIndex == index) tabColors[index] else Color.Gray,
                                modifier = Modifier.animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            )
                        },
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                }
            }

            // Tab content with padding and swipe gesture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // Add horizontal drag gesture detection for swipe
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // Reset swipe status when finger is lifted
                                isSwipeInProgress = false
                            },
                            onDragStart = {
                                // Nothing special on drag start
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                // Only process swipe if we're not already in a swipe transition
                                if (!isSwipeInProgress) {
                                    val threshold = 50f // Minimum swipe distance to trigger a tab change

                                    when {
                                        // Swipe left (negative dragAmount) - go to next tab
                                        dragAmount < -threshold -> {
                                            if (selectedTabIndex < tabTitles.size - 1) {
                                                isSwipeInProgress = true
                                                scope.launch {
                                                    // Move exactly one tab to the right
                                                    selectedTabIndex++

                                                    // Add a small delay before allowing another swipe
                                                    // This prevents accidental multiple swipes
                                                    delay(300)
                                                    isSwipeInProgress = false
                                                }
                                            }
                                        }
                                        // Swipe right (positive dragAmount) - go to previous tab
                                        dragAmount > threshold -> {
                                            if (selectedTabIndex > 0) {
                                                isSwipeInProgress = true
                                                scope.launch {
                                                    // Move exactly one tab to the left
                                                    selectedTabIndex--

                                                    // Add a small delay before allowing another swipe
                                                    delay(100)
                                                    isSwipeInProgress = false
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
            ) {
                // Content is loaded based on selected tab with slide transition
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        // Determine slide direction based on tab index change
                        if (targetState > initialState) {
                            // Moving right (e.g., from Approved to Pending)
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            // Moving left (e.g., from Pending to Approved)
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    }
                ) { targetTabIndex ->
                    when (targetTabIndex) {
                        0 -> ApprovedRequestContent()
                        1 -> PendingRequestContent()
                        2 -> RejectedRequestContent()
                        3 -> ReservationFormContent()
                    }
                }

                // Loading overlay
                this@Column.AnimatedVisibility(
                    visible = isRefreshing,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        CircularProgressIndicator(
                            color = HospitalityColors.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale)
                        )
                    }
                }
            }
        }
    }
}
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
        statusColor = HospitalityColors.approved,
        statusIcon = Icons.Default.CheckCircle
    )
}

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
        statusColor = HospitalityColors.pending,
        statusIcon = Icons.Default.Pending
    )
}

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
        statusColor = HospitalityColors.rejected,
        statusIcon = Icons.Default.Error
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFormContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = HospitalityColors.cardBackground)
        ) {
            // Call the ReservationFormScreen from the separate file
            ReservationFormScreen()
        }
    }
}

/**
 * A composable that fetches and displays a list of reservations.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReservationListContent(
    fetchReservations: () -> Call<List<Reservation>>,
    emptyMessage: String,
    statusColor: Color,
    statusIcon: ImageVector
) {
    val context = LocalContext.current
    var reservations by remember { mutableStateOf<List<Reservation>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Function to load data
    fun loadData() {
        isLoading = true
        hasError = false

        fetchReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(
                call: Call<List<Reservation>>,
                response: Response<List<Reservation>>
            ) {
                if (response.isSuccessful) {
                    reservations = response.body()
                    isLoading = false
                } else {
                    hasError = true
                    errorMessage = "Error: ${response.message()}"
                    isLoading = false
                }
            }

            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                hasError = true
                errorMessage = "Network error: ${t.localizedMessage}"
                isLoading = false
            }
        })
    }

    // Initial data load
    LaunchedEffect(Unit) {
        loadData()
    }

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = Triple(isLoading, hasError, reservations.isNullOrEmpty()),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with
                        fadeOut(animationSpec = tween(300))
            }
        ) { (loading, error, empty) ->
            when {
                loading -> LoadingState()
                error -> ErrorState(
                    message = errorMessage,
                    onRetry = { loadData() }
                )
                empty -> EmptyState(message = emptyMessage)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = reservations!!,
                            key = { it.id ?: UUID.randomUUID().toString() }
                        ) { reservation ->
                            // Animate items as they appear
                            var animatedVisibility by remember { mutableStateOf(false) }
                            LaunchedEffect(key1 = reservation.id) {
                                delay(100) // Stagger the animations
                                animatedVisibility = true
                            }

                            AnimatedVisibility(
                                visible = animatedVisibility,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                ReservationCard(
                                    reservation = reservation,
                                    statusColor = statusColor,
                                    statusIcon = statusIcon
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    statusColor: Color,
    statusIcon: ImageVector
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = HospitalityColors.cardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Reservation ID and Status Row with shimmer effect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reservation #${reservation.id?.takeLast(6) ?: "N/A"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = HospitalityColors.textPrimary
                )

                // Status chip with pulsating animation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp)),
                    color = statusColor.copy(alpha = 0.1f * alpha)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = "Status",
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reservation.status ?: "Unknown",
                            color = statusColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = HospitalityColors.divider)
            Spacer(modifier = Modifier.height(12.dp))

            // Guest Information with icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mail,
                    contentDescription = "Email",
                    tint = HospitalityColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reservation.guestEmail ?: "No email provided",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = HospitalityColors.textSecondary
                )
            }

            // Expand/collapse animation for additional details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = HospitalityColors.divider)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mock data for additional information
                    InfoRow(
                        icon = Icons.Rounded.Person,
                        label = "Guest",
                        value = reservation.guestEmail ?: "Guest information not available"
                    )

//                    InfoRow(
//                        icon = Icons.Rounded.CalendarMonth,
//                        label = "Check-in",
//                        value = reservation.checkInDate ?: "Not specified"
//                    )

//                    InfoRow(
//                        icon = Icons.Rounded.CalendarMonth,
//                        label = "Check-out",
//                        value = reservation.checkOutDate ?: "Not specified"
//                    )

//                    InfoRow(
//                        icon = Icons.Rounded.LocationOn,
//                        label = "Room",
//                        value = reservation.roomType ?: "Not specified"
//                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { /* Handle view details */ },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = HospitalityColors.primary
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("View Details")
                        }

                        Button(
                            onClick = { /* Handle primary action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HospitalityColors.primary
                            )
                        ) {
                            Text(
                                when (reservation.status?.lowercase()) {
                                    "approved" -> "Modify"
                                    "pending" -> "Review"
                                    "rejected" -> "Reconsider"
                                    else -> "View"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = HospitalityColors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = HospitalityColors.textSecondary
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = HospitalityColors.textPrimary
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = HospitalityColors.primary,
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading reservations...",
                color = HospitalityColors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animation for error icon
            val infiniteTransition = rememberInfiniteTransition(label = "error")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "shake"
            )

            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = HospitalityColors.rejected,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = HospitalityColors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HospitalityColors.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Empty state illustration with subtle animation
            val infiniteTransition = rememberInfiniteTransition(label = "empty")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = "No reservations",
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale),
                tint = HospitalityColors.textSecondary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = HospitalityColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Check back later or create a new reservation",
                fontSize = 14.sp,
                color = HospitalityColors.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}