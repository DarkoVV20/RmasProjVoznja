package com.example.brmbrm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await


@Composable
fun LeaderBoardScreen(navController: NavHostController) {
    val firebaseStorage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) }

    // Fetch top 10 users by points
    LaunchedEffect(Unit) {
        backgroundImageUrl = firebaseStorage.reference.child("background.jpg").downloadUrl.await().toString()
        firestore.collection("users")
            .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                users = result.documents.map { doc -> doc.data ?: emptyMap() }
            }
            .addOnFailureListener {
                // Handle failure (e.g., show a toast or log the error)
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Leaderboard", color = Color.White, fontSize = 30.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Display users in the leaderboard
            users.forEachIndexed { index, user ->
                val username = user["username"] as? String ?: "Unknown"
                val points = user["points"] as? Long ?: 0
                Text(
                    text = "${index + 1}. $username - $points points",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to return to HomeScreen
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Back to Home")
            }
        }
    }
}
