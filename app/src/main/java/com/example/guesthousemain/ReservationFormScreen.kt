package com.example.guesthousemain.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.Applicant
import com.example.guesthousemain.network.CreateReservationRequest
import com.example.guesthousemain.network.CreateReservationResponse
import com.example.guesthousemain.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun DatePickerTextField(value: String, label: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            // Month is 0-indexed so add 1
                            val selectedDate = "$year-${(month + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
                            onDateSelected(selectedDate)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TimePickerTextField(value: String, label: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val selectedTime = String.format("%02d:%02d", hour, minute)
                            onTimeSelected(selectedTime)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }
            ) {
                Icon(Icons.Filled.AccessTime, contentDescription = "Select Time")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }

    // --- Category Dropdown State ---
    var category by remember { mutableStateOf("C") }
    val categoryOptions = listOf("A", "B", "C", "D")
    var categoryExpanded by remember { mutableStateOf(false) }

    // --- Room Type Dropdown State ---
    var roomType by remember { mutableStateOf("Single Occupancy") }
    var roomTypeExpanded by remember { mutableStateOf(false) }
    val roomTypeOptions = listOf("Single Occupancy", "Double Occupancy")

    // --- Additional Editable Fields ---
    var noOfGuests by remember { mutableStateOf("1") }
    var noOfRooms by remember { mutableStateOf("1") }
    var guestName by remember { mutableStateOf("wjenfn") }
    var purpose by remember { mutableStateOf("jnjn") }
    var arrivalDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var arrivalTime by remember { mutableStateOf("09:00") }
    var departureDate by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var departureTime by remember { mutableStateOf("10:00") }
    var address by remember { mutableStateOf("Mathura near Rashtrawadi congress office Akashwani chowk") }
    var source by remember { mutableStateOf("GUEST") }

    // --- Applicant Information ---
    var applicantName by remember { mutableStateOf("km") }
    var applicantDesignation by remember { mutableStateOf("kin") }
    var applicantDepartment by remember { mutableStateOf("in") }
    var applicantCode by remember { mutableStateOf("iin") }
    var applicantMobile by remember { mutableStateOf("0000000000") }
    var applicantEmail by remember { mutableStateOf("2c@iitrpr.ac.in") }

    // --- Reviewer Dropdown State ---
    var reviewer by remember { mutableStateOf("") }
    var reviewerExpanded by remember { mutableStateOf(false) }
    val reviewerOptionsA = listOf("Director", "Registar", "Associate Dean", "Dean")
    val reviewerOptionsB = listOf("HOD", "Dean", "Associate Dean", "Registar")
    val currentReviewerOptions = when (category) {
        "A" -> reviewerOptionsA
        "B" -> reviewerOptionsB
        else -> listOf("Chairman")
    }
    if (category == "C" || category == "D") {
        reviewer = "Chairman"
    } else if (reviewer.isEmpty()) {
        reviewer = currentReviewerOptions.first()
    }

    // --- Date and Time Formatters ---
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Reservation Form",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Category Dropdown ---
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                label = { Text("Category") },
                readOnly = true,
                trailingIcon = { TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { categoryExpanded = !categoryExpanded }
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categoryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            categoryExpanded = false
                            reviewer = when (option) {
                                "A" -> reviewerOptionsA.first()
                                "B" -> reviewerOptionsB.first()
                                else -> "Chairman"
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Room Type Dropdown ---
        ExposedDropdownMenuBox(
            expanded = roomTypeExpanded,
            onExpandedChange = { roomTypeExpanded = !roomTypeExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = roomType,
                onValueChange = {},
                label = { Text("Room Type") },
                readOnly = true,
                trailingIcon = { TrailingIcon(expanded = roomTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable { roomTypeExpanded = !roomTypeExpanded }
            )
            ExposedDropdownMenu(
                expanded = roomTypeExpanded,
                onDismissRequest = { roomTypeExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                roomTypeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            roomType = option
                            roomTypeExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Number of Guests & Number of Rooms ---
        OutlinedTextField(
            value = noOfGuests,
            onValueChange = { noOfGuests = it },
            label = { Text("Number of Guests") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = noOfRooms,
            onValueChange = { noOfRooms = it },
            label = { Text("Number of Rooms") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- Other Editable Fields ---
        OutlinedTextField(
            value = guestName,
            onValueChange = { guestName = it },
            label = { Text("Guest Name") },
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

        // --- Arrival Date with Corner Button ---
        DatePickerTextField(
            value = arrivalDate,
            label = "Arrival Date (yyyy-MM-dd)"
        ) { selectedDate ->
            arrivalDate = selectedDate
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Arrival Time with Corner Button ---
        TimePickerTextField(
            value = arrivalTime,
            label = "Arrival Time (HH:mm)"
        ) { selectedTime ->
            arrivalTime = selectedTime
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Departure Date with Corner Button ---
        DatePickerTextField(
            value = departureDate,
            label = "Departure Date (yyyy-MM-dd)"
        ) { selectedDate ->
            departureDate = selectedDate
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Departure Time with Corner Button ---
        TimePickerTextField(
            value = departureTime,
            label = "Departure Time (HH:mm, before 11:00)"
        ) { selectedTime ->
            departureTime = selectedTime
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            label = { Text("Source") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Applicant Information ---
        Text(text = "Applicant Information", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicantName,
            onValueChange = { applicantName = it },
            label = { Text("Applicant Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicantDesignation,
            onValueChange = { applicantDesignation = it },
            label = { Text("Applicant Designation") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicantDepartment,
            onValueChange = { applicantDepartment = it },
            label = { Text("Applicant Department") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicantCode,
            onValueChange = { applicantCode = it },
            label = { Text("Applicant Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicantMobile,
            onValueChange = { applicantMobile = it },
            label = { Text("Applicant Mobile") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = applicantEmail,
            onValueChange = { applicantEmail = it },
            label = { Text("Applicant Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Reviewer Dropdown ---
        if (category == "A" || category == "B") {
            ExposedDropdownMenuBox(
                expanded = reviewerExpanded,
                onExpandedChange = { reviewerExpanded = !reviewerExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = reviewer,
                    onValueChange = {},
                    label = { Text("Reviewer") },
                    readOnly = true,
                    trailingIcon = { TrailingIcon(expanded = reviewerExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .clickable { reviewerExpanded = !reviewerExpanded }
                )
                ExposedDropdownMenu(
                    expanded = reviewerExpanded,
                    onDismissRequest = { reviewerExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentReviewerOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                reviewer = option
                                reviewerExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = "Chairman",
                onValueChange = {},
                label = { Text("Reviewer") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- Submit Button ---
        Button(
            onClick = {
                // Validate arrival date (must be today or later)
                try {
                    val arrivalLocalDate = LocalDate.parse(arrivalDate, dateFormatter)
                    if (arrivalLocalDate.isBefore(LocalDate.now())) {
                        Toast.makeText(context, "Arrival date must be today or later", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid arrival date", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Validate departure time (must be before 11:00 AM)
                try {
                    val departureHour = departureTime.split(":").getOrNull(0)?.toIntOrNull() ?: 0
                    if (departureHour >= 11) {
                        Toast.makeText(context, "Departure time must be before 11:00 AM", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid departure time", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true

                // Build the applicant object from editable fields
                val applicantObj = Applicant(
                    name = applicantName,
                    designation = applicantDesignation,
                    department = applicantDepartment,
                    code = applicantCode,
                    mobile = applicantMobile,
                    email = applicantEmail
                )

                // Build the reservation request
                val request = CreateReservationRequest(
                    numberOfGuests = noOfGuests.toIntOrNull() ?: 1,
                    numberOfRooms = noOfRooms.toIntOrNull() ?: 1,
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
                    applicant = applicantObj,
                    reviewers = reviewer,
                    subroles = ""
                )

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
                            Toast.makeText(
                                context,
                                response.body()?.message ?: "Reservation submitted",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<CreateReservationResponse>, t: Throwable) {
                        isLoading = false
                        Toast.makeText(
                            context,
                            "Failure: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
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
