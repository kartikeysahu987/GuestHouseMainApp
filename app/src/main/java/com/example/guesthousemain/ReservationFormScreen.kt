package com.example.guesthousemain.ui.screens

import android.content.Context
import android.icu.util.Calendar
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.CreateReservationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreen(
    accessToken: String,
    refreshToken: String,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State variables for form fields
    var guestName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var numberOfGuests by remember { mutableStateOf("") }
    var numberOfRooms by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var arrivalDate by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var roomOccupancy by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var reviewers by remember { mutableStateOf("") }

    // Validation states
    var guestNameError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var numberOfGuestsError by remember { mutableStateOf<String?>(null) }
    var numberOfRoomsError by remember { mutableStateOf<String?>(null) }
    var arrivalDateError by remember { mutableStateOf<String?>(null) }
    var departureDateError by remember { mutableStateOf<String?>(null) }

    // Date picker states
    var showArrivalDatePicker by remember { mutableStateOf(false) }
    var showDepartureDatePicker by remember { mutableStateOf(false) }

    // Upload states
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var receiptName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Dropdown states
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedRoomOccupancy by remember { mutableStateOf(false) }
    var expandedSource by remember { mutableStateOf(false) }
    var expandedReviewers by remember { mutableStateOf(false) }

    // Category options - based on screenshots
    val categoryOptions = listOf(
        "Executive Suite - Category A (Free)",
        "Executive Suite - Category B (₹3500)",
        "Business Room - Category A (Free)",
        "Business Room - Category B1 (₹2000)",
        "Business Room - Category B2 (₹1200)"
    )

    // Room occupancy options - dynamically updated based on selected category
    var roomOccupancyOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    // Source options
    val sources = listOf("GUEST", "CONFERENCE", "OFFICIAL", "PERSONAL")

    // Reviewers options - dynamically updated based on selected category
    var reviewerOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    // Update room occupancy options when category changes
    LaunchedEffect(category) {
        roomOccupancyOptions = when {
            category.contains("Executive Suite") -> {
                if (category.contains("Category B")) {
                    listOf("Single Occupancy (₹3500/- only)", "Double Occupancy (₹3500/- only)")
                } else {
                    listOf("Single Occupancy", "Double Occupancy")
                }
            }
            category.contains("Business Room") -> {
                when {
                    category.contains("Category B1") -> listOf("Single Occupancy (₹2000/- only)", "Double Occupancy (₹2000/- only)")
                    category.contains("Category B2") -> listOf("Single Occupancy (₹1200/- only)", "Double Occupancy (₹1200/- only)")
                    else -> listOf("Single Occupancy", "Double Occupancy")
                }
            }
            else -> emptyList()
        }

        // Reset room occupancy when category changes
        roomOccupancy = ""
    }

    // Update reviewer options when category changes
    LaunchedEffect(category) {
        reviewerOptions = when {
            category.contains("Executive Suite - Category A") ->
                listOf("Director", "Concerned Dean")
            category.contains("Executive Suite - Category B") ->
                listOf("Chairman, Guest House Committee")
            category.contains("Business Room - Category A") ->
                listOf("Registrar", "Concerned Dean", "Associate Dean", "Director (for other guests)")
            category.contains("Business Room - Category B1") ->
                listOf("Concerned Deans", "Associate Deans", "HoDs", "Registrar")
            category.contains("Business Room - Category B2") ->
                listOf("Chairman, Guest House Committee")
            else -> emptyList()
        }

        // Reset reviewer when category changes
        reviewers = ""
    }

    // Function to map UI selections to backend category codes
    fun mapToCategoryCode(category: String): String {
        return when {
            category.contains("Executive Suite - Category A") -> "ES-A"
            category.contains("Executive Suite - Category B") -> "ES-B"
            category.contains("Business Room - Category A") -> "BR-A"
            category.contains("Business Room - Category B1") -> "BR-B1"
            category.contains("Business Room - Category B2") -> "BR-B2"
            else -> ""
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            receiptUri = it
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                receiptName = cursor.getString(nameIndex)
            }
        }
    }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        // Reset errors
        guestNameError = null
        addressError = null
        numberOfGuestsError = null
        numberOfRoomsError = null
        arrivalDateError = null
        departureDateError = null
        formError = null

        // Validate guest name
        if (guestName.isBlank()) {
            guestNameError = "Guest name is required"
            isValid = false
        }

        // Validate address
        if (address.isBlank()) {
            addressError = "Address is required"
            isValid = false
        }

        // Validate number of guests
        if (numberOfGuests.isBlank()) {
            numberOfGuestsError = "Number of guests is required"
            isValid = false
        } else if (numberOfGuests.toIntOrNull() == null || numberOfGuests.toInt() <= 0) {
            numberOfGuestsError = "Please enter a valid number"
            isValid = false
        }

        // Validate number of rooms
        if (numberOfRooms.isBlank()) {
            numberOfRoomsError = "Number of rooms is required"
            isValid = false
        } else if (numberOfRooms.toIntOrNull() == null || numberOfRooms.toInt() <= 0) {
            numberOfRoomsError = "Please enter a valid number"
            isValid = false
        }

        // Validate dates
        if (arrivalDate.isBlank()) {
            arrivalDateError = "Arrival date is required"
            isValid = false
        }

        if (departureDate.isBlank()) {
            departureDateError = "Departure date is required"
            isValid = false
        }

        // Validate category, room occupancy and reviewers
        if (category.isBlank()) {
            formError = "Please select a category"
            isValid = false
        } else if (roomOccupancy.isBlank()) {
            formError = "Please select room occupancy type"
            isValid = false
        } else if (reviewers.isBlank()) {
            formError = "Please select an approving authority"
            isValid = false
        }

        // Validate source
        if (source.isBlank()) {
            formError = "Please select a source"
            isValid = false
        }

        // Validate receipt
        if (receiptUri == null) {
            formError = "Please upload a receipt PDF before submitting"
            isValid = false
        }

        return isValid
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSuccess()
            },
            title = { Text("Success") },
            text = { Text("Your reservation has been submitted successfully!") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    onSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Date pickers
    if (showArrivalDatePicker) {
        DatePickerDialog(
            context = context,
            onDismissRequest = { showArrivalDatePicker = false },
            onDateSelected = { date ->
                arrivalDate = date
                showArrivalDatePicker = false
            }
        )
    }

    if (showDepartureDatePicker) {
        DatePickerDialog(
            context = context,
            onDismissRequest = { showDepartureDatePicker = false },
            onDateSelected = { date ->
                departureDate = date
                showDepartureDatePicker = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Guest House Reservation",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Please fill in all required fields to complete your reservation",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Personal Information Section
            FormSection(title = "Personal Information") {
                // Guest Name
                FormTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = "Guest Name*",
                    errorMessage = guestNameError,
                    leadingIcon = Icons.Default.Person
                )

                // Address
                FormTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address*",
                    errorMessage = addressError,
                    leadingIcon = Icons.Default.Home
                )
            }

            // Reservation Details Section
            FormSection(title = "Reservation Details") {
                // Number of Guests
                FormTextField(
                    value = numberOfGuests,
                    onValueChange = { numberOfGuests = it },
                    label = "Number of Guests*",
                    keyboardType = KeyboardType.Number,
                    errorMessage = numberOfGuestsError,
                    leadingIcon = Icons.Default.Group
                )

                // Number of Rooms
                FormTextField(
                    value = numberOfRooms,
                    onValueChange = { numberOfRooms = it },
                    label = "Number of Rooms*",
                    keyboardType = KeyboardType.Number,
                    errorMessage = numberOfRoomsError,
                    leadingIcon = Icons.Default.MeetingRoom
                )

                // Purpose
                FormTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = "Purpose of Visit*",
                    leadingIcon = Icons.Default.Info
                )
            }

            // Date and Time Section
            FormSection(title = "Date and Time") {
                // Arrival Date
                DatePickerField(
                    value = arrivalDate,
                    onValueChange = { arrivalDate = it },
                    label = "Arrival Date*",
                    errorMessage = arrivalDateError,
                    onClick = { showArrivalDatePicker = true }
                )

                // Arrival Time
                FormTextField(
                    value = arrivalTime,
                    onValueChange = { arrivalTime = it },
                    label = "Arrival Time (HH:MM)*",
                    placeholder = "e.g., 14:30",
                    leadingIcon = Icons.Default.Schedule
                )

                // Departure Date
                DatePickerField(
                    value = departureDate,
                    onValueChange = { departureDate = it },
                    label = "Departure Date*",
                    errorMessage = departureDateError,
                    onClick = { showDepartureDatePicker = true }
                )

                // Departure Time
                FormTextField(
                    value = departureTime,
                    onValueChange = { departureTime = it },
                    label = "Departure Time (HH:MM)*",
                    placeholder = "e.g., 11:00",
                    leadingIcon = Icons.Default.Schedule
                )
            }

            // Additional Information Section
            FormSection(title = "Additional Information") {
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Category, contentDescription = null)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Room Occupancy Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedRoomOccupancy,
                    onExpandedChange = { expandedRoomOccupancy = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = roomOccupancy,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Room Type*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomOccupancy)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Hotel, contentDescription = null)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = category.isNotEmpty() && roomOccupancyOptions.isNotEmpty()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRoomOccupancy,
                        onDismissRequest = { expandedRoomOccupancy = false }
                    ) {
                        roomOccupancyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    roomOccupancy = option
                                    expandedRoomOccupancy = false
                                }
                            )
                        }
                    }
                }

                // Source Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedSource,
                    onExpandedChange = { expandedSource = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = source,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Source*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSource)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Source, contentDescription = null)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedSource,
                        onDismissRequest = { expandedSource = false }
                    ) {
                        sources.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    source = option
                                    expandedSource = false
                                }
                            )
                        }
                    }
                }

                // Approving Authority (Reviewers) Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedReviewers,
                    onExpandedChange = { expandedReviewers = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = reviewers,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Approving Authority*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedReviewers)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.SupervisorAccount, contentDescription = null)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = category.isNotEmpty() && reviewerOptions.isNotEmpty()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedReviewers,
                        onDismissRequest = { expandedReviewers = false }
                    ) {
                        reviewerOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    reviewers = option
                                    expandedReviewers = false
                                }
                            )
                        }
                    }
                }
            }

            // PDF Upload Section
            FormSection(title = "Receipt Upload") {
                // Upload area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = if (formError != null && receiptUri == null)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (receiptUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload PDF",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Upload your reservation receipt (PDF)",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FilledTonalButton(onClick = { pdfLauncher.launch("application/pdf") }) {
                                Text("Browse Files")
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "PDF File",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                receiptName,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { pdfLauncher.launch("application/pdf") }) {
                                Text("Change File")
                            }
                        }
                    }
                }
            }

            // Form error message
            if (formError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        formError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    if (validateForm()) {
                        isLoading = true
                        formError = null

                        // Extract occupancy type from selection (Single/Double)
                        val occupancyType = if (roomOccupancy.startsWith("Single")) "Single Occupancy" else "Double Occupancy"

                        // Map the category to the correct backend format
                        val categoryCode = mapToCategoryCode(category)

                        coroutineScope.launch {
                            submitReservation(
                                context = context,
                                receiptUri = receiptUri!!,
                                accessToken = accessToken,
                                refreshToken = refreshToken,
                                guestName = guestName,
                                address = address,
                                numberOfGuests = numberOfGuests,
                                numberOfRooms = numberOfRooms,
                                roomType = occupancyType, // Send occupancy as room type
                                purpose = purpose,
                                arrivalDate = arrivalDate,
                                arrivalTime = arrivalTime,
                                departureDate = departureDate,
                                departureTime = departureTime,
                                category = categoryCode, // Send mapped category code (ES-A, etc)
                                source = source,
                                reviewers = reviewers,
                                onSuccess = {
                                    isLoading = false
                                    showSuccessDialog = true
                                },
                                onError = { error ->
                                    isLoading = false
                                    formError = error
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Submit Reservation",
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = errorMessage != null,
            readOnly = true,
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    context: Context,
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(date)
            },
            year,
            month,
            day
        )
    }

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose {
            datePickerDialog.dismiss()
        }
    }
}

