package com.example.guesthousemain.ui

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.R
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.RegisterUserRequest
import com.example.guesthousemain.network.RegisterUserResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun SlidingBackgroundReg(images: List<Int>) {
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
fun RegisterScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    var emailField by remember { mutableStateOf(email) }
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var isDisabled by remember { mutableStateOf(false) }

    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Launch animation when screen loads
    LaunchedEffect(Unit) {
        launch{ alpha.animateTo(1f, animationSpec = tween(durationMillis = 600,easing = FastOutSlowInEasing)) }
        launch{scale.animateTo(1f, animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing))}
    }
    val imageList = listOf(
        R.drawable.spiral,
        R.drawable.cs,
        R.drawable.ee,
        R.drawable.hs,
        R.drawable.admin,
        R.drawable.lhc
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    verticalArrangement = Arrangement.Center,
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
                        text = "Welcome",
                        color = Color.Black,
                        fontSize = 20.sp,
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
                            //Button Animation
                            val scale by animateFloatAsState(
                                targetValue = if (isDisabled) 0.95f else 1f,
                                animationSpec = tween(durationMillis = 300),
                                label = "Button Scale Animation"
                            )
                            Button(
                                onClick = {
                                    if (emailField.isEmpty() || name.isEmpty() || contact.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Please fill all fields",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
                                                            body.message
                                                                ?: "User added successfully",
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

                                            override fun onFailure(
                                                call: Call<RegisterUserResponse>,
                                                t: Throwable
                                            ) {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Failure: ${t.localizedMessage}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                },
                                modifier = Modifier.fillMaxWidth() .scale(scale)
                            ) {
                                Text(text = if (isLoading) "Registering..." else "Register")
                            }
                        }
                    }
                }
            }
        }
    }
}
