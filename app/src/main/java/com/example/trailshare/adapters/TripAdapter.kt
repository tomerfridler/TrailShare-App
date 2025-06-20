package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TripAdapter(
    private val tripList: MutableList<Trip>,
    private val listener: OnTripClickListener
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    interface OnTripClickListener {
        fun onTripClick(trip: Trip)
    }

    private var selectedPosition = RecyclerView.NO_POSITION

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tripImage: ImageView = itemView.findViewById(R.id.trip_item_image)
        val tripTitle: TextView = itemView.findViewById(R.id.trip_item_title)
        val tripDescription: TextView = itemView.findViewById(R.id.trip_item_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(view)
    }

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

    override fun getItemCount() = tripList.size

    fun updateData(newList: List<Trip>) {
        tripList.clear()
        tripList.addAll(newList)
        notifyDataSetChanged()
    }

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
