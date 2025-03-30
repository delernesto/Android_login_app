package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.squareup.picasso.Picasso
import org.json.JSONException

class FacebookLoginActivity : AppCompatActivity() {
    private lateinit var nameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_login)

        // Ініціалізація UI елементів
        nameTextView = findViewById(R.id.name)
        profileImageView = findViewById(R.id.imageView)
        logOutButton = findViewById(R.id.logOutbtn)

        // Перевірка токена
        checkFacebookToken()

        // Обробник кнопки виходу
        logOutButton.setOnClickListener {
            logout()
        }
    }

    private fun checkFacebookToken() {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null || accessToken.isExpired) {
            redirectToAuth()
        } else {
            getUserProfile(accessToken)
        }
    }

    private fun getUserProfile(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
            try {
                jsonObject?.let {
                    val fullName = it.getString("name")
                    val url = it.getJSONObject("picture")
                        .getJSONObject("data")
                        .getString("url")

                    nameTextView.text = fullName
                    Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.ic_profile_placeholder) // Плейсхолдер
                        .error(R.drawable.ic_profile_placeholder)       // Показувати при помилці
                        .into(profileImageView)
                } ?: run {
                    showError("Failed to get profile data")
                }
            } catch (e: JSONException) {
                showError("Error parsing profile data")
                e.printStackTrace()
            }
        }

        val parameters = Bundle().apply {
            putString("fields", "id,name,link,picture.type(large)")
        }
        request.parameters = parameters
        request.executeAsync()
    }

    private fun logout() {
        LoginManager.getInstance().logOut()
        redirectToAuth()
    }

    private fun redirectToAuth() {
        val intent = Intent(this, AuthenticationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish() // Тепер finish() має працювати коректно
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}