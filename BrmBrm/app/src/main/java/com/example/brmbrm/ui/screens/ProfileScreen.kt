package com.example.brmbrm.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val firebaseStorage = FirebaseStorage.getInstance()
    val currentUser = firebaseAuth.currentUser

    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf(TextFieldValue("")) }
    var lastName by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var yearOfBirth by remember { mutableStateOf(TextFieldValue("")) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var backgroundImageUrl by remember { mutableStateOf<String?>(null) } // Background image URL

    val defaultProfileImageUrl = "default.png"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profileImageUrl = it.toString()
        }
    }


    // Učitavanje podataka korisnika i pozadinske slike
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            Toast.makeText(context, "User not found. Please log in.", Toast.LENGTH_LONG).show()
            navController.navigate("login")
        } else {
            val userId = currentUser.uid // Declare userId here
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    firstName = TextFieldValue(userDoc.getString("firstName") ?: "")
                    lastName = TextFieldValue(userDoc.getString("lastName") ?: "")
                    email = TextFieldValue(userDoc.getString("email") ?: "")
                    username = TextFieldValue(userDoc.getString("username") ?: "")
                    phoneNumber = TextFieldValue(userDoc.getString("phoneNumber") ?: "")
                    yearOfBirth = TextFieldValue(userDoc.getString("yearOfBirth") ?: "")

                    // Load profile image
                    val profileImageRef = firebaseStorage.reference.child("profile_pictures/$userId.jpg")
                    profileImageUrl = try {
                        profileImageRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        defaultProfileImageUrl // Use default if not found
                    }

                    // Load background image
                    val backgroundRef = firebaseStorage.reference.child("background.jpg")
                    backgroundImageUrl = try {
                        backgroundRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        // Handle background image not found error if needed
                        null
                    }
                } else {
                    Toast.makeText(context, "User data not found in Firestore.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    val scrollState = rememberScrollState()

    // UI prikaz
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
                .background(Color(0xFF000000), shape = RoundedCornerShape(8.dp))
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp) // Padding for the title
            )
            profileImageUrl?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TextField za email
            OutlinedTextField(
                value = email,
                onValueChange = { if (isEditing) email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    containerColor = Color.Transparent
                )
            )



            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { if (isEditing) username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { if (isEditing) firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { if (isEditing) lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (isEditing) phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = yearOfBirth,
                onValueChange = { if (isEditing) yearOfBirth = it },
                label = { Text("Year of Birth") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White,
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        val userId = firebaseAuth.currentUser?.uid
                        if (userId != null) {
                            val updatedUser = mapOf(
                                "firstName" to firstName.text,
                                "lastName" to lastName.text,
                                "username" to username.text,
                                "phoneNumber" to phoneNumber.text,
                                "yearOfBirth" to yearOfBirth.text
                            )

                            firestore.collection("users")
                                .document(userId)
                                .update(updatedUser)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_LONG).show()
                                    isEditing = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        isEditing = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text(text = if (isEditing) "Save Changes" else "Edit Profile")
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    //ProfileScreen()
}
