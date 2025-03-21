package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthenticationBinding
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONException


class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var prefs: PrefsHelper
    private lateinit var callbackManager: CallbackManager

    // Оголосити ActivityResultLauncher для реєстрації
    private val registrationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Оновлюємо список зареєстрованих користувачів після успішної реєстрації
            updateRegisteredUsersList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsHelper(this)

        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
        FacebookSdk.fullyInitialize()

        callbackManager = CallbackManager.Factory.create()

        setupUI()
        updateRegisteredUsersList()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener { login() }
        binding.btnRegister.setOnClickListener {
            // Запуск RegistrationActivity через ActivityResultLauncher
            val intent = Intent(this, RegistrationActivity::class.java)
            registrationLauncher.launch(intent)
        }
        binding.facebookButton.setOnClickListener {
            loginWithFacebook()
        }
    }



    private fun login() {
        val email = binding.etLogin.text.toString()
        val password = binding.etPassword.text.toString()
        val users = prefs.getUsers()

        users.find { it.email.equals(email, ignoreCase = true) && it.password == password }?.let { user ->
            // Встановлюємо поточного користувача
            prefs.setCurrentUser(email)

            // Переходимо на головний екран
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } ?: showError("Невірний email або пароль")
    }
    private fun loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                startActivity(Intent(this@AuthenticationActivity, FacebookLoginActivity::class.java))
                finish()
            }

            override fun onCancel() {
                showError("Вхід через Facebook скасовано")
            }

            override fun onError(error: FacebookException) {
                showError("Помилка входу через Facebook: ${error.message}")
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val request = GraphRequest.newMeRequest(token) { jsonObject, _ ->
            try {
                val email = jsonObject?.getString("email")
                val name = jsonObject?.getString("name")

                if (email != null) {
                    prefs.setCurrentUser(email)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    showError("Не вдалося отримати email")
                }

            } catch (e: JSONException) {
                showError("Помилка отримання даних з Facebook")
            }
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,email")
        request.parameters = parameters
        request.executeAsync()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }



    private fun updateRegisteredUsersList() {
        val users = prefs.getUsers()
        val registeredEmails = users.joinToString(", ") { it.email }
        binding.tvRegisteredUsers.text = getString(R.string.registered_users, registeredEmails)
    }


    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}