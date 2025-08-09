package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Adapter class that connects a list of Trip objects to a RecyclerView
class TripAdapter(
    private val tripList: MutableList<Trip>,
    private val listener: OnTripClickListener
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    // Interface to handle click events on a trip
    interface OnTripClickListener {
        fun onTripClick(trip: Trip)
    }

    // Holds the position of the currently selected trip
    private var selectedPosition = RecyclerView.NO_POSITION

    // ViewHolder class that holds references to the views in a single trip item
    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tripImage: ImageView = itemView.findViewById(R.id.trip_item_image)
        val tripTitle: TextView = itemView.findViewById(R.id.trip_item_title)
        val tripDescription: TextView = itemView.findViewById(R.id.trip_item_description)
    }

    // Called when RecyclerView needs to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(view)
    }

    // Called to bind data from tripList to the views in the ViewHolder
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripList[position]

        holder.tripTitle.text = trip.title
        holder.tripDescription.text = trip.description

        Glide.with(holder.itemView.context)
            .load(trip.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.tripImage)

        holder.itemView.isSelected = (selectedPosition == position)
        holder.itemView.setOnClickListener {
            listener.onTripClick(trip)
            selectTrip(trip)
        }
    }

    // Returns the total number of trips
    override fun getItemCount() = tripList.size

    // Updates the list of trips and refreshes the RecyclerView
    fun updateData(newList: List<Trip>) {
        tripList.clear()
        tripList.addAll(newList)
        notifyDataSetChanged()
    }

    // Selects a trip and refreshes the old and new selected items
    fun selectTrip(trip: Trip) {
        val index = tripList.indexOf(trip)
        if (index != -1 && index != selectedPosition) {
            val oldSelected = selectedPosition
            selectedPosition = index
            notifyItemChanged(oldSelected)
            notifyItemChanged(selectedPosition)
        }
    }
}
