package com.example.guesthousemain

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.guesthousemain.ui.ReservationFormScreen
//import com.example.guesthousemain.ui.ReservationFormScreen
import com.example.guesthousemain.ui.screens.HomeScreen
//import com.example.guesthousemain.ui.screens.ReservationFormScreen
import com.example.guesthousemain.ui.theme.GuestHouseMainTheme
import com.example.guesthousemain.util.SessionManager
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeManager = ThemeManager(applicationContext)
        enableEdgeToEdge()

        // Load tokens from persistent storage into SessionManager.
        SessionManager.loadTokens(this)

        // Determine the start destination.
        val startDestination = if (SessionManager.accessToken.isNotEmpty() && SessionManager.refreshToken.isNotEmpty())
            "main" else "login"

        setContent {
            val isDarkTheme by themeManager.isDarkThemeFlow.collectAsState(initial = false)
            val notificationViewModel: NotificationViewModel = viewModel()

            notificationViewModel.initialize(
                access = SessionManager.accessToken,
                refresh = SessionManager.refreshToken,
                dark = isDarkTheme
            )
            notificationViewModel.fetchNotifications()
            notificationViewModel.fetchUnreadCount()
            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                GuestHouseMainTheme(darkTheme = isDarkTheme) {
                    Scaffold { innerPadding ->
                        Surface(modifier = Modifier.padding(innerPadding)) {
                            AppNavigation(startDestination,notificationViewModel)
                        }
                    }
                }
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(startDestination: String,notificationViewModel: NotificationViewModel) {
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
            MainPageScreen(globalNavController,notificationViewModel)
        }
        composable("reservation_form") {
            val context = LocalContext.current
            com.example.guesthousemain.ui.screens.ReservationFormScreen(
                accessToken = SessionManager.accessToken,
                refreshToken = SessionManager.refreshToken,
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Reservation submitted successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }


        composable("home"){
            HomeScreen(globalNavController)
        }
    }
}
