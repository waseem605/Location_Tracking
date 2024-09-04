package com.waseem.locationtracking.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.waseem.locationtracking.R
import com.waseem.locationtracking.databinding.ActivitySignupBinding
import com.waseem.locationtracking.ui.login.LoginActivity
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.USER_EMAIL
import com.waseem.locationtracking.utils.helper.PreferenceHelper.PreferenceVariable.USER_ID
import org.koin.android.ext.android.inject
import java.util.Locale

class SignupActivity : AppCompatActivity() {
    val TAG = "SignupActivityTAG"
    private lateinit var binding:ActivitySignupBinding
    private val preferenceHelper: PreferenceHelper by inject()

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        // Set the locale to avoid the warning
        val locale = Locale.getDefault()
        auth.setLanguageCode(locale.language)

        binding.apply {
            signUpBtn.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (email.isEmpty()) {
                    emailEditText.error = "Email is required"
                    emailEditText.requestFocus()
                    return@setOnClickListener
                }

                if (password.isEmpty()) {
                    etPassword.error = "Password is required"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    etConfirmPassword.error = "Passwords do not match"
                    etConfirmPassword.requestFocus()
                    return@setOnClickListener
                }

                // Create user with email and password
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignupActivity) { task ->
                        if (task.isSuccessful) {

                            Log.d(TAG, "onCreate: ${  task}")
                            Log.d(TAG, "onCreate: ${  task.result.additionalUserInfo?.providerId}")
                            Log.d(TAG, "onCreate: ${  task.result.user?.uid}")
                            task.result.user?.uid?.let {
                                preferenceHelper.saveString(USER_ID,it)
                                preferenceHelper.saveString(USER_EMAIL,email)
                            }

                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(this@SignupActivity, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignupActivity,LoginActivity::class.java))
                            finish()
                            // Redirect to another activity if needed
                        } else {
                            println(task.exception)
                            Log.e(TAG, "onCreate: ${task.exception?.message}", )
                            Log.e(TAG, "onCreate: ${task.exception}", )
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this@SignupActivity, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }


            }

        }
    }
}