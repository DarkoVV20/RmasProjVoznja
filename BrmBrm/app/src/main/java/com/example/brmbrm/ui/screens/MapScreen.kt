package com.example.brmbrm.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController, latitude: Double, longitude: Double) {
    val currentLocation = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    // Initialize Firestore and store drive data
    val firestore = FirebaseFirestore.getInstance()
    var driveLocations by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch drives from Firestore
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val drivesSnapshot = firestore.collection("drives").whereEqualTo("status", 1).get().await()
                driveLocations = drivesSnapshot.documents.mapNotNull { it.data }
            } catch (e: Exception) {
                // Handle error (e.g., show a message)
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Add a marker for the user's current location
        Marker(
            state = MarkerState(position = currentLocation),
            title = "Current Location",
            snippet = "You are here"
        )

        // Add markers for each drive retrieved from Firestore
        for (drive in driveLocations) {
            val driveLatLng = LatLng(drive["latitude"] as Double, drive["longitude"] as Double)
            Marker(
                state = MarkerState(position = driveLatLng),
                title = drive["carModel"] as String,
                snippet = "Destination: ${drive["destination"]}",
                onClick = {
                    navController.navigate(
                        "drive_details/${drive["username"]}/${drive["carModel"]}/${drive["destination"]}/${drive["departureTime"]}/${drive["availableSeats"]}/${drive["price"]}"
                    )
                    true // Return true to indicate the click was handled
                }
            )

        }
    }
}
