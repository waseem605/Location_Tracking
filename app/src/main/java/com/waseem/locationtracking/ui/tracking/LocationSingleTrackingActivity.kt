package com.waseem.locationtracking.ui.tracking

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.waseem.locationtracking.R
import com.waseem.locationtracking.data.UserLocation
import com.waseem.locationtracking.databinding.ActivityLocationSingleTrackingBinding
import com.waseem.locationtracking.databinding.ActivityLocationTrackingBinding
import com.waseem.locationtracking.utils.FirebaseHelper
import com.waseem.locationtracking.utils.helper.LocationHelper
import com.waseem.locationtracking.utils.helper.LocationTracker
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import com.waseem.locationtracking.utils.helper.addCustomMarker
import com.waseem.locationtracking.utils.helper.addCustomTrackingMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationSingleTrackingActivity : AppCompatActivity() {

    private val TAG = "LocationTrackingActivity"

    private lateinit var binding: ActivityLocationSingleTrackingBinding
    private var orderMap: GoogleMap? = null
    private val preferenceHelper: PreferenceHelper by inject()
    private val locationTracker: LocationTracker by inject()
    private lateinit var mFirebaseHelper: FirebaseHelper
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityLocationSingleTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mFirebaseHelper = FirebaseHelper()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getStringExtra("UserID").toString()
        val userLocationRef: DatabaseReference = database.getReference("userLocation").child(id)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(callbackMapReady)



        userLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if data exists
                if (snapshot.exists()) {
                    // Parse data to UserLocation object
                    val userLocation = snapshot.getValue(UserLocation::class.java)
                    Log.d("FirebaseHelperSingle", "No data found for userId: $userLocation")
                    userLocation?.let { model ->
                Log.d(TAG, "onCreate: model $model")
                orderMap?.let { map ->
                    addCustomTrackingMarker(
                        map, LatLng(model.lat, model.lng), ContextCompat.getDrawable(
                            this@LocationSingleTrackingActivity,
                            R.drawable.tracking_marker
                        ), model.email
                    )
                }
            }
                } else {
                    // No data found
                    Log.d("FirebaseHelperSingle", "No data found for userId: $id")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read data
                Log.e("FirebaseHelperSingle", "Failed to fetch data", error.toException())
            }
        })
    }

    private val callbackMapReady = object : OnMapReadyCallback {
        override fun onMapReady(p0: GoogleMap) {
            orderMap = p0
            Log.d(TAG, "onCreate: model ready")
            // Use a custom info window adapter to handle multiple lines of text in the
            // info window contents.
            orderMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this@LocationSingleTrackingActivity,
                    R.raw.map_in_light
                )
            )
            orderMap?.uiSettings?.isZoomGesturesEnabled = true
            p0.setPadding(0, 18, 0, 0)
//        orderMap?.isTrafficEnabled = sharedPreferencesHelper.getBool(SHOW_TRAFFIC)
            // Turn on the My Location layer and the related control on the map.
            updateLocationUI()
            locationTracker.startLocationTracking(locationCallback = {
                it?.let { location ->
                    // Use the location
                    CoroutineScope(Dispatchers.Main).launch {
                        orderMap?.let { map ->
                            Log.d(TAG, "onLocationUpdate:    $location")
                            LocationHelper.currentLocationMarker(
                                map,
                                "currentLocation",
                                location,
                                ContextCompat.getDrawable(
                                    this@LocationSingleTrackingActivity,
                                    R.drawable.current_marker
                                )
                            )
                        }
                    }
                } ?: run {
                    // Handle location not available
                }
            })
        }

    }

    private fun updateLocationUI() {
        if (orderMap == null) {
            return
        }
        try {
//            if (locationPermissionGranted) {
            orderMap?.isMyLocationEnabled = true
            orderMap?.uiSettings?.isMyLocationButtonEnabled = true
//            } else {
//                orderMap?.isMyLocationEnabled = false
//                orderMap?.uiSettings?.isMyLocationButtonEnabled = false
//            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}