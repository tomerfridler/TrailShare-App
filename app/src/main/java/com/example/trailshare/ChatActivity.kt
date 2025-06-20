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

    private lateinit var receiverEmail: String
    private lateinit var senderEmail: String

    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var chatTitle: TextView

    private lateinit var databaseRef: DatabaseReference
    private val messageList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        receiverEmail = intent.getStringExtra("receiverEmail") ?: ""
        senderEmail = intent.getStringExtra("senderEmail") ?: ""
        Log.d("ChatDebug", "receiverEmail = $receiverEmail")


        databaseRef = FirebaseDatabase.getInstance().reference

        chatTitle = findViewById(R.id.chat_receiver_email)
        messageInput = findViewById(R.id.edit_message)
        sendButton = findViewById(R.id.btn_send)
        messagesRecycler = findViewById(R.id.recycler_chat)

        chatTitle.text = receiverEmail

        messagesRecycler.layoutManager = LinearLayoutManager(this)
        messagesRecycler.adapter = ChatAdapter(messageList)

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        loadMessages()

        val backButton = findViewById<ImageButton>(R.id.chat_back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun sanitizeEmail(email: String): String {
        return email.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }

    private fun generateChatId(email1: String, email2: String): String {
        val cleanEmail1 = sanitizeEmail(email1)
        val cleanEmail2 = sanitizeEmail(email2)

        return if (cleanEmail1 < cleanEmail2) {
            "${cleanEmail1}_$cleanEmail2"
        } else {
            "${cleanEmail2}_$cleanEmail1"
        }
    }

    private fun sendMessage(messageText: String) {
        val chatId = generateChatId(senderEmail, receiverEmail)

        val message = mapOf(
            "sender" to senderEmail,
            "receiver" to receiverEmail,
            "text" to messageText,
            "timestamp" to System.currentTimeMillis()
        )

        databaseRef.child("chats").child(chatId).push().setValue(message)

        messageInput.text.clear()
    }

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

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}
