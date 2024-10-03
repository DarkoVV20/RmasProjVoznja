package com.example.brmbrm.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.brmbrm.MainActivity
import com.example.brmbrm.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.util.*

class LocationService : Service() {
    val currentUser = FirebaseAuth.getInstance().currentUser

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    sendLocationToServer(location)

                    checkForNearbyDrives(location)
                }
            }
        }

        startForegroundServiceWithNotification()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking Location")
            .setContentText("Your location is being tracked.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        startLocationUpdates()
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "location_service_channel"
            val channelName = "Location Service"
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)

            return channelId
        }
        return ""
    }

    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            val locationRequest = LocationRequest.create().apply {
                interval = 15000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, null
            )
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun sendLocationToServer(location: Location) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "unknown_user"

        val locationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to Date()
        )

        firestore.collection("user_locations")
            .document(userId)
            .set(locationData)
            .addOnSuccessListener {
                println("Location successfully sent to server for userId: $userId")
            }
            .addOnFailureListener { exception ->
                println("Error sending location to server: ${exception.message}")
            }
    }


    private fun checkForNearbyDrives(location: Location) {
        val userLocation = Pair(location.latitude, location.longitude)

        firestore.collection("drives")
            .whereEqualTo("status", 1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val driveLocation = Pair(
                        document.getDouble("latitude") ?: 0.0,
                        document.getDouble("longitude") ?: 0.0
                    )


                    val distance = calculateDistance(userLocation, driveLocation)

                    if (distance < 1000) {
                        showNotification("Nearby Drive", "A drive is happening near you!")
                        break
                    }
                }
            }
    }


    private fun calculateDistance(loc1: Pair<Double, Double>, loc2: Pair<Double, Double>): Float {
        val result = FloatArray(1)
        Location.distanceBetween(loc1.first, loc1.second, loc2.first, loc2.second, result)
        return result[0]
    }


    private fun showNotification(title: String, message: String) {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
