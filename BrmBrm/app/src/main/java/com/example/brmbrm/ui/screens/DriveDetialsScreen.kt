package com.example.brmbrm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveDetailsScreen(
    navController: NavController,
    username: String,
    carModel: String,
    destination: String,
    departureTime: String,
    availableSeats: String,
    price: String
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    var availableSeatsInt = availableSeats.toInt()

    // Background image URL
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }
    val firebaseStorage = com.google.firebase.storage.FirebaseStorage.getInstance()

    // Load background image
    LaunchedEffect(Unit) {
        val backgroundRef = firebaseStorage.reference.child("background.jpg")
        backgroundImageUrl = try {
            backgroundRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Set the background image
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
                .background(Color(0xFF000000).copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Drive Details",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display drive details
            Text(text = "Username: $username", color = Color.White)
            Text(text = "Car Model: $carModel", color = Color.White)
            Text(text = "Destination: $destination", color = Color.White)
            Text(text = "Departure Time: $departureTime", color = Color.White)
            Text(text = "Available Seats: $availableSeats", color = Color.White)
            Text(text = "Price: $price", color = Color.White)

            // Display button to book if seats are available
            if (availableSeatsInt > 0 && currentUser != null) {
                Button(
                    onClick = {
                        // Update Firestore to book the drive
                        firestore.collection("drives")
                            .whereEqualTo("username", username)
                            .whereEqualTo("carModel", carModel)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val driveDoc = querySnapshot.documents[0]
                                    val driveId = driveDoc.id
                                    val currentBookedUsers = driveDoc.get("bookedUsers") as? List<String> ?: emptyList()

                                    if (!currentBookedUsers.contains(currentUser.uid)) {
                                        firestore.collection("drives")
                                            .document(driveId)
                                            .update(
                                                "bookedUsers", currentBookedUsers + currentUser.uid,
                                                "availableSeats", (availableSeatsInt - 1).toString()
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Drive booked successfully!", Toast.LENGTH_LONG).show()
                                                navController.popBackStack()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Failed to book drive: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "You have already booked this drive.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text("Book Drive")
                }
            }
        }
    }
}

