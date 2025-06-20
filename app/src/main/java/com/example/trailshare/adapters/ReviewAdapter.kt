package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.example.trailshare.models.Review
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val reviewList: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userEmailText: TextView = itemView.findViewById(R.id.review_user_email)
        val ratingBar: RatingBar = itemView.findViewById(R.id.review_rating_bar)
        val reviewText: TextView = itemView.findViewById(R.id.review_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.userEmailText.text = review.userEmail
        holder.ratingBar.rating = review.rating
        holder.reviewText.text = review.text
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}
