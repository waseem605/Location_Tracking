package com.waseem.locationtracking.utils.helper

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.waseem.locationtracking.BuildConfig
import com.waseem.locationtracking.databinding.DialogMockLocationBinding
import com.waseem.locationtracking.ui.PermissionsActivity
import com.waseem.locationtracking.utils.helper.LocationHelper.isMockLocationEnabled

class LocationTracker(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationRequest: LocationRequest?=null
    private var locationCallbackInternal: LocationCallback? = null
    private val locationInterval = 5000L
    private val locationFastestInterval = 4000L
    private val locationMaxWaitTime = 5000L

    init {
        setupLocationRequest()
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(locationFastestInterval)
            .setMaxUpdateDelayMillis(locationMaxWaitTime)
            .build()
    }


    fun startLocationTracking(locationCallback: (Location?) -> Unit) {

        locationCallbackInternal = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let {
                    locationCallback(it)
                    if (!BuildConfig.DEBUG) {
                        if (isMockLocationEnabled(context, it)) {
                            mockLocationDialog(context)
                        } else {
                            PreferenceHelper(context).saveLocation(it.latitude, it.longitude)
                            locationCallback(it)
                        }
                    }else{
                        locationCallback(it)
                    }


                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                if (!p0.isLocationAvailable) {
                    locationCallback(null)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }else {
            if (locationRequest !=null && locationCallbackInternal!=null) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest!!,
                    locationCallbackInternal!!,
                    Looper.getMainLooper()
                )
            }
        }
    }

    fun stopLocationTracking() {
        locationCallbackInternal?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }



    private fun moveToPermissionScreen(context: Context) {
        val intent = Intent(context, PermissionsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private var dialog: Dialog? = null
    fun mockLocationDialog(context: Context) {
        if (dialog?.isShowing == true) {
            return
        }
        dialog = Dialog(context)
        val binding = DialogMockLocationBinding.inflate(LayoutInflater.from(context))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        dialog?.setContentView(binding.root)
        val width = (context.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        try {
            dialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.btnSetting.setOnClickListener {
            dialog?.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        }
    }

}
