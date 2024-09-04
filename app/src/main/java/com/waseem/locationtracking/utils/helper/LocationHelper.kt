package com.waseem.locationtracking.utils.helper

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object LocationHelper {
    const val DEFAULT_ZOOM = 10f
    const val POLYLINE_WIDTH = 6f
    private var lastLocation: Location? = null
    private var currentMarker: Marker? = null


    fun isLocationEnabledOrNot(context: Context): Boolean {
        var locationManager: LocationManager? = null
        locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun requestBackgroundLocationPermission(
        activityContext: Activity,
        bgPermissionCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activityContext,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                bgPermissionCode
            )
        }
    }


    fun isMockLocationEnabled(context: Context, location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ALLOW_MOCK_LOCATION,
                0
            ) != 0
        }
//        return location != null && location.isFromMockProvider
    }


    suspend fun currentLocationMarker(
        mMap: GoogleMap,
        title: String,
        location: Location,
        drawable: Drawable?
    ) {
        withContext(Dispatchers.IO) {
            if (lastLocation == null) {
                lastLocation = location
            }
            val bitmap = if (drawable is BitmapDrawable) {
                Bitmap.createScaledBitmap(drawable.bitmap, 90, 90, true)
            } else {
                val tempBitmap = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(tempBitmap)
                drawable?.setBounds(0, 0, canvas.width, canvas.height)
                drawable?.draw(canvas)
                tempBitmap
            }
            withContext(Dispatchers.Main) {
                val latLng = LatLng(location.latitude, location.longitude)

                if (currentMarker == null) {
                    // If there is no marker, add one
                    val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .rotation(
                            getAdjustedBearing(
                                location.latitude,
                                location.longitude,
                                lastLocation?.latitude,
                                lastLocation?.longitude,
                                mMap.cameraPosition.bearing
                            )
                        )

                    currentMarker = mMap.addMarker(markerOptions)
                } else {
                    // If there is already a marker, animate it to the new position
                    animateMarker(currentMarker!!, latLng)
                    animateMarkerRotation(
                        currentMarker!!,
                        getAdjustedBearing(
                            location.latitude,
                            location.longitude,
                            lastLocation?.latitude,
                            lastLocation?.longitude,
                            mMap.cameraPosition.bearing
                        )
                    )
                }

                // Move and zoom the camera to the current location
                if (isZoomed) {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
                    mMap.animateCamera(cameraUpdate)
                    isZoomed = false
                }

            }
        }
        lastLocation = location
    }

    var isZoomed = true

    suspend fun addMarkerWithBitmap(
        mMap: GoogleMap,
        title: String,
        location: Location,
        drawable: Drawable?
    ) {
        withContext(Dispatchers.IO) {
            if (lastLocation == null) {
                lastLocation = location
            }
            val bitmap = if (drawable is BitmapDrawable) {
                Bitmap.createScaledBitmap(drawable.bitmap, 90, 90, true)
            } else {
                val tempBitmap = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(tempBitmap)
                drawable?.setBounds(0, 0, canvas.width, canvas.height)
                drawable?.draw(canvas)
                tempBitmap
            }
            withContext(Dispatchers.Main) {
                val markerOptions = MarkerOptions()
                    .title(title)
                    .position(LatLng(location.latitude, location.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .rotation(
                        getAdjustedBearing(
                            location.latitude,
                            location.longitude,
                            lastLocation?.latitude,
                            lastLocation?.longitude,
                            mMap.cameraPosition.bearing
                        )
                    )
                mMap.addMarker(markerOptions)
            }

        }
    }
}

fun addCustomMarker(
    mMap: GoogleMap,
    latLng: LatLng,
    drawable: Drawable?,
    id: String,
    email: String
) {

    val bitmap = if (drawable is BitmapDrawable) {
        Bitmap.createScaledBitmap(drawable.bitmap, 90, 90, true)
    } else {
        val tempBitmap = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(tempBitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        tempBitmap
    }
    val markerOptions = MarkerOptions()
            .position(latLng)
            .title(email)
//        .snippet(id)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        mMap.addMarker(markerOptions)
}

private var lastLocationTracking: LatLng? = null
private var trackingMarker: Marker? = null
fun addCustomTrackingMarker(
    mMap: GoogleMap,
    latLng: LatLng,
    drawable: Drawable?,
    title: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        if (lastLocationTracking == null) {
            lastLocationTracking = latLng
        }
        val bitmap = if (drawable is BitmapDrawable) {
            Bitmap.createScaledBitmap(drawable.bitmap, 90, 90, true)
        } else {
            val tempBitmap = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(tempBitmap)
            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)
            tempBitmap
        }
        withContext(Dispatchers.Main) {

            if (trackingMarker == null) {
                // If there is no marker, add one
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .rotation(
                        getAdjustedBearing(
                            latLng.latitude,
                            latLng.longitude,
                            lastLocationTracking?.latitude,
                            lastLocationTracking?.longitude,
                            mMap.cameraPosition.bearing
                        )
                    )

                trackingMarker = mMap.addMarker(markerOptions)
            } else {
                // If there is already a marker, animate it to the new position
                animateMarker(trackingMarker!!, latLng)
                animateMarkerRotation(
                    trackingMarker!!,
                    getAdjustedBearing(
                        latLng.latitude,
                        latLng.longitude,
                        lastLocationTracking?.latitude,
                        lastLocationTracking?.longitude,
                        mMap.cameraPosition.bearing
                    )
                )
            }

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16f)
            mMap.animateCamera(cameraUpdate)
        }
    }
}

private fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int
): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable?.setBounds(
        0,
        0,
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight
    )
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
private fun animateMarker(marker: Marker, toPosition: LatLng) {
    val fromPosition = marker.position
    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    valueAnimator.duration = 1000 // duration of the animation
    valueAnimator.interpolator = LinearInterpolator()

    valueAnimator.addUpdateListener { animation ->
        val v = animation.animatedFraction
        val newLatLng = LatLng(
            fromPosition.latitude * (1 - v) + toPosition.latitude * v,
            fromPosition.longitude * (1 - v) + toPosition.longitude * v
        )
        marker.position = newLatLng
    }

    valueAnimator.start()
}

private fun animateMarkerRotation(marker: Marker, toRotation: Float) {
    val startRotation = marker.rotation
    val shortestRotation = getShortestRotation(startRotation, toRotation)
    val valueAnimator = ValueAnimator.ofFloat(startRotation, shortestRotation)
    valueAnimator.duration = 1000 // duration of the animation
    valueAnimator.interpolator = LinearInterpolator()

    valueAnimator.addUpdateListener { animation ->
        marker.rotation = animation.animatedValue as Float
    }

    valueAnimator.start()
}

private fun getShortestRotation(fromRotation: Float, toRotation: Float): Float {
    val diff = (toRotation - fromRotation + 360) % 360
    return if (diff > 180) {
        fromRotation - (360 - diff)
    } else {
        fromRotation + diff
    }
}

private fun getAdjustedBearing(
    startLat: Double,
    startLng: Double,
    endLat: Double?,
    endLng: Double?,
    mapBearing: Float
): Float {
    val bearing = getBearing(startLat, startLng, endLat, endLng)
    return (bearing - mapBearing + 360) % 360
}

private fun getBearing(
    startLat: Double,
    startLng: Double,
    endLat: Double?,
    endLng: Double?
): Float {
    if (endLat == null || endLng == null) return 0f
    val startPoint = Location("startPoint").apply {
        latitude = startLat
        longitude = startLng
    }
    val endPoint = Location("endPoint").apply {
        latitude = endLat
        longitude = endLng
    }
    return startPoint.bearingTo(endPoint)
}