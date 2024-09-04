package com.waseem.locationtracking.ui.login

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.waseem.locationtracking.databinding.ActivityLoginBinding
import com.waseem.locationtracking.ui.PermissionsActivity
import com.waseem.locationtracking.ui.dashboard.MainActivity
import com.waseem.locationtracking.ui.signup.SignupActivity
import com.waseem.locationtracking.ui.tracking.LocationTrackingActivity
import com.waseem.locationtracking.utils.extension.startLocationService
import com.waseem.locationtracking.utils.helper.PermissionHelper.checkAllPermissions
import com.waseem.locationtracking.utils.helper.PermissionHelper.checkBackgroundLocationPermission
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.USER_EMAIL
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.USER_ID
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val preferenceHelper: PreferenceHelper by inject()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private val mNotificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


//        startActivity(Intent(this,PermissionsActivity::class.java))

        binding.apply {
            loginBtn?.setOnClickListener {

                val email = emailEditText?.text.toString().trim()
                val password = etPassword?.text.toString().trim()

                if (email.isEmpty()) {
                    emailEditText?.error = "Email is required"
                    emailEditText?.requestFocus()
                    return@setOnClickListener
                }

                if (password.isEmpty()) {
                    etPassword?.error = "Password is required"
                    etPassword?.requestFocus()
                    return@setOnClickListener
                }
                auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {

                    it.user?.uid?.let {
                        preferenceHelper.saveString(USER_ID,it)
                        preferenceHelper.saveString(USER_EMAIL,email)
                        preferenceHelper.setLoginStatus()
                    }
                  moveToNextScreen(email)
                }.addOnFailureListener {
                    showLoginFailed(it.message.toString())
                }
            }
            doNotHaveAccount?.setOnClickListener {
                startActivity(Intent(this@LoginActivity,SignupActivity::class.java))
                finish()
            }
        }

        if (preferenceHelper.getLoginStatus()){
            moveToNextScreen(preferenceHelper.getString(USER_EMAIL))
        }
    }

    private fun moveToNextScreen(email: String) {
        if (!checkAllPermissions(this)){
            startActivity(Intent(this@LoginActivity, PermissionsActivity::class.java))
            finish()
        }else{
            if (email.contains("admin")) {
                startActivity(Intent(this@LoginActivity, LocationTrackingActivity::class.java))
                finish()
            } else{
                startLocationService()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }

    }


    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
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

            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }



}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}