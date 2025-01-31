package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myapplication.databinding.ActivityMainBinding
import android.widget.Toast


class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsHelper
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsHelper(this)
        currentUserEmail = prefs.getCurrentUser()

        checkAutoLogin()
        setupUI()
    }

    private fun checkAutoLogin() {
        currentUserEmail?.let { email ->
            val users = prefs.getUsers()
            val user = users.find { it.email == email }

            user?.let {
                // Перевіряємо та оновлюємо лічильник
                if (!prefs.checkAndUpdateAutoLogin(email)) {
                    // Якщо лічильник = 0, переходимо на авторизацію
                    navigateToAuth()
                    return
                }

                updateLoginCounter(prefs.getAutoLoginCount(email))
                showMainScreen(email)
            } ?: navigateToAuth()
        } ?: navigateToAuth()
    }

    private fun showMainScreen(email: String) {
        binding.tvWelcome.text = getString(R.string.welcome_message, email) // Логін на новому рядку
    }

    private fun updateLoginCounter(count: Int) {
        binding.tvLoginCounter.text = getString(R.string.login_counter, count)
    }

    private fun setupUI() {
        binding.btnLogout.setOnClickListener { handleLogout() }
        binding.btnCloseApp.setOnClickListener { finish() }
        binding.btnDeleteAccount.setOnClickListener { handleDeleteAccount() }
    }

    private fun handleDeleteAccount() {
        currentUserEmail?.let { email ->
            prefs.deleteCurrentUser(email)
            Toast.makeText(this, "Акаунт видалено", Toast.LENGTH_SHORT).show()
            navigateToAuth()
        } ?: run {
            Toast.makeText(this, "Помилка: користувача не знайдено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLogout() {
        currentUserEmail?.let { email ->
            prefs.resetAutoLogin(email)
        }
        prefs.clearCurrentUser()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        startActivity(Intent(this, AuthenticationActivity::class.java))
        finish()
    }
}