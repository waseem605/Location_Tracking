package com.waseem.locationtracking.utils.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.waseem.locationtracking.R
import com.waseem.locationtracking.ui.PermissionsActivity
import com.waseem.locationtracking.utils.FirebaseHelper
import com.waseem.locationtracking.utils.extension.isInternetAvailable
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.USER_EMAIL
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.USER_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class BGLocationService : Service() {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private val TAG = "BGLocationService"
    private val preferenceHelper: PreferenceHelper by inject()

    private val CHANNEL_ID = "loadDataServiceChannel"
    private val NOTIFICATION_ID = 109

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val locationInterval = 5000L
    private val locationFastestInterval = 3000L
    private val locationMaxWaitTime = 5000L
    private val locationDistanceMeters = 50F
    private lateinit var mFirebaseHelper: FirebaseHelper

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        println("BGLocationService      BGLocationService  onCreate")
        sendNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mFirebaseHelper = FirebaseHelper()
        autoUpdateLocation()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        return START_STICKY
    }

    private fun sendNotification() {
        val mainIntent = Intent(applicationContext, PermissionsActivity::class.java)
        val pReceiverIntent = PendingIntent.getActivity(
            applicationContext, 1, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background).setColor(Color.WHITE)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setOngoing(true)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(resources.getString(R.string.location_is_tracking))
            .setContentIntent(pReceiverIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // or No
        val notification = builder.build()
        mNotificationManager?.notify(NOTIFICATION_ID, notification)
        val channel = NotificationChannel(CHANNEL_ID, "title", NotificationManager.IMPORTANCE_HIGH)
        mNotificationManager?.createNotificationChannel(channel)
//        try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
//        } catch (e: RuntimeException) {
//            e.printStackTrace()
//        }
    }
    private var mNotificationManager:NotificationManager?=null

//    override fun onDestroy() {
//        if (!preferenceHelper.getLoginStatus()){
//            removeNotification()
//        }else{
//            val broadcastIntent = Intent()
//            broadcastIntent.action = "restartservice"
//            broadcastIntent.setClass(this, RestartBackgroundService::class.java)
//            this.sendBroadcast(broadcastIntent)
//        }
//        super.onDestroy()
//    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun removeNotification() {
        runCatching {
            Log.d("MusicPlayerService", "mNotificationManager $NOTIFICATION_ID")
            if (NOTIFICATION_ID != 0) {
                mNotificationManager?.cancel(NOTIFICATION_ID)
                mNotificationManager?.cancelAll()
            }
        }
    }


    private fun setLocationToServer(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isInternetAvailable() && preferenceHelper.getLoginStatus() ){
                mFirebaseHelper.saveUserLocation(preferenceHelper.getString(USER_ID),location.latitude,location.longitude,preferenceHelper.getString(USER_EMAIL))
                //                Unauthorized
            }else{
//                LogUtils.d(TAG, "setLocationToServer: Unauthorized")
            }
        }
    }


    /// New Non-Deprecated Approach
    private fun autoUpdateLocation() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationFastestInterval)
                .setMaxUpdateDelayMillis(locationMaxWaitTime)
//                .setMinUpdateDistanceMeters(locationDistanceMeters)
                .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        super.onLocationResult(p0)
                        if (p0.lastLocation != null) {
                            val latitude = p0.lastLocation!!.latitude
                            val longitude = p0.lastLocation!!.longitude
//                            sharedPreferencesHelper.saveBgLocation(latitude,longitude)
                            val currentLatLng = LatLng(latitude, longitude)
                            Log.e(TAG, " onLocationChanged  $currentLatLng")
                            println("BGLocationService      BGLocationService  onLocationChanged  $currentLatLng")
                            setLocationToServer(p0.lastLocation!!)
                        }
                    }
                },
                null
            )
    }
}