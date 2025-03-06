//package com.example.guesthousemain.ui.screens
//
//import android.widget.Toast
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.Button
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun ReservationFormScreen() {
//    val context = LocalContext.current
//
//    // State variables for the form fields
//    var guestName by remember { mutableStateOf("") }
//    var numberOfGuests by remember { mutableStateOf("") }
//    var numberOfRooms by remember { mutableStateOf("") }
//    var roomType by remember { mutableStateOf("") }
//    var purpose by remember { mutableStateOf("") }
//    var arrivalDate by remember { mutableStateOf("") }
//    var arrivalTime by remember { mutableStateOf("") }
//    var departureDate by remember { mutableStateOf("") }
//    var departureTime by remember { mutableStateOf("") }
//    var address by remember { mutableStateOf("") }
//    var category by remember { mutableStateOf("") }
//    var source by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Reservation Form",
//            fontSize = 24.sp,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        OutlinedTextField(
//            value = guestName,
//            onValueChange = { guestName = it },
//            label = { Text("Guest Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = numberOfGuests,
//            onValueChange = { numberOfGuests = it },
//            label = { Text("Number of Guests") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = numberOfRooms,
//            onValueChange = { numberOfRooms = it },
//            label = { Text("Number of Rooms") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = roomType,
//            onValueChange = { roomType = it },
//            label = { Text("Room Type") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = purpose,
//            onValueChange = { purpose = it },
//            label = { Text("Purpose") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = arrivalDate,
//            onValueChange = { arrivalDate = it },
//            label = { Text("Arrival Date (YYYY-MM-DD)") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = arrivalTime,
//            onValueChange = { arrivalTime = it },
//            label = { Text("Arrival Time (HH:MM)") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = departureDate,
//            onValueChange = { departureDate = it },
//            label = { Text("Departure Date (YYYY-MM-DD)") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = departureTime,
//            onValueChange = { departureTime = it },
//            label = { Text("Departure Time (HH:MM)") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = address,
//            onValueChange = { address = it },
//            label = { Text("Address") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = category,
//            onValueChange = { category = it },
//            label = { Text("Category") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = source,
//            onValueChange = { source = it },
//            label = { Text("Source") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                // Validate required fields.
//                if (guestName.isEmpty() || numberOfGuests.isEmpty() || numberOfRooms.isEmpty() ||
//                    roomType.isEmpty() || purpose.isEmpty() || arrivalDate.isEmpty() ||
//                    arrivalTime.isEmpty() || departureDate.isEmpty() || departureTime.isEmpty() ||
//                    address.isEmpty() || category.isEmpty() || source.isEmpty()
//                ) {
//                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
//                } else {
//                    // Here, you would call your Retrofit API to create a reservation.
//                    // For now, we simulate submission with a Toast.
//                    Toast.makeText(context, "Reservation submitted successfully", Toast.LENGTH_SHORT).show()
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Submit Reservation")
//        }
//    }
//}
