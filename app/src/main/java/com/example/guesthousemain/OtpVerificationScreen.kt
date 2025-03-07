package com.example.guesthousemain.ui

import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.guesthousemain.R
import com.example.guesthousemain.network.*
import com.example.guesthousemain.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Define a cohesive color scheme with stable color values
object AppColors {
    // Primary brand color and variations
    val Primary = Color(0xFF3949AB)  // Indigo 600
    val PrimaryVariant = Color(0xFF1A237E)  // Indigo 900
    val PrimaryLight = Color(0xFF6F74DD)  // Lighter variant

    // Secondary accent color
    val Secondary = Color(0xFFFF6D00)  // Orange 800
    val SecondaryVariant = Color(0xFFFF9800)  // Orange 500

    // Background gradients
    val GradientStart = Color(0xFF1A237E).copy(alpha = 0.7f)  // Dark indigo with transparency
    val GradientEnd = Color(0xFF000000).copy(alpha = 0.75f)  // Black with transparency

    // Text colors
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.8f)
    val TextHint = Color.White.copy(alpha = 0.6f)

    // Card and surface colors
    val Surface = Color(0xFF303F9F).copy(alpha = 0.2f)  // Semi-transparent indigo
    val SurfaceLight = Color.White.copy(alpha = 0.12f)

    // Success and error colors
    val Success = Color(0xFF4CAF50)  // Green
    val Error = Color(0xFFF44336)  // Red

    // Disabled states
    val Disabled = Color.Gray.copy(alpha = 0.6f)
}

@Composable
fun SlidingBackgroundOtp(images: List<Int>) {
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
            // Add a more sophisticated gradient overlay with brand colors
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppColors.GradientStart,
                                AppColors.GradientEnd
                            )
                        )
                    )
            )
        }
    }

    // Page indicators at the bottom of the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) AppColors.Secondary else AppColors.TextSecondary
                val width = if (pagerState.currentPage == iteration) 24.dp else 10.dp

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                        .animateContentSize(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                )
            }
        }
    }
}

