package com.example.guesthousemain.ui

import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.OtpRequest
import com.example.guesthousemain.network.OtpResponse
import com.example.guesthousemain.network.OtpVerifyRequest
import com.example.guesthousemain.network.OtpVerifyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun OtpVerificationScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    var isDisabled by remember { mutableStateOf(false) }
    var timerText by remember { mutableStateOf("") }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Countdown timer for 30 seconds (for "Resend OTP")
    val countdownTimer = remember {
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                timerText = "Time Remaining: ${seconds}s"
            }
            override fun onFinish() {
                isTimerRunning = false
                timerText = ""
            }
        }
    }

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
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Enter OTP") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Call your OTP verification endpoint
                    if (otp.isNotEmpty()) {
                        isDisabled = true
                        ApiService.authService.verifyOtp(
                            OtpVerifyRequest(email, otp)
                        ).enqueue(object : Callback<OtpVerifyResponse> {
                            override fun onResponse(
                                call: Call<OtpVerifyResponse>,
                                response: Response<OtpVerifyResponse>
                            ) {
                                isDisabled = false
                                if (response.isSuccessful && response.body() != null) {
                                    if (response.body()!!.success == true) {
                                        Toast.makeText(
                                            context,
                                            "OTP verified successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("register/$email")
                                        // Proceed further or navigate to the next screen
                                    } else {
                                        Toast.makeText(
                                            context,
                                            response.body()?.message ?: "Verification failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Verification Error: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            override fun onFailure(call: Call<OtpVerifyResponse>, t: Throwable) {
                                isDisabled = false
                                Toast.makeText(
                                    context,
                                    "Verification Failure: ${t.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    } else {
                        Toast.makeText(context, "Please enter OTP", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isDisabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isDisabled) "Verifying..." else "Verify OTP", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Resend OTP logic: call the sendOtp endpoint
                    isTimerRunning = true
                    countdownTimer.start()
                    ApiService.authService.sendOtp(OtpRequest(email))
                        .enqueue(object : Callback<OtpResponse> {
                            override fun onResponse(
                                call: Call<OtpResponse>,
                                response: Response<OtpResponse>
                            ) {
                                if (response.isSuccessful && response.body() != null) {
                                    Toast.makeText(
                                        context,
                                        "OTP resent successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Resend Error: ${response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                                Toast.makeText(
                                    context,
                                    "Resend Failure: ${t.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                },
                enabled = !isTimerRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Resend OTP", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (timerText.isNotEmpty()) {
                Text(text = timerText, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
