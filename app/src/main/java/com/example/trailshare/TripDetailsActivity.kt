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

    private lateinit var reviewsRecycler: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<Review>()

    private lateinit var databaseRef: DatabaseReference
    private lateinit var currentUserEmail: String
    private lateinit var tripKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        val title = intent.getStringExtra("trip_title") ?: ""
        val description = intent.getStringExtra("trip_description") ?: ""
        val imageUrl = intent.getStringExtra("trip_imageUrl") ?: ""
        val ownerEmail = intent.getStringExtra("trip_ownerEmail") ?: ""

        val backButton = findViewById<ImageButton>(R.id.trip_details_back_button)
        val tripImage = findViewById<ImageView>(R.id.trip_image)
        val tripTitle = findViewById<TextView>(R.id.trip_location)
        val tripDescription = findViewById<TextView>(R.id.trip_description)
        val ratingBar = findViewById<RatingBar>(R.id.trip_rating_bar)
        val reviewInput = findViewById<EditText>(R.id.trip_review_input)
        val submitReviewButton = findViewById<Button>(R.id.btn_submit_review)
        val askButton = findViewById<Button>(R.id.btn_message)
        val deleteButton = findViewById<Button>(R.id.btn_delete_trip)

        reviewsRecycler = findViewById(R.id.recycler_reviews)
        reviewsRecycler.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviewList)
        reviewsRecycler.adapter = reviewAdapter

        backButton.setOnClickListener {
            finish()
        }

        tripTitle.text = title
        tripDescription.text = description

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(tripImage)

        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        databaseRef = FirebaseDatabase.getInstance().reference
        tripKey = title.replace(" ", "_")

        loadReviews()

        submitReviewButton.setOnClickListener {
            val rating = ratingBar.rating
            val reviewText = reviewInput.text.toString().trim()

            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please write a review!", Toast.LENGTH_SHORT).show()
            } else {
                val review = Review(
                    userEmail = currentUserEmail,
                    text = reviewText,
                    rating = rating
                )

                databaseRef.child("reviews").child(tripKey).push().setValue(review)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                        reviewInput.text.clear()
                        ratingBar.rating = 0f
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        if (currentUserEmail == ownerEmail) {
            askButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE

            ratingBar.visibility = View.GONE
            reviewInput.visibility = View.GONE
            submitReviewButton.visibility = View.GONE

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

        } else {
            askButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE

            ratingBar.visibility = View.VISIBLE
            reviewInput.visibility = View.VISIBLE
            submitReviewButton.visibility = View.VISIBLE

            askButton.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverEmail", ownerEmail)
                intent.putExtra("senderEmail", currentUserEmail)
                startActivity(intent)
            }
        }
    }

    private fun loadReviews() {
        databaseRef.child("reviews").child(tripKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviewList.clear()
                    for (reviewSnapshot in snapshot.children) {
                        val review = reviewSnapshot.getValue(Review::class.java)
                        if (review != null) {
                            reviewList.add(review)
                        }
                    }
                    reviewAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TripDetailsActivity, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteTrip(tripTitle: String) {
        val tripsRef = databaseRef.child("trips")

        tripsRef.orderByChild("title").equalTo(tripTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (tripSnapshot in snapshot.children) {
                        tripSnapshot.ref.removeValue()
                    }

                    // מוחק גם את הביקורות
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
