package com.example.trailshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI

class MainActivity : AppCompatActivity() {

    // Declare buttons for different actions in the main screen
    private lateinit var btnLogout: Button
    private lateinit var btnShareTrip: Button
    private lateinit var btnFindTrips: Button
    private lateinit var btnAllMessages: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout file for this activity
        setContentView(R.layout.activity_main)

        // Connect the buttons in the code to their corresponding UI elements in the layout
        btnLogout = findViewById(R.id.main_btn_logout)
        btnShareTrip = findViewById(R.id.btn_share_trip)
        btnFindTrips = findViewById(R.id.btn_find_trips)
        btnAllMessages = findViewById(R.id.btn_all_messages)

        // Handle logout button click
        btnLogout.setOnClickListener {
            // Sign out the user from Firebase Authentication
            AuthUI.getInstance().signOut(this).addOnSuccessListener {
                // After successful logout, go back to the Login screen
                startActivity(Intent(this, LoginActivity::class.java))
                // Close MainActivity so the user cannot return with the back button
                finish()
            }
        }

        // Handle "Share Trip" button click
        btnShareTrip.setOnClickListener {
            startActivity(Intent(this, ShareTripActivity::class.java))
        }

        // Handle "Find Trips" button click
        btnFindTrips.setOnClickListener {
            startActivity(Intent(this, FindTripsActivity::class.java))
        }

        // Handle "All Messages" button click
        btnAllMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
    }
}
