//package com.example.guesthousemain
//
//import android.os.Build
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Error
//import androidx.compose.material.icons.filled.Pending
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Divider
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.guesthousemain.network.ApiService
//import com.example.guesthousemain.network.Reservation
//import com.example.guesthousemain.network.ReservationDetailResponse
//import com.example.guesthousemain.ui.screens.ErrorState
//import com.example.guesthousemain.ui.screens.HospitalityColors
//import com.example.guesthousemain.ui.screens.LoadingState
//import com.example.guesthousemain.ui.screens.StatusChip
//import com.example.guesthousemain.util.SessionManager
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//@OptIn(ExperimentalMaterial3Api::class)
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun ReservationDetailsScreen(
//    reservationId: String,
//    onBackPressed: () -> Unit
//) {
//    val context = LocalContext.current
//    var reservation by remember { mutableStateOf<Reservation?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//    var hasError by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf("") }
//
//    // Load reservation details
//    LaunchedEffect(reservationId) {
//        isLoading = true
//        hasError = false
//
//        ApiService.authService.getReservationDetails(
//            reservationId,
//            SessionManager.accessToken,
//            SessionManager.refreshToken
//        ).enqueue(object : Callback<ReservationDetailResponse> {
//            override fun onResponse(
//                call: Call<ReservationDetailResponse>,
//                response: Response<ReservationDetailResponse>
//            ) {
//                if (response.isSuccessful) {
//                    reservation = response.body()?.reservation
//                    isLoading = false
//                } else {
//                    hasError = true
//                    errorMessage = "Error: ${response.message()}"
//                    isLoading = false
//                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<ReservationDetailResponse>, t: Throwable) {
//                hasError = true
//                errorMessage = "Network error: ${t.localizedMessage}"
//                isLoading = false
//                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Reservation Details") },
//                navigationIcon = {
//                    IconButton(onClick = onBackPressed) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = HospitalityColors.primary,
//                    titleContentColor = Color.White,
//                    navigationIconContentColor = Color.White
//                )
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(HospitalityColors.background)
//        ) {
//            when {
//                isLoading -> LoadingState()
//                hasError -> ErrorState(message = errorMessage) {
//                    // Retry logic
//                }
//                reservation != null -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        item {
//                            ReservationDetailsCard(reservation = reservation!!)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ReservationDetailsCard(reservation: Reservation) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(containerColor = HospitalityColors.cardBackground),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            // Status section
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Reservation #${reservation.id?.takeLast(6) ?: "N/A"}",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp,
//                    color = HospitalityColors.textPrimary
//                )
//
//                StatusChip(
//                    status = reservation.status ?: "Unknown",
//                    color = when(reservation.status?.lowercase()) {
//                        "approved" -> HospitalityColors.approved
//                        "pending" -> HospitalityColors.pending
//                        "rejected" -> HospitalityColors.rejected
//                        else -> HospitalityColors.textSecondary
//                    },
//                    icon = when(reservation.status?.lowercase()) {
//                        "approved" -> Icons.Default.CheckCircle
//                        "pending" -> Icons.Default.Pending
//                        "rejected" -> Icons.Default.Error
//                        else -> Icons.Default.Pending
//                    }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Divider(color = HospitalityColors.divider)
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Guest Details
//            Text(
//                text = "Guest Information",
//                fontWeight = FontWeight.Bold,
//                fontSize = 16.sp,
//                color = HospitalityColors.primary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            DetailItem("Guest Name", reservation.guestName ?: "Not provided")
//            DetailItem("Email", reservation.guestEmail ?: "Not provided")
//            DetailItem("Address", reservation.address ?: "Not provided")
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Divider(color = HospitalityColors.divider)
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Reservation Details
//            Text(
//                text = "Reservation Details",
//                fontWeight = FontWeight.Bold,
//                fontSize = 16.sp,
//                color = HospitalityColors.primary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            DetailItem("Number of Guests", "${reservation.numberOfGuests ?: "N/A"}")
//            DetailItem("Number of Rooms", "${reservation.numberOfRooms ?: "N/A"}")
//            DetailItem("Room Type", reservation.roomType ?: "Not specified")
//            DetailItem("Purpose", reservation.purpose ?: "Not specified")
//            DetailItem("Category", reservation.category ?: "Not specified")
//            DetailItem("Source", reservation.source ?: "Not specified")
//
//            Spacer(modifier = Modifier.height(16.dp))
//            Divider(color = HospitalityColors.divider)
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Date & Time Information
//            Text(
//                text = "Schedule Information",
//                fontWeight = FontWeight.Bold,
//                fontSize = 16.sp,
//                color = HospitalityColors.primary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            DetailItem("Arrival Date", reservation.arrivalDate ?: "Not set")
//            DetailItem("Arrival Time", reservation.arrivalTime ?: "Not set")
//            DetailItem("Departure Date", reservation.departureDate ?: "Not set")
//            DetailItem("Departure Time", reservation.departureTime ?: "Not set")
//
//            // Add Applicant information if available
//            reservation.applicant?.let { applicant ->
//                Spacer(modifier = Modifier.height(16.dp))
//                Divider(color = HospitalityColors.divider)
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Applicant Information",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 16.sp,
//                    color = HospitalityColors.primary
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                DetailItem("Name", applicant.name)
//                DetailItem("Designation", applicant.designation)
//                DetailItem("Department", applicant.department)
//                DetailItem("Code", applicant.code)
//                DetailItem("Mobile", applicant.mobile)
//                DetailItem("Email", applicant.email)
//            }
//        }
//    }
//}
//
//@Composable
//fun DetailItem(label: String, value: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//    ) {
//        Text(
//            text = "$label:",
//            fontWeight = FontWeight.Medium,
//            fontSize = 14.sp,
//            color = HospitalityColors.textSecondary,
//            modifier = Modifier.width(140.dp)
//        )
//        Text(
//            text = value,
//            fontSize = 14.sp,
//            color = HospitalityColors.textPrimary,
//            modifier = Modifier.weight(1f)
//        )
//    }
//}