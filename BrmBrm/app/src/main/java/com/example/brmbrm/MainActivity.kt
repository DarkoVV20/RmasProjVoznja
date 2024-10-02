package com.example.brmbrm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brmbrm.ui.screens.LoginScreen
import com.example.brmbrm.ui.screens.RegisterScreen
import com.example.brmbrm.ui.screens.HomeScreen
import com.example.brmbrm.ui.screens.ProfileScreen
import com.example.brmbrm.ui.screens.CreateDriveScreen
import com.example.brmbrm.ui.screens.MapScreen
import com.google.firebase.FirebaseApp
import com.example.brmbrm.ui.screens.DriveDetailsScreen
import com.example.brmbrm.ui.screens.MyDriveScreen
import com.example.brmbrm.ui.screens.ShowRouteScreen



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            setContent {

                MyApp()
            }
        }
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
                    navController.navigate("map/$latitude/$longitude") // Navigate using dynamic location
                },
                onLogoutClick = {
                    // Navigate back to login screen and remove home screen from back stack
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

