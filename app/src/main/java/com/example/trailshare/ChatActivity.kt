package com.example.trailshare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import android.util.Log
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    // Emails of the sender and receiver
    private lateinit var receiverEmail: String
    private lateinit var senderEmail: String

    private lateinit var messageInput: EditText  // Field where the user types a message
    private lateinit var sendButton: Button  // Button to send the message
    private lateinit var messagesRecycler: RecyclerView  // RecyclerView to display messages
    private lateinit var chatTitle: TextView  // TextView to display receiver's email

    private lateinit var databaseRef: DatabaseReference

    // List to store messages
    private val messageList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)  // Load the chat screen layout

        // Get sender and receiver emails from the Intent
        receiverEmail = intent.getStringExtra("receiverEmail") ?: ""
        senderEmail = intent.getStringExtra("senderEmail") ?: ""
        Log.d("ChatDebug", "receiverEmail = $receiverEmail")


        databaseRef = FirebaseDatabase.getInstance().reference

        // Link UI elements from XML to Kotlin variables
        chatTitle = findViewById(R.id.chat_receiver_email)
        messageInput = findViewById(R.id.edit_message)
        sendButton = findViewById(R.id.btn_send)
        messagesRecycler = findViewById(R.id.recycler_chat)

        // Set the chat title to show the receiver's email
        chatTitle.text = receiverEmail

        messagesRecycler.layoutManager = LinearLayoutManager(this)
        messagesRecycler.adapter = ChatAdapter(messageList)

        // Send button click listener
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        // Load existing messages and listen for updates
        loadMessages()

        // Back button click listener
        val backButton = findViewById<ImageButton>(R.id.chat_back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    // Replace characters that are not allowed in Firebase keys
    private fun sanitizeEmail(email: String): String {
        return email.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }

    // Create a unique chat ID for two users based on their emails
    private fun generateChatId(email1: String, email2: String): String {
        val cleanEmail1 = sanitizeEmail(email1)
        val cleanEmail2 = sanitizeEmail(email2)

        return if (cleanEmail1 < cleanEmail2) {
            "${cleanEmail1}_$cleanEmail2"
        } else {
            "${cleanEmail2}_$cleanEmail1"
        }
    }

    // Send a message to Firebase
    private fun sendMessage(messageText: String) {
        val chatId = generateChatId(senderEmail, receiverEmail)

        // Create a map representing the message data
        val message = mapOf(
            "sender" to senderEmail,
            "receiver" to receiverEmail,
            "text" to messageText,
            "timestamp" to System.currentTimeMillis()
        )

        // Push the message into the database under the specific chat ID
        databaseRef.child("chats").child(chatId).push().setValue(message)

        messageInput.text.clear()
    }

    // Load messages from Firebase in order of their timestamp
    private fun loadMessages() {
        val chatId = generateChatId(senderEmail, receiverEmail)

        databaseRef.child("chats").child(chatId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (messageSnapshot in snapshot.children) {
                        val sender = messageSnapshot.child("sender").getValue(String::class.java) ?: ""
                        val text = messageSnapshot.child("text").getValue(String::class.java) ?: ""
                        messageList.add("$sender: $text")
                    }
                    messagesRecycler.adapter?.notifyDataSetChanged()
                    messagesRecycler.scrollToPosition(messageList.size - 1)
                }

                // Handle database read error (optional: show a toast/log)
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}
