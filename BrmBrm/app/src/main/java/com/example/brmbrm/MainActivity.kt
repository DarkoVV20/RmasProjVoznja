package com.example.brmbrm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brmbrm.services.LocationService
import com.example.brmbrm.ui.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {


    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        FirebaseApp.initializeApp(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (!granted) {
                        Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                if (!granted) {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                } else {
                    startLocationService()
                }
            }


        requestPermissions()


        setContent {
            MyApp()
        }
    }


    private fun requestPermissions() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startService(intent)
    }
}


@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home_screen") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onBackToLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("leaderboard") {
            LeaderBoardScreen(navController)
        }
        composable("home_screen") {
            HomeScreen(
                onProfileClick = {
                    navController.navigate("profile")
                },
                onCreateDriveClick = {
                    navController.navigate("create_drive")
                },
                onMyDriveClick = {
                    navController.navigate("my_drive")
                },
                onMapClick = { latitude, longitude ->
                    navController.navigate("map/$latitude/$longitude")
                },
                onLeaderboardClick = {
                    navController.navigate("leaderboard")
                },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("home_screen") { inclusive = true }
                    }
                }
            )
        }
        composable("create_drive") {
            CreateDriveScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("map/{latitude}/{longitude}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            MapScreen(navController, latitude, longitude)
        }
        composable("my_drive") {
            MyDriveScreen(navController)
        }
        composable("route_screen") {
            ShowRouteScreen(navController)
        }
        composable("drive_details/{username}/{carModel}/{destination}/{departureTime}/{availableSeats}/{price}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Unknown"
            val carModel = backStackEntry.arguments?.getString("carModel") ?: "Unknown"
            val destination = backStackEntry.arguments?.getString("destination") ?: "Unknown"
            val departureTime = backStackEntry.arguments?.getString("departureTime") ?: "Unknown"
            val availableSeats = backStackEntry.arguments?.getString("availableSeats") ?: "0"
            val price = backStackEntry.arguments?.getString("price") ?: "Unknown"

            DriveDetailsScreen(
                navController = navController,
                username = username,
                carModel = carModel,
                destination = destination,
                departureTime = departureTime,
                availableSeats = availableSeats,
                price = price
            )
        }
    }
}
