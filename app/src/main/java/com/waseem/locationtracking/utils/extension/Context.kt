package com.waseem.locationtracking.utils.extension

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.waseem.locationtracking.ui.login.LoginActivity
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import com.waseem.locationtracking.utils.services.BGLocationService

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.showToastMsg(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (_: java.lang.Exception) {
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

fun Context.showSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}


fun isAppRunning(context: Context, packageName: String): Boolean {
    val activityManager =
        context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
    val probInfo = activityManager.runningAppProcesses
    if (probInfo != null) {
        for (processInfo in probInfo) {
            if (processInfo.processName == packageName) {
                return true
            }
        }
    }
    return false
}



fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
    val serviceName = serviceClass.name
    for (serviceInfo in runningServices) {
        if (serviceName == serviceInfo.service.className) {
            return true
        }
    }
    return false
}

//fun isServiceRunning(context: Context, serviceClass: BGLocationService): Boolean {
//    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//    val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
//    val serviceName = serviceClass.javaClass.name
//    for (serviceInfo in runningServices) {
//        if (serviceName == serviceInfo.service.className) {
//            return true
//        }
//    }
//    return false
//}

//fun toStopSoundVibrationService(context: Context) {
//    val myService = Intent(context, MusicPlayerService::class.java)
//    myService.putExtra("INTENT_SERVICE_VALUE","STOP_SOUND")
//    context.stopService(myService)
//}

fun Context.toStopBGLocationService() {
    if (isServiceRunning(this,BGLocationService::class.java)) {
        val myService = Intent(this, BGLocationService::class.java)
        stopService(myService)
    }
}

fun Context.startLocationService() {
   val  mServiceIntent = Intent(this, BGLocationService::class.java)
    if (!isServiceRunning(this, BGLocationService::class.java)) {
        this.startForegroundService(mServiceIntent)
    }
}

fun Activity.logoutUser(){
    FirebaseAuth.getInstance().signOut()
    PreferenceHelper(this).clearPreference()
    val intent = Intent(this, LoginActivity::class.java)
    intent.putExtra("finish", true)
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
    finish()
}
