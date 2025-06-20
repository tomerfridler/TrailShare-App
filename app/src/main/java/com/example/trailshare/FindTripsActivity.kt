package com.example.trailshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class FindTripsActivity : AppCompatActivity(), OnMapReadyCallback, TripAdapter.OnTripClickListener {

    private lateinit var map: GoogleMap
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private val tripList = mutableListOf<Trip>()
    private val tripKeys = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_trips)

        val backButton = findViewById<ImageButton>(R.id.find_trips_back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        recyclerView = findViewById(R.id.recycler_trips)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TripAdapter(tripList, this)
        recyclerView.adapter = adapter

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        loadTripsFromRealtimeDatabase()

        map.setOnMarkerClickListener { marker ->
            val markerKey = marker.snippet
            if (markerKey != null) {
                val index = tripKeys.indexOf(markerKey)
                if (index != -1) {
                    recyclerView.scrollToPosition(index)
                    adapter.selectTrip(tripList[index])
                }
            }
            true
        }
    }

    private fun loadTripsFromRealtimeDatabase() {
        val database = FirebaseDatabase.getInstance()
        val tripsRef = database.getReference("trips")

        tripsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tripList.clear()
                tripKeys.clear()
                map.clear()

                for (tripSnapshot in snapshot.children) {
                    val trip = tripSnapshot.getValue(Trip::class.java)
                    if (trip != null) {
                        tripList.add(trip)
                        tripKeys.add(tripSnapshot.key ?: "")

                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(trip.latitude, trip.longitude))
                                .title(trip.title)
                                .snippet(tripSnapshot.key)
                        )
                    }
                }
                adapter.notifyDataSetChanged()

                if (tripList.isNotEmpty()) {
                    val firstTrip = tripList[0]
                    val location = LatLng(firstTrip.latitude, firstTrip.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 5f))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FindTripsActivity, "Failed to load trips: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onTripClick(trip: Trip) {
        val intent = Intent(this, TripDetailsActivity::class.java)
        intent.putExtra("trip_title", trip.title)
        intent.putExtra("trip_description", trip.description)
        intent.putExtra("trip_imageUrl", trip.imageUrl)
        intent.putExtra("trip_ownerEmail", trip.ownerEmail)
        startActivity(intent)
    }
}
