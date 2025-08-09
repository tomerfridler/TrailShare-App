package com.example.trailshare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MessagesActivity : AppCompatActivity(), MessageAdapter.OnChatClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val chatPartners = mutableSetOf<String>() // Stores unique chat partner emails

    private lateinit var noMessagesText: TextView // "No messages" text

    private lateinit var databaseRef: DatabaseReference
    private lateinit var currentUserEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages) // Set the layout for this screen

        // Back button setup - closes this activity
        val backButton = findViewById<ImageButton>(R.id.messages_back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Reference to the "No messages" text
        noMessagesText = findViewById(R.id.no_messages_text)

        // Setup RecyclerView to display chat partners
        recyclerView = findViewById(R.id.recycler_messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(chatPartners.toList(), this)  // Pass the chat list to the adapter
        recyclerView.adapter = adapter


        // Get the current user's email from Firebase Authentication
        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        Log.d("MessagesDebug", "currentUserEmail = $currentUserEmail")

        // Get reference to Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().reference

        // Load chat list from the database
        loadMessages()
    }

    // Replace special characters in email so it can be used as a Firebase key
    private fun sanitizeEmail(email: String): String {
        return email.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }

    // Load all chat partners for the current user from Firebase
    private fun loadMessages() {
        val sanitizedEmail = sanitizeEmail(currentUserEmail)

        databaseRef.child("chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartners.clear()   // Clear the old list

                    // Loop through all chats
                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: ""

                        // If the chat belongs to the current user
                        if (chatId.contains(sanitizedEmail)) {
                            // Loop through all messages in this chat
                            for (messageSnapshot in chatSnapshot.children) {
                                val sender = messageSnapshot.child("sender").getValue(String::class.java) ?: ""
                                val receiver = messageSnapshot.child("receiver").getValue(String::class.java) ?: ""

                                // Determine the other participant in the chat
                                val partnerEmail = if (sender == currentUserEmail) {
                                    receiver
                                } else {
                                    sender
                                }

                                chatPartners.add(partnerEmail)  // Add to the list
                            }
                        }
                    }

                    // Show "No messages" text if list is empty, otherwise show chat list
                    if (chatPartners.isEmpty()) {
                        noMessagesText.visibility = TextView.VISIBLE
                        recyclerView.visibility = RecyclerView.GONE
                    } else {
                        noMessagesText.visibility = TextView.GONE
                        recyclerView.visibility = RecyclerView.VISIBLE
                    }

                    // Update the adapter with the new list
                    adapter = MessageAdapter(chatPartners.toList(), this@MessagesActivity)
                    recyclerView.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessagesDebug", "Failed to load messages: ${error.message}")
                }
            })
    }


    // Handle click on a chat partner → open ChatActivity
    override fun onChatClick(partnerEmail: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("receiverEmail", partnerEmail)
        intent.putExtra("senderEmail", currentUserEmail)
        startActivity(intent)
    }
}
