package com.waseem.locationtracking.ui.splash

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.waseem.locationtracking.R
import com.waseem.locationtracking.databinding.ActivityLoginBinding
import com.waseem.locationtracking.databinding.ActivitySplashScreenBinding
import com.waseem.locationtracking.ui.PermissionsActivity
import com.waseem.locationtracking.ui.dashboard.MainActivity
import com.waseem.locationtracking.ui.login.LoginActivity
import com.waseem.locationtracking.ui.tracking.LocationTrackingActivity
import com.waseem.locationtracking.utils.extension.startLocationService
import com.waseem.locationtracking.utils.helper.PermissionHelper
import com.waseem.locationtracking.utils.helper.PermissionHelper.checkAllPermissions
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySplashScreenBinding
    private val preferenceHelper: PreferenceHelper by inject()

    private val mNotificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        animateImage(binding.splashImg)
    }

    private fun animateImage(imageView: ImageView) {
        // Fade in and out animation
        val fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f)
        fadeIn.duration = 1000 // 1 second

        val fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)
        fadeOut.duration = 1000 // 1 second

        fadeIn.startDelay = 500 // Delay to start fade out after fade in

        // Start the animations one after the other
        fadeIn.start()
        fadeIn.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                fadeOut.start()
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        fadeOut.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                if (preferenceHelper.getLoginStatus()){
                    moveToNextScreen(preferenceHelper.getString(PreferenceHelper.PreferenceVariable.USER_EMAIL))
                }else{
                    startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                    finish()
                }
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun moveToNextScreen(email: String) {
        if (!checkAllPermissions(this)){
            startActivity(Intent(this@SplashScreenActivity, PermissionsActivity::class.java))
            finish()
        }else{
            if (email.contains("admin")) {
                startActivity(Intent(this@SplashScreenActivity, LocationTrackingActivity::class.java))
                finish()
            } else{
                startLocationService()
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            }
        }

    }


}