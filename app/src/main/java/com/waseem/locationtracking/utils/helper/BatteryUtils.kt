package com.waseem.locationtracking.utils.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.waseem.locationtracking.databinding.DialogOptimzeBatteryBinding
import com.waseem.locationtracking.databinding.DialogPermissionAlertBinding
import com.waseem.locationtracking.ui.PermissionsActivity
import com.waseem.locationtracking.utils.extension.setSafeOnClickListener
import com.waseem.locationtracking.utils.helper.PermissionHelper.checkBackgroundLocationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object BatteryUtils {

    fun checkBatteryOptimize(context: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            if (checkAllPermissions(context)){
                delay(1000)
                val value = isIgnoringBatteryOptimization(context)
                if (!value) {
                    withContext(Dispatchers.Main) {
                        batteryOptimizeDialog(context, callBack = {
                            openBatterySettingsIntent(context)
                        })
                    }
                }
            }else{
                val mIntent = Intent(context, PermissionsActivity::class.java)
                context.startActivity(mIntent)
                context.finish()
            }
        }
    }

     fun batteryOptimizeDialog(context: Context, callBack: ((Boolean) -> Unit)) {
        val dialog = Dialog(context)
        val binding = DialogOptimzeBatteryBinding.inflate(LayoutInflater.from(context))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        val width = (context.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.btnForward.setSafeOnClickListener {
            dialog.dismiss()
            callBack(true)
        }
    }

    private fun isIgnoringBatteryOptimization(context: Context): Boolean {
        var isIgnoring = false
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        return isIgnoring
    }


    private fun openBatterySettingsIntent(context: Context) {
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).also {
            it.data = Uri.parse("package:${context.packageName}")
            context.startActivity(it)
        }
    }


    fun permissionAlertDialog(context: Context, title:String,desc:String,callBack: ((Boolean) -> Unit)) {
        val dialog = Dialog(context)
        val binding = DialogPermissionAlertBinding.inflate(LayoutInflater.from(context))
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(binding.root)
        val width = (context.resources?.displayMetrics?.widthPixels?.times(0.9))?.toInt()
        if (width != null) {
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.apply {
            titleTx.text = title
            descTx.text = desc
            cancelButton.setSafeOnClickListener {
                dialog.dismiss()
                callBack(false)
            }
            okButton.setSafeOnClickListener {
                dialog.dismiss()
                callBack(true)
            }
        }
    }

    private fun checkAllPermissions(context: Context):Boolean{
        var notificationPerm = false
        var doNotDisturbPerm = false
        var overLayPerm = false
        var bgLocationPerm = false
        notificationPerm = if (Build.VERSION.SDK_INT >= 33) {
            (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        }else{
            true
        }
       val mNotificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        doNotDisturbPerm = mNotificationManager.isNotificationPolicyAccessGranted

        overLayPerm = Settings.canDrawOverlays(context)

        bgLocationPerm = checkBackgroundLocationPermission(context)
        return notificationPerm && doNotDisturbPerm && overLayPerm && bgLocationPerm
    }





}