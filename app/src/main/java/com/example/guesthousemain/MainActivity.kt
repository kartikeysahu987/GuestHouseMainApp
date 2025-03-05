package com.example.guesthousemain

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.guesthousemain.ui.LoginScreen
import com.example.guesthousemain.ui.OtpVerificationScreen
import com.example.guesthousemain.ui.theme.GuestHouseMainTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Send OTP request on startup with a hardcoded email
//        sendOtpRequest(this, "kartikeysahu987@gmail.com")

        setContent {
            GuestHouseMainTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController)
        }

        composable(
            "otpVerification/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(navController, email)
        }

        // NEW: Register screen route, also passing the email
        composable(
            "register/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RegisterScreen(navController, email)
        }
    }
}


/**
 * Sends an HTTPS POST request to the OTP endpoint with a hardcoded JSON body.
 * The response or any error is displayed in a Toast.
 */
fun sendOtpRequest(context: android.content.Context, email: String) {
    val client = OkHttpClient()
    val url = "https://guest-house-backend-iitrpr-production.up.railway.app/auth/otp" // Your OTP endpoint
    // Hardcoded JSON request body with the provided email
    val jsonBody = "{\"email\": \"$email\"}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = jsonBody.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "OTP Request failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseText = response.body?.string() ?: "No response"
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Response: $responseText",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    })
}
