package com.example.guesthousemain.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.R
import com.example.guesthousemain.network.ApiService
import com.example.guesthousemain.network.GoogleSignInRequest
import com.example.guesthousemain.network.LoginResponse
import com.example.guesthousemain.network.OtpRequest
import com.example.guesthousemain.network.OtpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun SlidingBackgroundLogin(images: List<Int>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val coroutineScope = rememberCoroutineScope()
    val transition = rememberInfiniteTransition(label = "Background Animation")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Background Scale Animation"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(
                    nextPage,
                    animationSpec = tween(1000, easing = EaseInOutCubic)
                )
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .blur(2.dp),
                contentScale = ContentScale.Crop
            )
            // Add a subtle gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
    }

    // Page indicators
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(images.size) { iteration ->
            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
            val size = if (pagerState.currentPage == iteration) 10.dp else 8.dp

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var emailPrefix by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf("@iitrpr.ac.in") } // Default domain
    var isLoading by remember { mutableStateOf(false) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isCustomDomain by remember { mutableStateOf(false) }
    var customEmail by remember { mutableStateOf("") }

    // Google Sign-In setup
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id)) // Ensure this is set in your strings.xml
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Create launcher for Google sign-in intent
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.email?.let { email ->
                    isLoading = true

                    // Directly send OTP for the selected email
                    ApiService.authService.sendOtp(OtpRequest(email)).enqueue(object : Callback<OtpResponse> {
                        override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                            isLoading = false
                            if (response.isSuccessful && response.body() != null) {
                                Toast.makeText(context, "OTP sent successfully to $email", Toast.LENGTH_SHORT).show()
                                // Navigate directly to OTP verification with the email
                                navController.navigate("otpVerification/$email")
                            } else {
                                if (response.code() == 404) {
                                    Toast.makeText(context, "Email not registered. Please register first.", Toast.LENGTH_SHORT).show()
                                    navController.navigate("registration/$email")
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: ${response.errorBody()?.string() ?: response.message()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(context, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isLoading = false
        }
    }


    // Create a ScrollState for vertical scrolling
    val scrollState = rememberScrollState()

    // Create a FocusManager to handle keyboard visibility
    val focusManager = LocalFocusManager.current

    // Enhanced animations for the login content
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(800, easing = EaseOutQuint)) }
        launch { scale.animateTo(1f, tween(1000, easing = EaseOutBack)) }
        launch {
            delay(300)
            logoRotation.animateTo(360f, tween(1200, easing = EaseOutCubic))
        }
    }

    // Common domains for quick selection
    val commonDomains = listOf(
        "@iitrpr.ac.in",
        "@gmail.com",
        "@outlook.com",
        "@yahoo.com",
        "@hotmail.com"
    )

    // Validation function
    fun validateEmail(): Boolean {
        return if (isCustomDomain) {
            customEmail.contains("@") &&
                    customEmail.indexOf("@") > 0 &&
                    customEmail.indexOf("@") < customEmail.length - 1
        } else {
            emailPrefix.isNotEmpty()
        }
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

        // Add keyboard actions to close keyboard when tapping outside or when done
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale.value)
                .alpha(alpha.value),
            contentAlignment = Alignment.Center
        ) {
            // Wrap the main content in a scrollable column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(70.dp))

                // Animated logo
                Image(
                    painter = painterResource(id = R.drawable.iit_ropar_logo),
                    contentDescription = "IIT Ropar Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            rotationY = logoRotation.value
                        }
                )

                Text(
                    "Indian Institute of Technology Ropar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    "Welcome",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    "Sign in to continue",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale.value),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "LOG IN",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Toggle between IIT email and custom email
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isCustomDomain) Color(0xFF6200EE) else Color.White.copy(alpha = 0.1f))
                                    .clickable { isCustomDomain = false }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "IIT Ropar",
                                    color = Color.White,
                                    fontWeight = if (!isCustomDomain) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCustomDomain) Color(0xFF6200EE) else Color.White.copy(alpha = 0.1f))
                                    .clickable { isCustomDomain = true }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Other Email",
                                    color = Color.White,
                                    fontWeight = if (isCustomDomain) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        // IIT Ropar Email Input
                        AnimatedVisibility(
                            visible = !isCustomDomain,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                // Username with domain suffix
                                OutlinedTextField(
                                    value = emailPrefix,
                                    onValueChange = {
                                        emailPrefix = it.replace("@", "")
                                        isEmailValid = true
                                    },
                                    placeholder = { Text("Username", color = Color.White.copy(alpha = 0.6f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = "Email Icon",
                                            tint = Color.White.copy(alpha = 0.8f)
                                        )
                                    },
                                    trailingIcon = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF6200EE).copy(alpha = 0.5f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    selectedDomain,
                                                    color = Color.White,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    },
                                    isError = !isEmailValid,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                        cursorColor = Color.White
                                    ),
                                    singleLine = true
                                )

                                // Quick domain selector
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    commonDomains.take(3).forEach { domain ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (domain == selectedDomain)
                                                        Color(0xFF6200EE).copy(alpha = 0.7f)
                                                    else
                                                        Color.White.copy(alpha = 0.1f)
                                                )
                                                .clickable {
                                                    isCustomDomain = false
                                                    selectedDomain = domain
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = domain.replace("@", ""),
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Email Input
                        AnimatedVisibility(
                            visible = isCustomDomain,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = customEmail,
                                onValueChange = {
                                    customEmail = it
                                    isEmailValid = true
                                },
                                placeholder = { Text("Enter your email address", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = "Email Icon",
                                        tint = Color.White.copy(alpha = 0.8f)
                                    )
                                },
                                isError = !isEmailValid,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                    cursorColor = Color.White
                                ),
                                singleLine = true
                            )
                        }

                        if (!isEmailValid) {
                            Text(
                                if (isCustomDomain) "Please enter a valid email address"
                                else "Please enter your username",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        val buttonScale by animateFloatAsState(
                            if (isLoading) 0.95f else 1f,
                            tween(300),
                            label = "Button Scale Animation"
                        )

                        // OTP Sign-in Button
                        Button(
                            onClick = {
                                // Clear focus to hide keyboard when button is clicked
                                focusManager.clearFocus()

                                val fullEmail = if (isCustomDomain) {
                                    customEmail
                                } else {
                                    "$emailPrefix$selectedDomain"
                                }

                                if (validateEmail()) {
                                    isLoading = true
                                    ApiService.authService.sendOtp(OtpRequest(fullEmail)).enqueue(object : Callback<OtpResponse> {
                                        override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                                            isLoading = false
                                            if (response.isSuccessful && response.body() != null) {
                                                Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                                                navController.navigate("otpVerification/$fullEmail")
                                            } else {
                                                Toast.makeText(context, "Error: ${response.errorBody()?.string() ?: response.message()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                                            isLoading = false
                                            Toast.makeText(context, "Failure: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                } else {
                                    isEmailValid = false
                                    if ((isCustomDomain && customEmail.isEmpty()) || (!isCustomDomain && emailPrefix.isEmpty())) {
                                        Toast.makeText(
                                            context,
                                            if (isCustomDomain) "Please enter your email address" else "Please enter your username",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .scale(buttonScale),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6200EE),
                                disabledContainerColor = Color(0xFF6200EE).copy(alpha = 0.6f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                this@Button.AnimatedVisibility(
                                    visible = isLoading,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }

                                this@Button.AnimatedVisibility(
                                    visible = !isLoading,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        "Send OTP",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Google Sign-in Button
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                            ),
                            enabled = !isLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.google_logo),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Uncomment and implement this if you need a help/support option
                        // TextButton(
                        //     onClick = { /* Handle help/support */ },
                        //     modifier = Modifier.padding(top = 8.dp)
                        // ) {
                        //     Text(
                        //         "Need Help?",
                        //         color = Color.White.copy(alpha = 0.8f),
                        //         fontSize = 16.sp
                        //     )
                        // }

                        // Extra space at the bottom for scrolling
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
