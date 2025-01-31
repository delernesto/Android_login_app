package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var prefs: PrefsHelper

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

    private fun updateRegisteredUsersList() {
        val users = prefs.getUsers()
        val registeredEmails = users.joinToString(", ") { it.email }
        binding.tvRegisteredUsers.text = getString(R.string.registered_users, registeredEmails)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}