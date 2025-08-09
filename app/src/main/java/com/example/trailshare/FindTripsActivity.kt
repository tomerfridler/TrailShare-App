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

// Screen that shows a Google Map + a list (RecyclerView) of trips
// Trips are pulled from Firebase Realtime Database under the "trips" node
class FindTripsActivity : AppCompatActivity(), OnMapReadyCallback, TripAdapter.OnTripClickListener {

    // Google Map instance
    private lateinit var map: GoogleMap

    // RecyclerView + its adapter to display the list of trips
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter

    // In-memory data backing the RecyclerView and a parallel list of RTDB keys
    private val tripList = mutableListOf<Trip>()
    private val tripKeys = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_trips)

        // Back button: return to MainActivity
        val backButton = findViewById<ImageButton>(R.id.find_trips_back_button)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        recyclerView = findViewById(R.id.recycler_trips)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TripAdapter(tripList, this) // "this" implements OnTripClickListener
        recyclerView.adapter = adapter

        // Obtain the map fragment and request async callback when it’s ready
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Called when the Google Map is fully initialized and ready to use
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        loadTripsFromRealtimeDatabase()

        // When a marker is clicked, scroll the list to the matching trip and select it
        map.setOnMarkerClickListener { marker ->
            val markerKey = marker.snippet
            if (markerKey != null) {
                val index = tripKeys.indexOf(markerKey)
                if (index != -1) {
                    recyclerView.scrollToPosition(index)
                    adapter.selectTrip(tripList[index])
                }
            }
            true // consume the event
        }
    }


    // Reads all trips from the "trips" node in Realtime Database
    // For each trip: add to list, remember its key, and place a marker on the map
    private fun loadTripsFromRealtimeDatabase() {
        val database = FirebaseDatabase.getInstance()
        val tripsRef = database.getReference("trips")

        tripsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Clear old data to avoid duplicates before repopulating
                tripList.clear()
                tripKeys.clear()
                map.clear()

                for (tripSnapshot in snapshot.children) {
                    val trip = tripSnapshot.getValue(Trip::class.java)
                    if (trip != null) {
                        // Keep the trip and its database key
                        tripList.add(trip)
                        tripKeys.add(tripSnapshot.key ?: "")

                        // Add a marker for this trip on the map
                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(trip.latitude, trip.longitude))
                                .title(trip.title)
                                .snippet(tripSnapshot.key)
                        )
                    }
                }
                // Refresh the list UI
                adapter.notifyDataSetChanged()

                // Move the camera to the first trip (if any) with a wide zoom
                if (tripList.isNotEmpty()) {
                    val firstTrip = tripList[0]
                    val location = LatLng(firstTrip.latitude, firstTrip.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 5f))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Database read failed
                Toast.makeText(this@FindTripsActivity, "Failed to load trips: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Callback from TripAdapter when a trip item is clicked
    // Opens the TripDetailsActivity and passes the trip details via Intent extras
    override fun onTripClick(trip: Trip) {
        val intent = Intent(this, TripDetailsActivity::class.java)
        intent.putExtra("trip_title", trip.title)
        intent.putExtra("trip_description", trip.description)
        intent.putExtra("trip_imageUrl", trip.imageUrl)
        intent.putExtra("trip_ownerEmail", trip.ownerEmail)
        startActivity(intent)
    }
}
