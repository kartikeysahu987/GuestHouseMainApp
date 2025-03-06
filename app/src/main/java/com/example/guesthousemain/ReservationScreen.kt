package com.example.guesthousemain.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.CreateReservationRequest
import com.example.guesthousemain.network.CreateReservationResponse
import com.example.guesthousemain.network.Reservation
import com.example.guesthousemain.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen() {
    // Titles for the four tabs
    val tabTitles = listOf(
        "Approved Request",
        "Pending Request",
        "Rejected Request",
        "Reservation Form"
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservation Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> ApprovedRequestContent()
                1 -> PendingRequestContent()
                2 -> RejectedRequestContent()
                3 -> ReservationFormContent()
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
        emptyMessage = "No approved reservations found."
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
        emptyMessage = "No pending reservations found."
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
        emptyMessage = "No rejected reservations found."
    )
}

@Composable
fun ReservationFormContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Wrap the form inside a Card to add an elevated look.
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            ReservationFormScreen()
        }
    }
}

/**
 * A composable that fetches and displays a list of reservations.
 */
@Composable
fun ReservationListContent(
    fetchReservations: () -> Call<List<Reservation>>,
    emptyMessage: String
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

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        reservations?.let { list ->
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = emptyMessage, fontSize = 20.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(list) { reservation ->
                        Text(
                            text = "ID: ${reservation.id} | Email: ${reservation.guestEmail} | Status: ${reservation.status}",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No data.", fontSize = 20.sp)
        }
    }
}

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

    // Use a vertically scrollable Column for the form fields
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
