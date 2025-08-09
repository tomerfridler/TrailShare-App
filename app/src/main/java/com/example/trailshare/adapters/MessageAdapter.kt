package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class that connects the list of chat partners to the RecyclerView
class MessageAdapter(
    private val chatPartners: List<String>,
    private val listener: OnChatClickListener
) : RecyclerView.Adapter<MessageAdapter.ChatViewHolder>() {

    // Interface to define the click event for a chat partner
    interface OnChatClickListener {
        fun onChatClick(partnerEmail: String)
    }

    // ViewHolder class that holds references to the views in a single item layout
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partnerEmailText: TextView = itemView.findViewById(R.id.partner_email)
    }

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_partner, parent, false)
        return ChatViewHolder(view)
    }

    // Called to bind the data to the ViewHolder at a specific position
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val partnerEmail = chatPartners[position]
        holder.partnerEmailText.text = partnerEmail

        holder.itemView.setOnClickListener {
            listener.onChatClick(partnerEmail)
        }
    }

    // Returns the total number of chat partners
    override fun getItemCount(): Int {
        return chatPartners.size
    }
}
