package com.waseem.locationtracking.utils.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission

object PermissionHelper {
    fun checkBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            checkLocationPermissionGetStarted(context)
        }
    }

    private fun checkLocationPermissionGetStarted(context: Context): Boolean {
        return (ContextCompat.checkSelfPermission(
            context.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun checkAllPermissions(context: Context): Boolean {
        var notificationPerm = false
        var overLayPerm = false
        var bgLocationPerm = false
        notificationPerm = if (Build.VERSION.SDK_INT >= 33) {
            (ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }

        overLayPerm = Settings.canDrawOverlays(context)

        bgLocationPerm = checkBackgroundLocationPermission(context)
        return notificationPerm  && overLayPerm && bgLocationPerm
    }
}