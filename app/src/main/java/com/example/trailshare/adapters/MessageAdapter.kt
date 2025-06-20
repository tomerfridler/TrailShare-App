package com.example.trailshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val chatPartners: List<String>,
    private val listener: OnChatClickListener
) : RecyclerView.Adapter<MessageAdapter.ChatViewHolder>() {

    interface OnChatClickListener {
        fun onChatClick(partnerEmail: String)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partnerEmailText: TextView = itemView.findViewById(R.id.partner_email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_partner, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val partnerEmail = chatPartners[position]
        holder.partnerEmailText.text = partnerEmail

        holder.itemView.setOnClickListener {
            listener.onChatClick(partnerEmail)
        }
    }

    override fun getItemCount(): Int {
        return chatPartners.size
    }
}
