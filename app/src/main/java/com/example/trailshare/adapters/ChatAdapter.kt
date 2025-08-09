package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class that connects the message list data to the RecyclerView
class ChatAdapter(private val messageList: List<String>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // ViewHolder class that holds the views for each individual item (message) in the list
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.message_text)
    }

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    // Called to bind data (a message) to the ViewHolder at a specific position
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.messageText.text = messageList[position]
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return messageList.size
    }
}
