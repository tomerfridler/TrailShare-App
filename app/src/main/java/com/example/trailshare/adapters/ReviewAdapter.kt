package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.example.trailshare.models.Review
import androidx.recyclerview.widget.RecyclerView

// Adapter class that connects a list of Review objects to a RecyclerView
class ReviewAdapter(private val reviewList: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // ViewHolder class: holds references to the views for a single review item
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userEmailText: TextView = itemView.findViewById(R.id.review_user_email)
        val ratingBar: RatingBar = itemView.findViewById(R.id.review_rating_bar)
        val reviewText: TextView = itemView.findViewById(R.id.review_text)
    }

    // Called when RecyclerView needs to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    // Called to bind data from the reviewList to the views in the ViewHolder
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userEmailText.text = review.userEmail
        holder.ratingBar.rating = review.rating
        holder.reviewText.text = review.text
    }

    // Returns the total number of reviews to display in the RecyclerView
    override fun getItemCount(): Int {
        return reviewList.size
    }
}
