package com.waseem.locationtracking.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.waseem.locationtracking.R
import com.waseem.locationtracking.databinding.ActivityPermissionsBinding
import com.waseem.locationtracking.ui.dashboard.MainActivity
import com.waseem.locationtracking.ui.dialogs.OverLayPermissionAppDialog
import com.waseem.locationtracking.utils.extension.beGone
import com.waseem.locationtracking.utils.extension.beVisible
import com.waseem.locationtracking.utils.extension.showSettings
import com.waseem.locationtracking.utils.extension.showToastMsg
import com.waseem.locationtracking.utils.extension.startLocationService
import com.waseem.locationtracking.utils.helper.BatteryUtils.batteryOptimizeDialog
import com.waseem.locationtracking.utils.helper.BatteryUtils.permissionAlertDialog
import com.waseem.locationtracking.utils.helper.DialogUtils.locationPermissionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PermissionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionsBinding
    private lateinit var mOverLayPermissionAppDialog: OverLayPermissionAppDialog
    private lateinit var mNotificationManager : NotificationManager
    private var hasNotificationPermissionGranted = false
    private var mBatteryOptimizationIsIgnoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        overLayPermission()
        notificationPermission()
        locationPermission()
        viewButtonClickListener()

//        binding.doNotDisturbSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (!isChecked){
//                disableContinueButton()
//            }
//        }
        binding.batteryOptimizationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked){
                disableContinueButton()
            }
        }

        binding.continueBtn.setOnClickListener { moveToMainScreen() }
    }

    private fun viewButtonClickListener() {
        binding.apply {
//            doNotDisturbButton.setOnClickListener {
//                if (!binding.doNotDisturbSwitch.isChecked) {
//                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//                    doNotDisturbResultLauncher.launch(intent)
//                    binding.doNotDisturbSwitch.isChecked = true
//                    binding.doNotDisturbSwitch.setEnabled(false)
//                }
//            }

            notificationButton.setOnClickListener {
                if (!binding.notificationSwitch.isChecked){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    binding.notificationSwitch.isChecked = true
                    binding.notificationSwitch.setEnabled(false)
                }
            }
            overLeyButton.setOnClickListener {
                if (!overlaySwitch.isChecked){
                    mOverLayPermissionAppDialog.show()
                    overlaySwitch.isChecked = true
                    overlaySwitch.setEnabled(false)
                }
            }
            locationButton.setOnClickListener {
                if (!locationSwitch.isChecked) {
                    locationPermissionDialog(callback = {
                        checkLocationPermission()
                    })
                }
            }
            batteryOptButton.setOnClickListener {
                if (!batteryOptimizationSwitch.isChecked){
                    batteryOptimizeDialog(this@PermissionsActivity, callBack = {
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            data = Uri.parse("package:$packageName")
                        }

                        batteryOptimizeResultLauncher.launch(intent)
//                        batteryOptimizeResultLauncher.launch(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    })
                }
            }
        }
    }


    private fun locationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.locationSwitch.isChecked = false
            binding.locationSwitch.setEnabled(true)
            disableContinueButton()
        }else{
            binding.locationSwitch.isChecked = true
            binding.locationSwitch.setEnabled(false)
            enableContinueButton()
        }

        binding.locationSwitch.setOnCheckedChangeListener { _, _ ->
//            if (isChecked){
////                binding.locationSwitch.setEnabled(false)
////                checkLocationPermission()
//            }else{
////                binding.locationSwitch.setEnabled(true)
//            }
        }
    }


    private suspend fun batterOptimization() {
        withContext(Dispatchers.IO) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            delay(1000)
            mBatteryOptimizationIsIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
            Log.d(TAG, "batterOptimization:   isIgnoring $mBatteryOptimizationIsIgnoring")
            withContext(Dispatchers.Main) {
                if (mBatteryOptimizationIsIgnoring) {
                    binding.batteryOptimizationSwitch.isChecked = true
                    binding.batteryOptimizationSwitch.setEnabled(false)
                    enableContinueButton()
                } else {
                    binding.batteryOptimizationSwitch.isChecked = false
                    binding.batteryOptimizationSwitch.setEnabled(true)
                    disableContinueButton()
                }
            }
        }
    }

    private var batteryOptimizeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        mBatteryOptimizationIsIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        if (mBatteryOptimizationIsIgnoring) {
            binding.batteryOptimizationSwitch.isChecked = true
            binding.batteryOptimizationSwitch.setEnabled(false)
            enableContinueButton()
        }else{
            binding.batteryOptimizationSwitch.isChecked = false
            binding.batteryOptimizationSwitch.setEnabled(true)
            disableContinueButton()
        }
    }

