package com.waseem.locationtracking.ui.dashboard

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.waseem.locationtracking.R
import com.waseem.locationtracking.databinding.ActivityMainBinding
import com.waseem.locationtracking.utils.extension.logoutUser
import com.waseem.locationtracking.utils.helper.LocationHelper.currentLocationMarker
import com.waseem.locationtracking.utils.helper.LocationTracker
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private val TAG = "MainActivityTAG"
    private var orderMap: GoogleMap? = null
    private val preferenceHelper: PreferenceHelper by inject()
    private val locationTracker: LocationTracker by inject()
    private var dropOffMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        startActivity(Intent(this,LoginActivity::class.java))

//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val  mNavController = navHostFragment.navController

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(callbackMapReady)

        binding.logoutBtn.setOnClickListener {
            logoutUser()
        }

    }

    private val callbackMapReady = object :OnMapReadyCallback{
        override fun onMapReady(p0: GoogleMap) {
            orderMap = p0
            // Use a custom info window adapter to handle multiple lines of text in the
            // info window contents.
            orderMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MainActivity, R.raw.map_in_light))
            orderMap?.uiSettings?.isZoomGesturesEnabled = true
            p0.setPadding(0, 18, 0, 0)
//        orderMap?.isTrafficEnabled = sharedPreferencesHelper.getBool(SHOW_TRAFFIC)
            // Turn on the My Location layer and the related control on the map.
            updateLocationUI()
            locationTracker.startLocationTracking(locationCallback = {
                it?.let {location->
                    // Use the location
                    CoroutineScope(Dispatchers.Main).launch {
                        orderMap?.let { map ->

                            if (dropOffMarker!=null){
                                dropOffMarker?.remove()
                            }
                            Log.d(TAG, "onLocationUpdate:    $location")
                            currentLocationMarker(
                                map,
                                "currentLocation",
                                location,
                                ContextCompat.getDrawable(
                                    this@MainActivity,
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

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopLocationTracking()
    }
}