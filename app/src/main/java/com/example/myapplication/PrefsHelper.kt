package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefsHelper(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Збереження списку користувачів
    fun saveUsers(users: List<User>) {
        sharedPref.edit()
            .putString("users", gson.toJson(users))
            .apply()
    }

    // Отримання списку користувачів
    fun getUsers(): List<User> {
        val json = sharedPref.getString("users", "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<User>>() {}.type)
    }

    // Отримання лічильника автовходів
    fun getAutoLoginCount(email: String): Int =
        sharedPref.getInt("${email}_count", if (getUsers().find { it.email == email }?.autoLogin == true) 3 else 1)

    // Встановлення лічильника автовходів
    fun setAutoLoginCount(email: String, count: Int) {
        sharedPref.edit()
            .putInt("${email}_count", count)
            .apply()
    }

    // Зменшення лічильника автовходів
    fun decrementAutoLoginCount(email: String) {
        val count = getAutoLoginCount(email) - 1
        sharedPref.edit()
            .putInt("${email}_count", count)
            .apply()
    }

    // Перевірка лічильника та оновлення його значення
    fun checkAndUpdateAutoLogin(email: String): Boolean {
        val loginCount = getAutoLoginCount(email)
        return if (loginCount <= 0) {
            // Якщо лічильник = 0, повертаємо початкове значення
            val user = getUsers().find { it.email == email }
            if (user?.autoLogin == true) {
                setAutoLoginCount(email, 3) // Початкове значення для автовходу
            } else {
                setAutoLoginCount(email, 1) // Початкове значення для звичайного входу
            }
            false // Повертаємо false, щоб показати, що потрібно перейти на авторизацію
        } else {
            decrementAutoLoginCount(email) // Зменшуємо лічильник
            true // Повертаємо true, щоб продовжити роботу
        }
    }

    // Скидання лічильника автовходів
    fun resetAutoLogin(email: String) {
        sharedPref.edit()
            .remove("${email}_count")
            .apply()
    }

    // Видалення поточного користувача
    fun clearCurrentUser() {
        sharedPref.edit()
            .remove("current_user")
            .apply()
    }

    // Збереження поточного користувача
    fun setCurrentUser(email: String) {
        sharedPref.edit()
            .putString("current_user", email)
            .apply()
    }

    // Отримання поточного користувача
    fun getCurrentUser(): String? =
        sharedPref.getString("current_user", null)

    fun deleteCurrentUser(email: String) {
        val users = getUsers().toMutableList()
        users.removeIf { it.email == email }
        saveUsers(users)
        resetAutoLogin(email)
        clearCurrentUser()
    }
}