package com.example.brmbrm.ui.screens

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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


    val firestore = FirebaseFirestore.getInstance()
    var driveLocations by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()


    var radius by remember { mutableStateOf(10f) } // Default to 10 km


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val drivesSnapshot = firestore.collection("drives").whereEqualTo("status", 1).get().await()
                driveLocations = drivesSnapshot.documents.mapNotNull { it.data }
            } catch (e: Exception) {

            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Text("Search radius: ${radius.toInt()} km", modifier = Modifier.padding(16.dp))
        Slider(
            value = radius,
            onValueChange = { radius = it },
            valueRange = 1f..100f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )


        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {

            Marker(
                state = MarkerState(position = currentLocation),
                title = "Current Location",
                snippet = "You are here"
            )


            for (drive in driveLocations) {
                val driveLatLng = LatLng(drive["latitude"] as Double, drive["longitude"] as Double)


                val results = FloatArray(1)
                Location.distanceBetween(
                    latitude, longitude,
                    driveLatLng.latitude, driveLatLng.longitude,
                    results
                )
                val distanceInKm = results[0] / 1000


                if (distanceInKm <= radius) {
                    Marker(
                        state = MarkerState(position = driveLatLng),
                        title = drive["carModel"] as String,
                        snippet = "Destination: ${drive["destination"]}",
                        onClick = {
                            navController.navigate(
                                "drive_details/${drive["username"]}/${drive["carModel"]}/${drive["destination"]}/${drive["departureTime"]}/${drive["availableSeats"]}/${drive["price"]}"
                            )
                            true
                        }
                    )
                }
            }
        }
    }
}