//    private fun checkDoNotDisturb() {
//        if (!mNotificationManager.isNotificationPolicyAccessGranted) {
//            binding.doNotDisturbSwitch.isChecked = false
//            binding.doNotDisturbSwitch.setEnabled(true)
//            disableContinueButton()
//        } else {
//            binding.doNotDisturbSwitch.isChecked = true
//            binding.doNotDisturbSwitch.setEnabled(false)
//            enableContinueButton()
//        }
//        Log.d(TAG, "checkDoNotDisturb: ${mNotificationManager.isNotificationPolicyAccessGranted}")
//    }

//    private var doNotDisturbResultLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            if (notificationManager.isNotificationPolicyAccessGranted) {
//                enableContinueButton()
//                binding.doNotDisturbSwitch.isChecked = true
//                binding.doNotDisturbSwitch.setEnabled(false)
//            } else {
//                disableContinueButton()
//                binding.doNotDisturbSwitch.isChecked = false
//                binding.doNotDisturbSwitch.setEnabled(true)
//            }
//        }

    private fun checkNotificationPermission(){
        if (Build.VERSION.SDK_INT >= 33) {
            binding.notificationOnLayout.beVisible()
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                hasNotificationPermissionGranted = true
                binding.notificationSwitch.isChecked = true
                binding.notificationSwitch.setEnabled(false)
                enableContinueButton()
            }else{
                hasNotificationPermissionGranted = false
                binding.notificationSwitch.isChecked = false
                binding.notificationSwitch.setEnabled(true)
                disableContinueButton()
            }
        }else{
            hasNotificationPermissionGranted = true
            binding.notificationOnLayout.beGone()
        }

        Log.d(TAG, "checkNotificationPermission: $hasNotificationPermissionGranted")
    }
    private fun notificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            binding.notificationOnLayout.beVisible()
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                binding.notificationSwitch.isChecked = true
                binding.notificationSwitch.setEnabled(false)
                enableContinueButton()
            }else{
                binding.notificationSwitch.isChecked = false
                binding.notificationSwitch.setEnabled(true)
                disableContinueButton()
            }
        }else{
            binding.notificationOnLayout.beGone()
        }

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                }
            }else{
                disableContinueButton()
            }
        }
    }


    private fun overLayPermission() {
        mOverLayPermissionAppDialog = OverLayPermissionAppDialog(this) {
            kotlin.runCatching {
                val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                myIntent.data = Uri.parse("package:${packageName}")
                overlayResultLauncher.launch(myIntent)
//                startActivityForResult(myIntent, 33)
            }
        }
        binding.overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
//                mOverLayPermissionAppDialog.show()
            }else{
                disableContinueButton()
            }
        }
    }
    private var overlayResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (Settings.canDrawOverlays(this)) {
            binding.overlaySwitch.isChecked = true
            binding.overlaySwitch.setEnabled(false)
            enableContinueButton()
        } else {
            binding.overlaySwitch.isChecked = false
            binding.overlaySwitch.setEnabled(true)
            disableContinueButton()
        }
    }

    private fun checkOverLayPermission(){
        if (!Settings.canDrawOverlays(this)) {
            binding.overlaySwitch.isChecked = false
            binding.overlaySwitch.setEnabled(true)
            disableContinueButton()
        } else {
            binding.overlaySwitch.isChecked = true
            binding.overlaySwitch.setEnabled(false)
            enableContinueButton()
        }
        Log.d(TAG, "checkOverLayPermission: ${Settings.canDrawOverlays(this)}")
    }
    private fun disableContinueButton() {
        binding.continueBtn.apply {
            isEnabled = false
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey))
        }
    }

    private fun enableContinueButton() {
        binding.apply {
            if(hasNotificationPermissionGranted /*&& doNotDisturbSwitch.isChecked*/ && overlaySwitch.isChecked && mBatteryOptimizationIsIgnoring && locationSwitch.isChecked){
                continueBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PermissionsActivity, R.color.primary_color))
                continueBtn.isEnabled = true
//                moveToMainScreen()
            }else{
                continueBtn.isEnabled = false
                continueBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PermissionsActivity, R.color.grey))
            }
        }
    }


    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            batterOptimization()
        }
        checkNotificationPermission()
