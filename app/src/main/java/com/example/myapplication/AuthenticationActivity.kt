package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.myapplication.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var prefs: PrefsHelper

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
            startActivityForResult(
                Intent(this, RegistrationActivity::class.java),
                REGISTRATION_REQUEST_CODE
            )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REGISTRATION_REQUEST_CODE && resultCode == RESULT_OK) {
            updateRegisteredUsersList() // Оновлюємо список після успішної реєстрації
        }
    }

    companion object {
        private const val REGISTRATION_REQUEST_CODE = 1001
    }
}