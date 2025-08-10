package com.example.trailshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailshare.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TripDetailsActivity : AppCompatActivity() {

    // UI reviews list
    private lateinit var reviewsRecycler: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()

    // Firebase + state
    private lateinit var databaseRef: DatabaseReference
    private lateinit var currentUserEmail: String
    private lateinit var tripKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)  // Inflate the screen layout

        // Read data passed from previous screen via Intent extras
        val title = intent.getStringExtra("trip_title") ?: ""
        val description = intent.getStringExtra("trip_description") ?: ""
        val imageUrl = intent.getStringExtra("trip_imageUrl") ?: ""
        val ownerEmail = intent.getStringExtra("trip_ownerEmail") ?: ""

        // Bind UI views from XML
        val backButton = findViewById<ImageButton>(R.id.trip_details_back_button)
        val tripImage = findViewById<ImageView>(R.id.trip_image)
        val tripTitle = findViewById<TextView>(R.id.trip_location)
        val tripDescription = findViewById<TextView>(R.id.trip_description)
        val ratingBar = findViewById<RatingBar>(R.id.trip_rating_bar)
        val reviewInput = findViewById<EditText>(R.id.trip_review_input)
        val submitReviewButton = findViewById<Button>(R.id.btn_submit_review)
        val askButton = findViewById<Button>(R.id.btn_message)
        val deleteButton = findViewById<Button>(R.id.btn_delete_trip)

        // Setup RecyclerView for reviews
        reviewsRecycler = findViewById(R.id.recycler_reviews)
        reviewsRecycler.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviewList)
        reviewsRecycler.adapter = reviewAdapter

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Populate the UI with trip content
        tripTitle.text = title
        tripDescription.text = description

        // Load trip image with Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(tripImage)

        // Firebase setup
        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        databaseRef = FirebaseDatabase.getInstance().reference
        tripKey = title.replace(" ", "_")

        loadReviews()

        // Handle review submission
        submitReviewButton.setOnClickListener {
            val rating = ratingBar.rating
            val reviewText = reviewInput.text.toString().trim()

            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please write a review!", Toast.LENGTH_SHORT).show()
            } else {
                // Create review model and push it under /reviews/tripKey/generatedId
                val review = Review(
                    userEmail = currentUserEmail,
                    text = reviewText,
                    rating = rating
                )

                // push() generates a unique child key
                databaseRef.child("reviews").child(tripKey).push().setValue(review)
                    .addOnSuccessListener {
                        // Reset UI after success
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                        reviewInput.text.clear()
                        ratingBar.rating = 0f
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Owner vs visitor UI logic
        if (currentUserEmail == ownerEmail) {
            // The owner shouldn't chat with themselves or rate their own trip
            askButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE

            ratingBar.visibility = View.GONE
            reviewInput.visibility = View.GONE
            submitReviewButton.visibility = View.GONE

            // Owner can delete the trip
            deleteButton.setOnClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete this trip?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteTrip(title)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // A visitor can message the owner and add a review
        } else {
            askButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE

            ratingBar.visibility = View.VISIBLE
            reviewInput.visibility = View.VISIBLE
            submitReviewButton.visibility = View.VISIBLE

            // Open chat screen with owner
            askButton.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverEmail", ownerEmail)
                intent.putExtra("senderEmail", currentUserEmail)
                startActivity(intent)
            }
        }
    }

    // Listen to /reviews/tripKey and refresh the list whenever data changes
    private fun loadReviews() {
        databaseRef.child("reviews").child(tripKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Rebuild list from server snapshot
                    reviewList.clear()
                    for (reviewSnapshot in snapshot.children) {
                        val review = reviewSnapshot.getValue(Review::class.java)
                        if (review != null) {
                            reviewList.add(review)
                        }
                    }
                    // Notify adapter that data set has changed
                    reviewAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TripDetailsActivity, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Delete this trip documents and its associated reviews
    private fun deleteTrip(tripTitle: String) {
        val tripsRef = databaseRef.child("trips")

        // Find trip(s) by title and remove them
        tripsRef.orderByChild("title").equalTo(tripTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Remove each matched trip
                    for (tripSnapshot in snapshot.children) {
                        tripSnapshot.ref.removeValue()
                    }

                    // Also remove all reviews for this trip
                    databaseRef.child("reviews").child(tripKey).removeValue()

                    Toast.makeText(this@TripDetailsActivity, "Trip deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TripDetailsActivity, "Failed to delete trip", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
