package com.example.guesthousemain.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
        Column(
            modifier = Modifier
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
                3 -> ReservationFormContent() // This will call the composable from the separate file.
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
    // We wrap the form in a Card for styling.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            // Call the ReservationFormScreen from the separate file.
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
