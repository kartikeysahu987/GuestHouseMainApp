package com.example.guesthousemain.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.Applicant
import com.example.guesthousemain.network.CreateReservationRequest
import com.example.guesthousemain.network.CreateReservationResponse
import com.example.guesthousemain.ui.theme.GuestHouseMainTheme
import com.example.guesthousemain.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
//@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }

    // Form state variables
    var category by remember { mutableStateOf("C") }
    val categoryOptions = listOf("A", "B", "C", "D")
    var categoryExpanded by remember { mutableStateOf(false) }

    var roomType by remember { mutableStateOf("Single Occupancy") }
    var roomTypeExpanded by remember { mutableStateOf(false) }
    val roomTypeOptions = listOf("Single Occupancy", "Double Occupancy")

    var noOfGuests by remember { mutableStateOf("1") }
    var noOfRooms by remember { mutableStateOf("1") }
    var guestName by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var arrivalDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var arrivalTime by remember { mutableStateOf("09:00") }
    var departureDate by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var departureTime by remember { mutableStateOf("10:00") }
    var address by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("GUEST") }

    var applicantName by remember { mutableStateOf("") }
    var applicantDesignation by remember { mutableStateOf("") }
    var applicantDepartment by remember { mutableStateOf("") }
    var applicantCode by remember { mutableStateOf("") }
    var applicantMobile by remember { mutableStateOf("") }
    var applicantEmail by remember { mutableStateOf("") }

    // Error state variables
    var guestNameError by remember { mutableStateOf(false) }
    var purposeError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var applicantNameError by remember { mutableStateOf(false) }
    var applicantDesignationError by remember { mutableStateOf(false) }
    var applicantDepartmentError by remember { mutableStateOf(false) }
    var applicantCodeError by remember { mutableStateOf(false) }
    var applicantMobileError by remember { mutableStateOf(false) }
    var applicantMobileFormatError by remember { mutableStateOf(false) }
    var applicantEmailError by remember { mutableStateOf(false) }
    var applicantEmailFormatError by remember { mutableStateOf(false) }

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

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Validation functions
    val validatePhoneNumber: (String) -> Boolean = { phone ->
        phone.length == 10 && phone.all { it.isDigit() }
    }

    val validateEmail: (String) -> Boolean = { email ->
        email.endsWith("@iitrpr.ac.in") && email.contains("@") && email.indexOf("@") > 0
    }

    val validateFields: () -> Boolean = {
        // Reset all error states
        guestNameError = guestName.isBlank()
        purposeError = purpose.isBlank()
        addressError = address.isBlank()
        applicantNameError = applicantName.isBlank()
        applicantDesignationError = applicantDesignation.isBlank()
        applicantDepartmentError = applicantDepartment.isBlank()
        applicantCodeError = applicantCode.isBlank()

        // Phone validation
        applicantMobileError = applicantMobile.isBlank()
        applicantMobileFormatError = !applicantMobile.isBlank() && !validatePhoneNumber(applicantMobile)

        // Email validation
        applicantEmailError = applicantEmail.isBlank()
        applicantEmailFormatError = !applicantEmail.isBlank() && !validateEmail(applicantEmail)

        // Return true only if all validations pass
        !guestNameError && !purposeError && !addressError &&
                !applicantNameError && !applicantDesignationError && !applicantDepartmentError &&
                !applicantCodeError && !applicantMobileError && !applicantMobileFormatError &&
                !applicantEmailError && !applicantEmailFormatError
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Reservation Form",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Fill in the details to book your guest house",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "* All fields are mandatory",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Basic Information Section
        FormSection(title = "Basic Information") {
            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Category *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
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
            Spacer(modifier = Modifier.height(12.dp))

            // Room Type Dropdown
            ExposedDropdownMenuBox(
                expanded = roomTypeExpanded,
                onExpandedChange = { roomTypeExpanded = !roomTypeExpanded }
            ) {
                OutlinedTextField(
                    value = roomType,
                    onValueChange = {},
                    label = { Text("Room Type *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = roomTypeExpanded,
                    onDismissRequest = { roomTypeExpanded = false }
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
            Spacer(modifier = Modifier.height(12.dp))

            // Number inputs in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = noOfGuests,
                    onValueChange = { noOfGuests = it },
                    label = { Text("Guests *") },
                    modifier = Modifier.weight(1f),
                    isError = noOfGuests.isBlank()
                )
                OutlinedTextField(
                    value = noOfRooms,
                    onValueChange = { noOfRooms = it },
                    label = { Text("Rooms *") },
                    modifier = Modifier.weight(1f),
                    isError = noOfRooms.isBlank()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = guestName,
                onValueChange = {
                    guestName = it
                    guestNameError = it.isBlank()
                },
                label = { Text("Guest Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = guestNameError,
                supportingText = {
                    if (guestNameError) {
                        Text("Guest name is required")
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = purpose,
                onValueChange = {
                    purpose = it
                    purposeError = it.isBlank()
                },
                label = { Text("Purpose of Visit *") },
                modifier = Modifier.fillMaxWidth(),
                isError = purposeError,
                supportingText = {
                    if (purposeError) {
                        Text("Purpose is required")
                    }
                }
            )
        }

        // Date & Time Section
        FormSection(title = "Dates & Times") {
            // Arrival row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerTextField(
                    value = arrivalDate,
                    label = "Arrival Date *",
                    onDateSelected = { arrivalDate = it },
                    modifier = Modifier.weight(1f)
                )
                TimePickerTextField(
                    value = arrivalTime,
                    label = "Arrival Time *",
                    onTimeSelected = { arrivalTime = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Departure row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerTextField(
                    value = departureDate,
                    label = "Departure Date *",
                    onDateSelected = { departureDate = it },
                    modifier = Modifier.weight(1f)
                )
                TimePickerTextField(
                    value = departureTime,
                    label = "Departure Time *",
                    onTimeSelected = { departureTime = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Note: Checkout time must be before 11:00 AM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Additional Info Section
        FormSection(title = "Additional Information") {
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    addressError = it.isBlank()
                },
                label = { Text("Address *") },
                modifier = Modifier.fillMaxWidth(),
                isError = addressError,
                supportingText = {
                    if (addressError) {
                        Text("Address is required")
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source *") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Applicant Information Section
        FormSection(title = "Applicant Information") {
            OutlinedTextField(
                value = applicantName,
                onValueChange = {
                    applicantName = it
                    applicantNameError = it.isBlank()
                },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = applicantNameError,
                supportingText = {
                    if (applicantNameError) {
                        Text("Name is required")
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = applicantDesignation,
                    onValueChange = {
                        applicantDesignation = it
                        applicantDesignationError = it.isBlank()
                    },
                    label = { Text("Designation *") },
                    modifier = Modifier.weight(1f),
                    isError = applicantDesignationError,
                    supportingText = {
                        if (applicantDesignationError) {
                            Text("Required")
                        }
                    }
                )
                OutlinedTextField(
                    value = applicantDepartment,
                    onValueChange = {
                        applicantDepartment = it
                        applicantDepartmentError = it.isBlank()
                    },
                    label = { Text("Department *") },
                    modifier = Modifier.weight(1f),
                    isError = applicantDepartmentError,
                    supportingText = {
                        if (applicantDepartmentError) {
                            Text("Required")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = applicantCode,
                onValueChange = {
                    applicantCode = it
                    applicantCodeError = it.isBlank()
                },
                label = { Text("Employee Code *") },
                modifier = Modifier.fillMaxWidth(),
                isError = applicantCodeError,
                supportingText = {
                    if (applicantCodeError) {
                        Text("Employee code is required")
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = applicantMobile,
                    onValueChange = {
                        applicantMobile = it
                        applicantMobileError = it.isBlank()
                        if (!it.isBlank()) {
                            applicantMobileFormatError = !validatePhoneNumber(it)
                        }
                    },
                    label = { Text("Mobile *") },
                    modifier = Modifier.weight(1f),
                    isError = applicantMobileError || applicantMobileFormatError,
                    supportingText = {
                        when {
                            applicantMobileError -> Text("Mobile number is required")
                            applicantMobileFormatError -> Text("Must be 10 digits")
                        }
                    }
                )
                OutlinedTextField(
                    value = applicantEmail,
                    onValueChange = {
                        applicantEmail = it
                        applicantEmailError = it.isBlank()
                        if (!it.isBlank()) {
                            applicantEmailFormatError = !validateEmail(it)
                        }
                    },
                    label = { Text("Email *") },
                    modifier = Modifier.weight(1f),
                    isError = applicantEmailError || applicantEmailFormatError,
                    supportingText = {
                        when {
                            applicantEmailError -> Text("Email is required")
                            applicantEmailFormatError -> Text("Must end with @iitrpr.ac.in")
                        }
                    }
                )
            }
        }

        // Reviewer Section
        FormSection(title = "Approval Information") {
            if (category == "A" || category == "B") {
                ExposedDropdownMenuBox(
                    expanded = reviewerExpanded,
                    onExpandedChange = { reviewerExpanded = !reviewerExpanded }
                ) {
                    OutlinedTextField(
                        value = reviewer,
                        onValueChange = {},
                        label = { Text("Reviewer *") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reviewerExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = reviewerExpanded,
                        onDismissRequest = { reviewerExpanded = false }
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
                    label = { Text("Reviewer *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Submit Button
        Button(
            onClick = {
                // Validate all fields first
                if (!validateFields()) {
                    Toast.makeText(context, "Please fill all required fields correctly", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Validate arrival date
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

                // Validate departure time
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

                // Build the applicant object
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
                                response.body()?.message ?: "Reservation submitted successfully!",
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
                            "Failed to submit: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Submit Reservation", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun DatePickerTextField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
    )
}

@Composable
fun TimePickerTextField(
    value: String,
    label: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ReservationFormScreenPreview() {
    GuestHouseMainTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ReservationFormScreen()
        }
    }
}