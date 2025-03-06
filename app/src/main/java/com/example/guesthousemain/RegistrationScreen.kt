package com.example.guesthousemain.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.RegisterUserRequest
import com.example.guesthousemain.network.RegisterUserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun RegisterScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    var emailField by remember { mutableStateOf(email) }
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "REGISTER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = emailField,
                        onValueChange = { emailField = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Contact") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (emailField.isEmpty() || name.isEmpty() || contact.isEmpty()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            val request = RegisterUserRequest(
                                email = emailField,
                                name = name,
                                contact = contact
                            )
                            ApiService.authService.registerUser(request)
                                .enqueue(object : Callback<RegisterUserResponse> {
                                    override fun onResponse(
                                        call: Call<RegisterUserResponse>,
                                        response: Response<RegisterUserResponse>
                                    ) {
                                        isLoading = false
                                        if (response.isSuccessful && response.body() != null) {
                                            val body = response.body()!!
                                            // If the user already exists, navigate to main page.
                                            if (!body.success && body.message == "User already exists") {
                                                Toast.makeText(
                                                    context,
                                                    "User already exists. Redirecting to main page.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate("main") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else if (body.success) {
                                                Toast.makeText(
                                                    context,
                                                    body.message ?: "User added successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Registration successful; navigate to main page.
                                                navController.navigate("main") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    body.message ?: "Registration failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error: ${response.message()}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    override fun onFailure(call: Call<RegisterUserResponse>, t: Throwable) {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Failure: ${t.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (isLoading) "Registering..." else "Register")
                    }
                }
            }
        }
    }
}
