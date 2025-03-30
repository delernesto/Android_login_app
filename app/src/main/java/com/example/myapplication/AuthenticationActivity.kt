package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthenticationBinding
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONException
import java.security.MessageDigest

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var callbackManager: CallbackManager
    private lateinit var prefs: PrefsHelper // Додано ініціалізацію PrefsHelper

    private val registrationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateRegisteredUsersList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ініціалізація PrefsHelper
        prefs = PrefsHelper(this)

        // Ініціалізація Facebook SDK
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()

        setupUI()
        updateRegisteredUsersList()

        generateKeyHash()
    }

    private fun generateKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            for (signature in info.signingInfo?.apkContentsSigners!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", "Generated Key Hash: $hash")
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "Error getting Key Hash", e)
        }
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnRegister.setOnClickListener {
            registrationLauncher.launch(Intent(this, RegistrationActivity::class.java))
        }

        binding.facebookButton.setOnClickListener {
            loginWithFacebook()
        }
    }

    private fun login() {
        val email = binding.etLogin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password")
            return
        }

        // Виправлено використання 'it' у лямбда-виразі
        val user = prefs.getUsers().find { user ->
            user.email.equals(email, true) && user.password == password
        }

        if (user != null) {
            prefs.setCurrentUser(email)
            startMainActivity()
        } else {
            showError("Invalid email or password")
        }
    }

    private fun loginWithFacebook() {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    showError("Facebook login cancelled")
                }

                override fun onError(error: FacebookException) {
                    showError("Facebook login failed: ${error.message}")
                }
            }
        )

        LoginManager.getInstance().logInWithReadPermissions(
            this,
            listOf("email", "public_profile")
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val request = GraphRequest.newMeRequest(token) { jsonObject, _ ->
            try {
                val email = jsonObject?.getString("email") ?: run {
                    showError("Email is required")
                    return@newMeRequest
                }

                prefs.setCurrentUser(email)
                startMainActivity()
            } catch (e: JSONException) {
                showError("Error processing Facebook data")
            }
        }

        val parameters = Bundle().apply {
            putString("fields", "id,name,email")
        }
        request.parameters = parameters
        request.executeAsync()
    }

    private fun startMainActivity() {
        Intent(this, FacebookLoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        if (!isFinishing && !isDestroyed) {
            finish()
        }
    }

    private fun updateRegisteredUsersList() {
        val users = prefs.getUsers()
        // Виправлено використання 'it' у лямбда-виразі
        val usersText = users.joinToString(", ") { user -> user.email }
        binding.tvRegisteredUsers.text = getString(R.string.registered_users, usersText)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}