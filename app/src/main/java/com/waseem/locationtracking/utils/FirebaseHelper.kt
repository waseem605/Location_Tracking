package com.waseem.locationtracking.utils

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.waseem.locationtracking.data.UserLocation

class FirebaseHelper {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userLocationRef: DatabaseReference = database.getReference("userLocation")
    private val adminLocationRef: DatabaseReference = database.getReference("adminLocation")

    fun saveUserLocation(userId: String, lat: Double, lng: Double, email: String) {
        val userLocation = UserLocation(userId, lat, lng, email)
       val dbReference =  if (email.contains("admin")){
           adminLocationRef
        }else{
           userLocationRef
       }

        // Save data to "userLocation/{userId}"
        dbReference.child(userId).setValue(userLocation)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Data saved successfully
                    Log.d("FirebaseHelper", "User location saved successfully.")
                } else {
                    // Failed to save data
                    task.exception?.let {
                        Log.e("FirebaseHelper", "Error saving user location", it)
                    }
                }
            }
    }


    fun fetchAllUserLocations(callback: (List<UserLocation>) -> Unit) {
        userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userLocationList = mutableListOf<UserLocation>()

                for (dataSnapshot in snapshot.children) {
                    val userLocation = dataSnapshot.getValue(UserLocation::class.java)
                    userLocation?.let { userLocationList.add(it) }
                }

                callback(userLocationList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to fetch data", error.toException())
                callback(emptyList())
            }
        })
    }
    fun listenForUserLocationUpdates(callback: (List<UserLocation>) -> Unit) {
        userLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userLocationList = mutableListOf<UserLocation>()

                for (dataSnapshot in snapshot.children) {
                    val userLocation = dataSnapshot.getValue(UserLocation::class.java)
                    userLocation?.let { userLocationList.add(it) }
                }

                callback(userLocationList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Failed to listen for updates", error.toException())
            }
        })
    }

    fun fetchUserLocation(userId: String, callback: (UserLocation?) -> Unit) {
        userLocationRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if data exists
                if (snapshot.exists()) {
                    // Parse data to UserLocation object
                    val userLocation = snapshot.getValue(UserLocation::class.java)
                    callback(userLocation)
                } else {
                    // No data found
                    Log.d("FirebaseHelper", "No data found for userId: $userId")
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read data
                Log.e("FirebaseHelper", "Failed to fetch data", error.toException())
                callback(null)
            }
        })
    }
}
