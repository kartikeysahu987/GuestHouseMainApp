// File: LoginScreen.kt
package com.example.guesthousemain.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.OtpRequest
import com.example.guesthousemain.network.OtpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isDisabled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (email.isNotEmpty()) {
                        isDisabled = true
                        // Call your Node.js endpoint to send OTP
                        ApiService.authService.sendOtp(OtpRequest(email))
                            .enqueue(object : Callback<OtpResponse> {
                                override fun onResponse(
                                    call: Call<OtpResponse>,
                                    response: Response<OtpResponse>
                                ) {
                                    isDisabled = false
                                    if (response.isSuccessful && response.body() != null) {
                                        Toast.makeText(
                                            context,
                                            "OTP sent successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Navigate to OTP Verification page with the email as argument
                                        navController.navigate("otpVerification/${email}")
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error: ${response.message()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                                    isDisabled = false
                                    Toast.makeText(
                                        context,
                                        "Failure: ${t.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } else {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isDisabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isDisabled) "Sending OTP..." else "Send OTP", fontSize = 16.sp)
            }
        }
    }
}
