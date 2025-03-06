// File: LoginScreen.kt
package com.example.guesthousemain.ui

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.R
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.OtpRequest
import com.example.guesthousemain.network.OtpResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun SlidingBackgroundLogin(images: List<Int>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isDisabled by remember { mutableStateOf(false) }

    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { scale.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
    }

    val imageList = listOf(
        R.drawable.spiral,
        R.drawable.cs,
        R.drawable.ee,
        R.drawable.hs,
        R.drawable.admin,
        R.drawable.lhc
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SlidingBackgroundLogin(imageList)

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Box(
            modifier = Modifier.fillMaxSize().scale(scale.value).alpha(alpha.value),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Image(
                    painter = painterResource(id = R.drawable.iit_ropar_logo),
                    contentDescription = "IIT Ropar Logo",
                    modifier = Modifier.size(100.dp)
                )
                Text("Indian Institute of Technology Ropar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(50.dp))
                Text("Welcome", color = Color.Black, fontSize = 22.sp, modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().scale(scale.value),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LOG IN", fontSize = 20.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Enter your email", color = Color.Black) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = Color.Black),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val buttonScale by animateFloatAsState(if (isDisabled) 0.95f else 1f, tween(300), label = "Button Scale Animation")
                        Button(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    isDisabled = true
                                    ApiService.authService.sendOtp(OtpRequest(email)).enqueue(object : Callback<OtpResponse> {
                                        override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                                            isDisabled = false
                                            if (response.isSuccessful && response.body() != null) {
                                                Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                                                navController.navigate("otpVerification/$email")
                                            } else {
                                                Toast.makeText(context, "Error: ${response.errorBody()?.string() ?: response.message()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                                            isDisabled = false
                                            Toast.makeText(context, "Failure: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                } else {
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isDisabled,
                            modifier = Modifier.fillMaxWidth().scale(buttonScale),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text(if (isDisabled) "Sending OTP..." else "Send OTP", fontSize = 16.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Sign-in Button
                        Button(
                            onClick = { /* Google Sign-in logic */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.google_logo),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Sign in with Google", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
