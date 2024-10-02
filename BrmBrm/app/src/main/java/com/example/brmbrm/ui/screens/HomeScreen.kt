package com.example.brmbrm.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

@Composable
fun HomeScreen(
    onCreateDriveClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMyDriveClick: () -> Unit,
    onMapClick: (Double, Double) -> Unit,
    onLogoutClick: () -> Unit
) {

    val firebaseStorage = FirebaseStorage.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userImageUrl by remember { mutableStateOf<String?>(null) }
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var locationGranted by remember { mutableStateOf(false) }
    var lastKnownLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Permission launcher to request location permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        try {
            // Fetching background image
            backgroundImageUrl = firebaseStorage.reference.child("background.jpg").downloadUrl.await().toString()
            // Fetching user profile image
            userImageUrl =  firebaseStorage.reference.child("profile_pictures/${currentUser?.uid}.jpg").downloadUrl.await().toString()
        } catch (e: Exception) {
            // Handle errors (image not found, etc.)
        }
    }

    // Function to get the device's location
    fun requestLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationGranted = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastKnownLocation = Pair(location.latitude, location.longitude)
                    onMapClick(location.latitude, location.longitude) // Navigate with actual coordinates
                } else {
                    Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Setting background image
        backgroundImageUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize() // Fill the whole screen with background image
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { onCreateDriveClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )) {
                Text(text = "Create Drive")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { requestLocation() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )) {
                Text(text = "Go to Map")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { onMyDriveClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )) {
                Text(text = "My Drive")
            }
        }

        // Profile Button with User Image
        Button(
            onClick = { onProfileClick() },
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            userImageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onLogoutClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red, // Logout dugme u crvenoj boji
                    contentColor = Color.White
                )
            ) {
                Text(text = "Logout")
            }
        }
    }
}