//        checkDoNotDisturb()
        checkOverLayPermission()
        checkBackgroundLocationPermission()
    }

    private fun checkBackgroundLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.locationSwitch.isChecked = false
            binding.locationSwitch.setEnabled(true)
            disableContinueButton()
        }else{
            binding.locationSwitch.isChecked = true
            binding.locationSwitch.setEnabled(false)
            enableContinueButton()
        }
    }


    private fun moveToMainScreen() {
        startLocationService()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        this.finish()
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                binding.notificationSwitch.isChecked = false
                binding.notificationSwitch.setEnabled(true)
                disableContinueButton()
                if (Build.VERSION.SDK_INT >= 33) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        showNotificationPermissionRationale()
                    } else {
                        showSettingDialog()
                    }
                }
            } else {
                binding.notificationSwitch.isChecked = true
                binding.notificationSwitch.setEnabled(false)
                showToastMsg(resources.getString(R.string.notification_permission_granted))
                enableContinueButton()
            }
        }

    private fun showSettingDialog() {
        permissionAlertDialog(this,
            resources.getString(R.string.notification_permission),
            resources.getString(R.string.notification_permission_required),
            callBack = {
                if (it){
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }else{
                    binding.notificationSwitch.isChecked = false
                    binding.notificationSwitch.setEnabled(true)
                    disableContinueButton()
                }
            })
    }

    private fun showNotificationPermissionRationale() {
        permissionAlertDialog(this,
            resources.getString(R.string.notification_permission),
            resources.getString(R.string.notification_permission_required),
            callBack = {
                if (it){
                    if (Build.VERSION.SDK_INT >= 33) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }else{
                    binding.notificationSwitch.isChecked = false
                    binding.notificationSwitch.setEnabled(true)
                    disableContinueButton()
                }
            })
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                permissionAlertDialog(this,
                    resources.getString(R.string.location_permission),
                    resources.getString(R.string.this_app_needs_the_location_permission_please),
                    callBack = {
                        if (it){
                            requestLocationPermission()
                        }else{
                            binding.locationSwitch.isChecked = false
                            binding.locationSwitch.setEnabled(true)
                            disableContinueButton()
                        }
                    })

            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }
    }

    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private var permissionCount = 0
    private fun requestBackgroundLocationPermission() {
        permissionCount++
        if (permissionCount>2){
            showSettings()
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(TAG, "requestBackgroundLocationPermission: 480")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
                )
            } else {
                Log.d(TAG, "requestBackgroundLocationPermission: 4489")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Now check background location
                        checkBackgroundLocation()
                    }
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    }
                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                Log.d(TAG, "onRequestPermissionsResult: MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION")
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        binding.locationSwitch.isChecked = true
                        binding.locationSwitch.setEnabled(false)
                        enableContinueButton()
                        showToastMsg(resources.getString(R.string.background_permission_granted))
                    }
                } else {
                    binding.locationSwitch.isChecked = false
                    binding.locationSwitch.setEnabled(true)
                    disableContinueButton()
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showToastMsg(resources.getString(R.string.permissions_denied))
                }
                return
            }
        }
    }




    companion object {
        val TAG = "EnablePermissionTAG"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }
}