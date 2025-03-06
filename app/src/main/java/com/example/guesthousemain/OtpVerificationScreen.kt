package com.example.guesthousemain.ui

import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.R
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.LoginRequest
import com.example.guesthousemain.network.LoginResponse
import com.example.guesthousemain.network.OtpRequest
import com.example.guesthousemain.network.OtpResponse
import com.example.guesthousemain.network.OtpVerifyRequest
import com.example.guesthousemain.network.OtpVerifyResponse
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign


@Composable
fun SlidingBackgroundOtp(images: List<Int>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-slide logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Change image every 3 seconds
            coroutineScope.launch {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % images.size)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Image(
            painter = painterResource(id = images[page]),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
@Composable
fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = List(6) { FocusRequester() }
    var otpValues by remember { mutableStateOf(List(6) { "" }) }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        otpValues.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                        otpValues = otpValues.toMutableList().also { it[index] = newValue }
                        onOtpChange(otpValues.joinToString(""))

                        if (newValue.isNotEmpty() && index < 5) {
                            focusManager.moveFocus(FocusDirection.Next)
                        } else if (newValue.isEmpty() && index > 0) {
                            focusManager.moveFocus(FocusDirection.Previous)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(48.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = MaterialTheme.typography.headlineSmall,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}



@Composable
fun OtpVerificationScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current

    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Launch animations when screen loads
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing)) }
        launch { scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing)) }
    }

    val imageList = listOf(
        R.drawable.spiral,
        R.drawable.cs,
        R.drawable.ee,
        R.drawable.hs,
        R.drawable.admin,
        R.drawable.lhc
    )

    var isDisabled by remember { mutableStateOf(false) }
    var timerText by remember { mutableStateOf("") }
    var isTimerRunning by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    val countdownTimer = remember {
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isTimerRunning = false
                timerText = ""
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SlidingBackgroundOtp(imageList)

        // Animated Login UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale.value)  // Apply Scale Animation
                .alpha(alpha.value), // Apply Fade-in Animation
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Image(
                    painter = painterResource(id = R.drawable.iit_ropar_logo),
                    contentDescription = "IIT Ropar Logo",
                    modifier = Modifier.size(80.dp)
                )
                // Institute Name
                Text(
                    text = "Indian Institute of Technology Ropar",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = "OTP Verification",
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                        .scale(scale.value),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Enter the OTP sent to $email",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        var otp by remember { mutableStateOf("") }

                        OtpInputField(otp = otp, onOtpChange = { otp = it })

                        Spacer(modifier = Modifier.height(16.dp))
                        //Button Animation
                        val scale by animateFloatAsState(
                            targetValue = if (isDisabled) 0.95f else 1f,
                            animationSpec = tween(durationMillis = 300),
                            label = "Button Scale Animation"
                        )
                        Button(
                            onClick = {
                                if (otp.isNotEmpty()) {
                                    isDisabled = true
                                    // Call API to verify OTP
                                    // First, verify the OTP
                                    ApiService.authService.verifyOtp(
                                        OtpVerifyRequest(email, otp)
                                    ).enqueue(object : Callback<OtpVerifyResponse> {
                                        override fun onResponse(
                                            call: Call<OtpVerifyResponse>,
                                            response: Response<OtpVerifyResponse>
                                        ) {
                                            isDisabled = false
                                            if (response.isSuccessful && response.body() != null) {
                                                val verifyResponse = response.body()!!
                                                if (verifyResponse.success == true) {
                                                    if (!verifyResponse.user.isNullOrEmpty()) {
                                                        // User exists: call login route to update tokens.
                                                        Toast.makeText(
                                                            context,
                                                            "OTP verified. Logging in...",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        ApiService.authService.loginUser(
                                                            LoginRequest(email, otp)
                                                        ).enqueue(object : Callback<LoginResponse> {
                                                            override fun onResponse(
                                                                call: Call<LoginResponse>,
                                                                response: Response<LoginResponse>
                                                            ) {
                                                                if (response.isSuccessful && response.body() != null) {
                                                                    val loginResponse =
                                                                        response.body()!!
                                                                    if (loginResponse.success) {
                                                                        // Store tokens persistently with "Bearer " prefix.
                                                                        SessionManager.saveTokens(
                                                                            context,
                                                                            "Bearer " + (loginResponse.accessToken
                                                                                ?: ""),
                                                                            "Bearer " + (loginResponse.refreshToken
                                                                                ?: "")
                                                                        )
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Login successful",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        navController.navigate("main") {
                                                                            popUpTo("login") {
                                                                                inclusive = true
                                                                            }
                                                                        }
                                                                    } else {
                                                                        Toast.makeText(
                                                                            context,
                                                                            loginResponse.message
                                                                                ?: "Login failed",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                } else {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Login error: ${response.message()}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }

                                                            override fun onFailure(
                                                                call: Call<LoginResponse>,
                                                                t: Throwable
                                                            ) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Login failure: ${t.localizedMessage}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        })
                                                    } else {
                                                        // User doesn't exist: navigate to registration.
                                                        Toast.makeText(
                                                            context,
                                                            "OTP verified. Proceed to registration.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        navController.navigate("register/$email")
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        verifyResponse.message
                                                            ?: "Verification failed",
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

                                        override fun onFailure(
                                            call: Call<OtpVerifyResponse>,
                                            t: Throwable
                                        ) {
                                            isDisabled = false
                                            Toast.makeText(
                                                context,
                                                "Verification Failure: ${t.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                                } else {
                                    Toast.makeText(context, "Please enter OTP", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            enabled = !isDisabled,
                            modifier = Modifier.fillMaxWidth() .scale(scale),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                        ) {
                            Text(
                                text = if (isDisabled) "Verifying..." else "Verify OTP",
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isTimerRunning = true
                                countdownTimer.start()
                                // Call API to resend OTP
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

                                        override fun onFailure(
                                            call: Call<OtpResponse>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(
                                                context,
                                                "Resend Failure: ${t.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            },
                            enabled = !isTimerRunning,
                            modifier = Modifier.fillMaxWidth() .scale(scale),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
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
        }
    }
}

