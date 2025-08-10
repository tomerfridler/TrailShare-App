package com.example.trailshare

import android.os.Bundle
import android.widget.*
import android.util.Log
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.gms.maps.model.LatLng
import java.util.*

class ShareTripActivity : AppCompatActivity() {

    // UI references
    private lateinit var btnAddPhotos: Button
    private lateinit var btnSubmitTrip: Button
    private lateinit var editLocation: EditText
    private lateinit var editDescription: EditText
    private lateinit var backButton: ImageButton

    private var selectedImageUri: Uri? = null    // Selected image from gallery
    private var selectedLatLng: LatLng? = null    // Coordinates returned by Places Autocomplete

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1  // Request code for Places Autocomplete result
        private const val TAG = "RealtimeDBDebug"        // Tag for Logcat debugging
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_trip)   // Inflate layout


        // Bind views
        btnAddPhotos = findViewById(R.id.btn_add_photos)
        btnSubmitTrip = findViewById(R.id.btn_submit_trip)
        editLocation = findViewById(R.id.edit_location)
        editDescription = findViewById(R.id.edit_description)
        backButton = findViewById(R.id.share_trip_back_button)

        // Back button closes this screen
        backButton.setOnClickListener {
            finish()
        }

        // Initialize Google Places SDK once per application process
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        // Location field is not manually editable
        editLocation.isFocusable = false
        editLocation.setOnClickListener {
            // Define which fields we want back from Places
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            // Launch full-screen autocomplete UI
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        // Activity Result API
        val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
            }
        }

        // Open image picker when "Add Photos" is clicked
        btnAddPhotos.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // Validate inputs and start upload/save flow
        btnSubmitTrip.setOnClickListener {
            val location = editLocation.text.toString().trim()
            val description = editDescription.text.toString().trim()

            // Basic form validation
            if (location.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If valid → upload image (if any) and save trip
            uploadImageAndSaveTrip(location, description)
        }
    }

    // Handle result from Places Autocomplete
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val place = Autocomplete.getPlaceFromIntent(data)  // Extract selected place
                editLocation.setText(place.address ?: place.name)  // Show the address
                selectedLatLng = place.latLng  // Store coordinates for later persistence
                Log.d(TAG, "Location selected: ${place.address} / ${place.latLng}")
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data)  // Surface error to user and logcat
                Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Autocomplete error: ${status.statusMessage}")
            }
        }
    }

    // Uploads the selected image to Firebase Storage
    private fun uploadImageAndSaveTrip(location: String, description: String) {
        if (selectedImageUri == null) {  // No image selected → save immediately with empty imageUrl
            saveTripToRealtimeDatabase(location, description, "", selectedLatLng)
            return
        }

        // Create a unique file path in Storage
        val filename = "trail_images/${UUID.randomUUID()}.jpg"
        val storageRef = Firebase.storage.reference.child(filename)

        // Upload file to Firebase Storage
        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Persist trip with the image URL
                    saveTripToRealtimeDatabase(location, description, imageUrl, selectedLatLng)
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get image URL: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Failed to get image URL", e)
                }
            }
            .addOnFailureListener { e ->
                // Upload failed → inform user
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Image upload failed", e)
            }
    }

    // Persists a new trip under the "trips" node in Firebase Realtime Database
    // Each trip is stored under a unique push ID containing metadata and owner info
    private fun saveTripToRealtimeDatabase(location: String, description: String, imageUrl: String, latLng: LatLng?) {
        val database = FirebaseDatabase.getInstance()
        val tripsRef = database.getReference("trips")  // Root node for all trips

        // Generate a new unique child key for this trip
        val tripId = tripsRef.push().key

        // Data map to persist
        val tripData = hashMapOf(
            "title" to location,
            "description" to description,
            "imageUrl" to imageUrl,
            "latitude" to (latLng?.latitude ?: 0.0),
            "longitude" to (latLng?.longitude ?: 0.0),
            "timestamp" to System.currentTimeMillis(),
            "ownerEmail" to (FirebaseAuth.getInstance().currentUser?.email ?: ""),
            "ownerUid" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")

        )

        // Only proceed if a push ID was successfully created
        if (tripId != null) {
            tripsRef.child(tripId).setValue(tripData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Trip saved!", Toast.LENGTH_SHORT).show()
                    finish()  // Close activity after successful save
                }
                .addOnFailureListener { e ->
                    // Handle database write failure
                    Toast.makeText(this, "Failed to save trip: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Failed to save trip", e)
                }
        } else {
            Log.e(TAG, "Trip ID was null")
        }
    }
}
