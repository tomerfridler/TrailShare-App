package com.example.trailshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogout: Button
    private lateinit var btnShareTrip: Button
    private lateinit var btnFindTrips: Button
    private lateinit var btnAllMessages: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogout = findViewById(R.id.main_btn_logout)
        btnShareTrip = findViewById(R.id.btn_share_trip)
        btnFindTrips = findViewById(R.id.btn_find_trips)
        btnAllMessages = findViewById(R.id.btn_all_messages)

        btnLogout.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnSuccessListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        btnShareTrip.setOnClickListener {
            startActivity(Intent(this, ShareTripActivity::class.java))
        }

        btnFindTrips.setOnClickListener {
            startActivity(Intent(this, FindTripsActivity::class.java))
        }

        btnAllMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
    }
}