@Composable
fun OTPTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6,
    onOtpTextChange: (String) -> Unit
) {
    LaunchedEffect(otpText) {
        if (otpText.length > otpCount) {
            onOtpTextChange(otpText.substring(0, otpCount))
        }
    }

    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(otpCount) { FocusRequester() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until otpCount) {
            val isFocused = remember { mutableStateOf(false) }
            val char = when {
                i >= otpText.length -> ""
                else -> otpText[i].toString()
            }

            val interactionSource = remember { MutableInteractionSource() }
            val borderColor = if (isFocused.value) AppColors.Secondary else Color(0xCCFFFFFF)
            val backgroundColor = if (isFocused.value) Color(0x40FFFFFF) else Color(0x1FFFFFFF)
            val animation = remember { Animatable(0f) }

            LaunchedEffect(char) {
                if (char.isNotEmpty()) {
                    animation.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                    )
                } else {
                    animation.snapTo(0f)
                }
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        focusRequesters[i].requestFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .scale(0.8f + (animation.value * 0.2f))
                        .alpha(animation.value)
                )

                BasicTextField(
                    value = "",  // Keep this empty to avoid cursor positioning issues
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            // Handle backspace - remove the character at current position
                            if (otpText.isNotEmpty() && i < otpText.length) {
                                val newOtp = otpText.substring(0, i) + otpText.substring(i + 1)
                                onOtpTextChange(newOtp)

                                // Move focus to previous field if not at first field
                                if (i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                }
                            } else if (i > 0 && otpText.length == i) {
                                // If at the end of text, remove last character
                                val newOtp = otpText.substring(0, otpText.length - 1)
                                onOtpTextChange(newOtp)

                                // Move focus to previous field
                                focusRequesters[i - 1].requestFocus()
                            }
                        } else if (newValue.all { it.isDigit() }) {
                            // Create new OTP string with the typed digit
                            val digit = newValue.last().toString()
                            val newOtp = if (i >= otpText.length) {
                                otpText + digit
                            } else {
                                val sb = StringBuilder(otpText)
                                sb.setCharAt(i, digit[0])
                                sb.toString()
                            }

                            onOtpTextChange(newOtp)

                            // Move focus to next field if not at last field
                            if (i < otpCount - 1) {
                                focusRequesters[i + 1].requestFocus()
                            } else {
                                // Last digit entered, clear focus
                                focusManager.clearFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequesters[i])
                        .onFocusChanged { state ->
                            isFocused.value = state.isFocused
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    interactionSource = interactionSource,
                    textStyle = TextStyle(color = Color.White),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White)
                )
            }
        }
    }

    // Request focus on first empty box when component loads
    LaunchedEffect(Unit) {
        if (otpText.length < otpCount) {
            focusRequesters[otpText.length].requestFocus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(navController: NavHostController, email: String) {
    // Add a scrollable state
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }
    val yOffset = remember { Animatable(-50f) }

    var otp by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerText by remember { mutableStateOf("") }
    var timerProgress by remember { mutableStateOf(1f) }
    var countdownTimer by remember { mutableStateOf<CountDownTimer?>(null) }

    // Launch animations when screen loads
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, animationSpec = tween(800, easing = EaseOutQuint)) }
        launch { scale.animateTo(1f, animationSpec = tween(1000, easing = EaseOutBack)) }
        launch { yOffset.animateTo(0f, animationSpec = tween(800, easing = EaseOutQuint)) }
    }

    // Animation for success verification
    val successScale = animateFloatAsState(
        targetValue = if (isVerified) 1f else 0f,
        animationSpec = tween(500, easing = EaseOutBack),
        label = "Success Animation"
    )

    // Create countdown timer
    LaunchedEffect(Unit) {
        countdownTimer = object : CountDownTimer(30000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timerText = "${(millisUntilFinished / 1000) + 1}s"
                timerProgress = millisUntilFinished / 30000f
            }

            override fun onFinish() {
                isTimerRunning = false
                timerText = ""
                timerProgress = 0f
            }
        }

        // Start timer when component loads
        isTimerRunning = true
        countdownTimer?.start()
    }

    // Cleanup timer on dispose
    DisposableEffect(Unit) {
        onDispose {
            countdownTimer?.cancel()
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

    // Handle keyboard visibility
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Add imePadding to handle keyboard properly
            .imePadding()
            // Add clickable with no ripple to allow dismissing keyboard when clicking outside
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
    ) {
        SlidingBackgroundOtp(imageList)

        // Top app bar with back button
        CenterAlignedTopAppBar(
            title = { /* Empty title */ },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Animated Login UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale.value)
                .alpha(alpha.value)
                .graphicsLayer { translationY = yOffset.value },
            contentAlignment = Alignment.Center
        ) {
            // Add scrollable container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(70.dp))

                // Logo with pulsating animation
                val pulseAnimation = rememberInfiniteTransition(label = "Pulse Animation")
                val pulseFactor by pulseAnimation.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Pulse Scale Animation"
                )

                // Add a glow effect to the logo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColors.Primary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iit_ropar_logo),
                        contentDescription = "IIT Ropar Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseFactor)
                    )
                }

                // Institute Name with fade-in animation
                Text(
                    text = "Indian Institute of Technology Ropar",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(60.dp))

                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Add subtle glow to icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.Secondary.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "OTP Icon",
                            tint = AppColors.Secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "OTP Verification",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Email display
                Text(
                    text = "We've sent a verification code to",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = email,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Secondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Main content card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // OTP input fields - using our fixed implementation
                        OTPTextField(
                            otpText = otp,
                            onOtpTextChange = { otp = it },
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Verify Button with animation and gradient
                        val buttonScale by animateFloatAsState(
                            targetValue = if (isVerifying) 0.95f else 1f,
                            animationSpec = tween(durationMillis = 300),
                            label = "Button Scale Animation"
                        )

                        Button(
                            onClick = {
                                // Hide keyboard when button is clicked
                                keyboardController?.hide()

                                if (otp.length == 6) {
                                    isVerifying = true
                                    // Call API to verify OTP
                                    ApiService.authService.verifyOtp(
                                        OtpVerifyRequest(email, otp)
                                    ).enqueue(object : Callback<OtpVerifyResponse> {
                                        override fun onResponse(
                                            call: Call<OtpVerifyResponse>,
                                            response: Response<OtpVerifyResponse>
                                        ) {
                                            if (response.isSuccessful && response.body() != null) {
                                                val verifyResponse = response.body()!!
                                                if (verifyResponse.success == true) {
                                                    // Show success animation
                                                    coroutineScope.launch {
                                                        isVerified = true
                                                        delay(1000) // Show success animation for a second

                                                        if (!verifyResponse.user.isNullOrEmpty()) {
                                                            // User exists: call login route to update tokens.
                                                            ApiService.authService.loginUser(
                                                                LoginRequest(email, otp)
                                                            ).enqueue(object : Callback<LoginResponse> {
                                                                override fun onResponse(
                                                                    call: Call<LoginResponse>,
                                                                    response: Response<LoginResponse>
                                                                ) {
                                                                    isVerifying = false
                                                                    if (response.isSuccessful && response.body() != null) {
                                                                        val loginResponse = response.body()!!
                                                                        if (loginResponse.success) {
                                                                            // Store tokens persistently
                                                                            SessionManager.saveTokens(
                                                                                context,
                                                                                "Bearer " + (loginResponse.accessToken ?: ""),
                                                                                "Bearer " + (loginResponse.refreshToken ?: "")
                                                                            )

                                                                            // Navigate with animation
                                                                            navController.navigate("main") {
                                                                                popUpTo("login") { inclusive = true }
                                                                            }
                                                                        } else {
                                                                            Toast.makeText(
                                                                                context,
                                                                                loginResponse.message ?: "Login failed",
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
                                                                    isVerifying = false
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Login failure: ${t.localizedMessage}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            })
                                                        } else {
                                                            // User doesn't exist: navigate to registration.
                                                            isVerifying = false
                                                            navController.navigate("register/$email")
                                                        }
                                                    }
                                                } else {
                                                    isVerifying = false
                                                    Toast.makeText(
                                                        context,
                                                        verifyResponse.message ?: "Verification failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                isVerifying = false
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
                                            isVerifying = false
                                            Toast.makeText(
                                                context,
                                                "Verification Failure: ${t.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                                } else {
                                    Toast.makeText(context, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isVerifying && !isVerified && otp.length == 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .scale(buttonScale),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                disabledContainerColor = AppColors.Primary.copy(alpha = 0.6f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Success animation with improved color
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(successScale.value)
                                )

                                // Loading or text
                                this@Button.AnimatedVisibility(
                                    visible = !isVerified,
                                    exit = fadeOut()
                                ) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Verify OTP",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider with text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "Didn't receive the code?",
                                fontSize = 14.sp,
                                color = AppColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resend button with timer
                        Button(
                            onClick = {
                                // Hide keyboard when button is clicked
                                keyboardController?.hide()

                                isTimerRunning = true
                                timerProgress = 1f
                                countdownTimer?.cancel()
                                countdownTimer?.start()

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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.SurfaceLight,
                                disabledContainerColor = Color.White.copy(alpha = 0.06f)
                            )
                        ) {
                            if (isTimerRunning) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Resend in ",
                                        fontSize = 16.sp,
                                        color = AppColors.TextSecondary
                                    )
                                    Text(
                                        text = timerText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Text(
                                    text = "Resend OTP",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Timer progress indicator with improved color
                        AnimatedVisibility(visible = isTimerRunning) {
                            LinearProgressIndicator(
                                progress = { timerProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = AppColors.Secondary,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                // Add extra padding at the bottom to ensure content is accessible when keyboard is shown
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

fun Modifier.border(width: Dp, color: Color, shape: RoundedCornerShape) = this
    .clip(shape)
    .then(
        Modifier.drawBehind {
            drawRoundRect(
                color = color,
                style = Stroke(width.toPx())
            )
        }
    )