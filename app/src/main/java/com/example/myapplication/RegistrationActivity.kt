package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegistrationBinding
import android.util.Patterns

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var prefs: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsHelper(this)

        setupUI()
        updateRegisteredUsersList() // Оновлюємо список при запуску
    }

    private fun setupUI() {
        binding.btnRegister.setOnClickListener { register() }


        // Обробник для показу паролю
        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    private fun register() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        when {
            !isValidEmail(email) -> showError("Невірний формат email")
            else -> {
                val (isValid, errorMessage) = isValidPassword(password)
                if (!isValid) {
                    showError(errorMessage ?: "Невідомий формат пароля")
                    return
                }
                processRegistration(email, password)
            }
        }
    }

    private fun processRegistration(email: String, password: String) {
        val users = prefs.getUsers().toMutableList()

        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            showError("Користувач з таким email вже існує")
            return
        }

        val newUser = User(
            email = email,
            password = password,
            autoLogin = binding.cbAutoLogin.isChecked
        )

        // Ініціалізація лічильника
        if (newUser.autoLogin) {
            prefs.setAutoLoginCount(email, 3) // Лічильник = 3 для автовходу
        } else {
            prefs.setAutoLoginCount(email, 1) // Лічильник = 1 для звичайного входу
        }

        users.add(newUser)
        prefs.saveUsers(users)
        showSuccess()
        updateRegisteredUsersList() // Оновлюємо список після реєстрації
    }

    private fun updateRegisteredUsersList() {
        val users = prefs.getUsers()
        val registeredEmails = users.joinToString(", ") { it.email }
        binding.tvRegisteredUsers.text = "Зареєстровані користувачі: $registeredEmails"
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPassword(password: String): Pair<Boolean, String?> {
        if (password.length < 6) return Pair(false, "Пароль має бути не менше 6 символів")

        val ukrainianLowercaseRegex = Regex("[а-щьюяєіїґ]")
        if (ukrainianLowercaseRegex.containsMatchIn(password)) {
            return Pair(false, "Пароль не повинен містити рядкові українські літери")
        }

        val digits = password.filter { it.isDigit() }
        if (digits.toSet().size != digits.length) {
            return Pair(false, "Цифри у паролі не повинні повторюватися")
        }

        return Pair(true, null)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String = "Реєстрація успішна") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}