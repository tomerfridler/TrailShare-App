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
    private val chatPartners = mutableSetOf<String>()

    private lateinit var noMessagesText: TextView

    private lateinit var databaseRef: DatabaseReference
    private lateinit var currentUserEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        val backButton = findViewById<ImageButton>(R.id.messages_back_button)
        backButton.setOnClickListener {
            finish()
        }

        noMessagesText = findViewById(R.id.no_messages_text)

        recyclerView = findViewById(R.id.recycler_messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(chatPartners.toList(), this)
        recyclerView.adapter = adapter

        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        Log.d("MessagesDebug", "currentUserEmail = $currentUserEmail")

        databaseRef = FirebaseDatabase.getInstance().reference

        loadMessages()
    }

    private fun sanitizeEmail(email: String): String {
        return email.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }

    private fun loadMessages() {
        val sanitizedEmail = sanitizeEmail(currentUserEmail)

        databaseRef.child("chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartners.clear()

                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key ?: ""

                        if (chatId.contains(sanitizedEmail)) {
                            for (messageSnapshot in chatSnapshot.children) {
                                val sender = messageSnapshot.child("sender").getValue(String::class.java) ?: ""
                                val receiver = messageSnapshot.child("receiver").getValue(String::class.java) ?: ""

                                val partnerEmail = if (sender == currentUserEmail) {
                                    receiver
                                } else {
                                    sender
                                }

                                chatPartners.add(partnerEmail)
                            }
                        }
                    }

                    if (chatPartners.isEmpty()) {
                        noMessagesText.visibility = TextView.VISIBLE
                        recyclerView.visibility = RecyclerView.GONE
                    } else {
                        noMessagesText.visibility = TextView.GONE
                        recyclerView.visibility = RecyclerView.VISIBLE
                    }

                    adapter = MessageAdapter(chatPartners.toList(), this@MessagesActivity)
                    recyclerView.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessagesDebug", "Failed to load messages: ${error.message}")
                }
            })
    }


    override fun onChatClick(partnerEmail: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("receiverEmail", partnerEmail)
        intent.putExtra("senderEmail", currentUserEmail)
        startActivity(intent)
    }
}
