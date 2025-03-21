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

        // Initialize UI elements
        nameTextView = findViewById(R.id.name)
        profileImageView = findViewById(R.id.imageView)
        logOutButton = findViewById(R.id.logOutbtn)

        // Get current user's AccessToken
        val accessToken = AccessToken.getCurrentAccessToken()

        if (accessToken != null && !accessToken.isExpired) {
            getUserProfile(accessToken)
        } else {
            // If token is missing or expired, go to the authentication screen
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
        }

        // Logout button handler
        logOutButton.setOnClickListener {
            LoginManager.getInstance().logOut()
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
        }
    }

    private fun getUserProfile(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(accessToken) { `object`, _ ->
            try {
                // Check if the object is not null
                if (`object` != null) {
                    val fullName = `object`.getString("name")
                    val url = `object`.getJSONObject("picture")
                        .getJSONObject("data")
                        .getString("url")

                    // Set the name in TextView
                    nameTextView.text = fullName

                    // Load the image using Picasso
                    if (url != null) {
                        Picasso.get().load(url).into(profileImageView)
                    } else {
                        Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to get profile data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                // Handle JSON parsing errors
                Toast.makeText(this, "Error fetching profile data", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        // Set request parameters
        val parameters = Bundle()
        parameters.putString("fields", "id,name,link,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }
}