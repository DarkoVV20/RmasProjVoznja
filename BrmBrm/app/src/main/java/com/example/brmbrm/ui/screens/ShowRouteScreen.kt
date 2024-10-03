package com.example.brmbrm.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun ShowRouteScreen(navController: NavController) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(LocalContext.current)
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }


    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = LatLng(location.latitude, location.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            destination = latLng
        }
    ) {

        currentLocation?.let {
            Marker(state = MarkerState(position = it), title = "You are here")
        }


        destination?.let {
            Marker(state = MarkerState(position = it), title = "Destination")
        }


        currentLocation?.let { currentLatLng ->
            destination?.let { destLatLng ->
                Polyline(
                    points = listOf(currentLatLng, destLatLng),
                    color = Color.Blue,
                    width = 10f
                )
            }
        }
    }
}
