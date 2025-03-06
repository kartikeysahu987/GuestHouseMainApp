package com.example.guesthousemain

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import com.example.guesthousemain.ui.MainPageScreen
//import com.example.guesthousemain.ui.MainPageScreen
import com.example.guesthousemain.ui.OtpVerificationScreen
import com.example.guesthousemain.ui.RegisterScreen
import com.example.guesthousemain.ui.screens.HomeScreen
import com.example.guesthousemain.ui.screens.ReservationFormScreen
import com.example.guesthousemain.ui.theme.GuestHouseMainTheme
import com.example.guesthousemain.util.SessionManager

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load tokens from persistent storage into SessionManager.
        SessionManager.loadTokens(this)

        // Determine the start destination.
        val startDestination = if (SessionManager.accessToken.isNotEmpty() && SessionManager.refreshToken.isNotEmpty())
            "main" else "login"

        setContent {
            GuestHouseMainTheme {
                Scaffold { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(startDestination)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(startDestination: String) {
    val globalNavController = rememberNavController()
    NavHost(navController = globalNavController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(globalNavController)
        }
        composable(
            "otpVerification/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(globalNavController, email)
        }
        composable(
            "register/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RegisterScreen(globalNavController, email)
        }
        composable("main") {
            MainPageScreen(globalNavController)
        }
        composable("reservation_form"){
            ReservationFormScreen()

        }
        composable("home"){
            HomeScreen(globalNavController)
        }
    }
}
