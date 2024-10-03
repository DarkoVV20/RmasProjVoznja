package com.example.brmbrm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.annotation.SuppressLint
import android.location.Location
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun CreateDriveScreen(navController: NavController) {
    var carModel by remember { mutableStateOf(TextFieldValue("")) }
    var departureTime by remember { mutableStateOf(TextFieldValue("")) }
    var destination by remember { mutableStateOf(TextFieldValue("")) }
    var availableSeats by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }

    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = firebaseAuth.currentUser
    val context = LocalContext.current
    val firebaseStorage = FirebaseStorage.getInstance()
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        try {
            val backgroundRef = firebaseStorage.reference.child("background.jpg")
            backgroundImageUrl = backgroundRef.downloadUrl.await().toString()
        } catch (e: Exception) {

        }
    }
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Please log in first.", Toast.LENGTH_LONG).show()
            navController.navigate("login")
        }
        return
    }


    Box(modifier = Modifier.fillMaxSize()) {
        backgroundImageUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color(0xFF000000).copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Drive",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = carModel,
                onValueChange = { carModel = it },
                label = { Text("Car Model") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = departureTime,
                onValueChange = { departureTime = it },
                label = { Text("Departure Time") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = availableSeats,
                onValueChange = { availableSeats = it },
                label = { Text("Available Seats") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val location: Location? = fusedLocationClient.lastLocation.await()

                        if (location == null) {
                            Toast.makeText(context, "Unable to get location", Toast.LENGTH_LONG).show()
                            return@launch
                        }




                        val userId = currentUser.uid
                        val userDoc = firestore.collection("users").document(userId).get().await()

                        if (userDoc.exists()) {
                            val username = userDoc.getString("username") ?: "Unknown User"


                            println("Username to be saved: $username")

                            // Kreiranje podatka za vožnju
                            val driveData = mapOf(
                                "username" to username,
                                "carModel" to carModel.text,
                                "departureTime" to departureTime.text,
                                "destination" to destination.text,
                                "availableSeats" to availableSeats.text,
                                "price" to price.text,
                                "latitude" to location.latitude,
                                "longitude" to location.longitude,
                                "status" to 1
                            )


                            firestore.collection("drives")
                                .add(driveData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Drive created successfully!", Toast.LENGTH_LONG).show()
                                    navController.navigate("map/${location.latitude}/${location.longitude}")
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error creating drive: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "User data not found in Firestore.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text("Create Drive")
            }


        }
    }
}
