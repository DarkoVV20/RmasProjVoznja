package com.example.brmbrm.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDriveScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    var driveData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var bookedUsernames by remember { mutableStateOf<List<String>>(emptyList()) }
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        val backgroundRef = FirebaseStorage.getInstance().reference.child("background.jpg")
        backgroundImageUrl = try {
            backgroundRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }


    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val userSnapshot = firestore.collection("users").document(currentUser.uid).get().await()
            username = userSnapshot.getString("username")
            Log.d("MyDriveScreen", "Fetched username: $username")


            val driveSnapshot = firestore.collection("drives")
                .whereEqualTo("username", username)
                .whereEqualTo("status", 1) //
                .get()
                .await()

            Log.d("MyDriveScreen", "Drive Snapshot: ${driveSnapshot.documents}")

            if (!driveSnapshot.isEmpty) {
                driveData = driveSnapshot.documents[0].data


                val bookedUsersIds = driveData?.get("bookedUsers") as? List<String> ?: emptyList()
                Log.d("MyDriveScreen", "Booked user IDs: $bookedUsersIds")

                bookedUsernames = bookedUsersIds.mapNotNull { userId ->
                    val userSnapshot = firestore.collection("users").document(userId).get().await()
                    Log.d("MyDriveScreen", "Fetched username for userId $userId: ${userSnapshot.getString("username")}")
                    userSnapshot.getString("username")
                }
            } else {
                Log.d("MyDriveScreen", "No drive found for the current user.")
                Toast.makeText(context, "No drive found for you.", Toast.LENGTH_LONG).show()
            }
        }
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

        if (driveData != null) {
            val carModel = driveData?.get("carModel").toString()
            val destination = driveData?.get("destination").toString()
            val departureTime = driveData?.get("departureTime").toString()
            val availableSeats = driveData?.get("availableSeats").toString()
            val price = driveData?.get("price").toString()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color(0xFF000000).copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "My Drive",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                Text(text = "Car Model: $carModel", color = Color.White)
                Text(text = "Destination: $destination", color = Color.White)
                Text(text = "Departure Time: $departureTime", color = Color.White)
                Text(text = "Available Seats: $availableSeats", color = Color.White)
                Text(text = "Price: $price", color = Color.White)


                Text(text = "Booked Users:", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                bookedUsernames.forEach { username ->
                    Text(text = username, color = Color.White)
                }
                Button(
                    onClick = {

                        navController.navigate("route_screen")
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Show Route")
                }
                Button(
                    onClick = {
                        if (driveData != null) {
                            val driveRef = firestore.collection("drives")
                                .whereEqualTo("username", username)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val driveDocument = querySnapshot.documents[0].reference
                                        Log.d("MyDriveScreen", "Drive found for username: $username")


                                        driveDocument.delete().addOnSuccessListener {

                                            Log.d("MyDriveScreen", "Drive successfully deleted")


                                            val userPoints = firestore.collection("users").document(currentUser!!.uid)
                                            userPoints.get().addOnSuccessListener { userDoc ->
                                                val currentPoints = userDoc.getLong("points") ?: 0
                                                userPoints.update("points", currentPoints + 100)
                                            }

                                            val bookedUsersIds = driveData?.get("bookedUsers") as? List<String> ?: emptyList()
                                            bookedUsersIds.forEach { userId ->
                                                firestore.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                                                    val currentPoints = userDoc.getLong("points") ?: 0
                                                    firestore.collection("users").document(userId).update("points", currentPoints + 50)
                                                }
                                            }

                                            Toast.makeText(context, "Drive ended and deleted successfully!", Toast.LENGTH_LONG).show()
                                            navController.popBackStack()

                                        }.addOnFailureListener { e ->
                                            Toast.makeText(context, "Error deleting drive: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "No drive found for the current user.", Toast.LENGTH_LONG).show()
                                    }
                                }.addOnFailureListener { e ->
                                    Toast.makeText(context, "Error fetching drive: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "End Drive")
                }

            }
        } else {
            Text(text = "Loading your drive details...", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}