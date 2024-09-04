package com.waseem.locationtracking.ui.tracking

import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.android.gms.maps.model.Marker
import com.waseem.locationtracking.R
import com.waseem.locationtracking.databinding.ActivityLocationTrackingBinding
import com.waseem.locationtracking.utils.CustomMapInfoWindow
import com.waseem.locationtracking.utils.FirebaseHelper
import com.waseem.locationtracking.utils.extension.toast
import com.waseem.locationtracking.utils.helper.LocationHelper
import com.waseem.locationtracking.utils.helper.LocationTracker
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import com.waseem.locationtracking.utils.helper.addCustomMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationTrackingActivity : AppCompatActivity() {

    private val TAG = "LocationTrackingActivity"

    private lateinit var binding: ActivityLocationTrackingBinding
    private var orderMap: GoogleMap? = null
    private val preferenceHelper: PreferenceHelper by inject()
    private val locationTracker: LocationTracker by inject()
    private lateinit var mFirebaseHelper: FirebaseHelper
    @SuppressLint("PotentialBehaviorOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityLocationTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mFirebaseHelper = FirebaseHelper()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(callbackMapReady)

        mFirebaseHelper.fetchAllUserLocations(callback = {
            it.forEach { model ->
                Log.d(TAG, "onCreate: model $model")
                orderMap?.let { map ->
                    Log.d(TAG, "onCreate: model ------ $model")
                    addCustomMarker(
                        map, LatLng(model.lat, model.lng), ContextCompat.getDrawable(
                            this@LocationTrackingActivity,
                            R.drawable.tracking_marker
                        ), model.userId,model.email
                    )
                }
            }
        })
        var id = "YoH25MCgRTd51sJvPx0UiC6aSRZ2"
        orderMap
        binding.singleuser.setOnClickListener { 
            if (id.isNotEmpty()){
                val mIntent = Intent(this@LocationTrackingActivity,LocationSingleTrackingActivity::class.java)
                mIntent.putExtra("UserID",id)
                startActivity(mIntent)
            }else{
                Log.d(TAG, "onCreate: null")
            }
        }

        
    }

    private val callbackMapReady = object : OnMapReadyCallback {
        override fun onMapReady(p0: GoogleMap) {
            p0.setOnMarkerClickListener { p0 ->

                orderMap?.setInfoWindowAdapter(
                    CustomMapInfoWindow(
                        this@LocationTrackingActivity,
                        p0.title.toString(), "p0.snippet.toString()"
                    )
                )

                false
            }



            orderMap = p0
            Log.d(TAG, "onCreate: model ready")
            // Use a custom info window adapter to handle multiple lines of text in the
            // info window contents.
            orderMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this@LocationTrackingActivity,
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
                                    this@LocationTrackingActivity,
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