private fun createPartFromString(data: String): RequestBody {
    return data.toRequestBody("text/plain".toMediaTypeOrNull())
}

private suspend fun submitReservation(
    context: Context,
    receiptUri: Uri,
    accessToken: String,
    refreshToken: String,
    guestName: String,
    address: String,
    numberOfGuests: String,
    numberOfRooms: String,
    roomType: String,
    purpose: String,
    arrivalDate: String,
    arrivalTime: String,
    departureDate: String,
    departureTime: String,
    category: String,
    source: String,
    reviewers: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) = withContext(Dispatchers.IO) {
    try {
        val receiptFile = uriToFile(context, receiptUri)
        val requestFile = receiptFile.asRequestBody("application/pdf".toMediaTypeOrNull())
        val receiptPart = MultipartBody.Part.createFormData("receipt", receiptFile.name, requestFile)

        // Create request parts from form data
        val guestNamePart = createPartFromString(guestName)
        val addressPart = createPartFromString(address)
        val numberOfGuestsPart = createPartFromString(numberOfGuests)
        val numberOfRoomsPart = createPartFromString(numberOfRooms)
        val roomTypePart = createPartFromString(roomType)
        val purposePart = createPartFromString(purpose)
        val arrivalDatePart = createPartFromString(arrivalDate)
        val arrivalTimePart = createPartFromString(arrivalTime)
        val departureDatePart = createPartFromString(departureDate)
        val departureTimePart = createPartFromString(departureTime)
        val categoryPart = createPartFromString(category)
        val sourcePart = createPartFromString(source)
        val reviewersPart = createPartFromString(reviewers)

        ApiService.authService.createReservationMultipart(
            accessToken,
            refreshToken,
            receiptPart,
            guestNamePart,
            addressPart,
            numberOfGuestsPart,
            numberOfRoomsPart,
            roomTypePart,
            purposePart,
            arrivalDatePart,
            arrivalTimePart,
            departureDatePart,
            departureTimePart,
            categoryPart,
            sourcePart,
            reviewersPart
        ).enqueue(object : Callback<CreateReservationResponse> {
            override fun onResponse(
                call: Call<CreateReservationResponse>,
                response: Response<CreateReservationResponse>
            ) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: "Unknown error"
                    onError("Error: ${response.code()} - $errorBodyString")
                }
            }

            override fun onFailure(call: Call<CreateReservationResponse>, t: Throwable) {
                onError("Network Error: ${t.message ?: "Unknown network error"}")
            }
        })
    } catch (e: Exception) {
        onError("Exception: ${e.message ?: "Unknown error"}")
    }
}

private fun uriToFile(context: Context, uri: Uri): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.pdf")
    inputStream?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}
