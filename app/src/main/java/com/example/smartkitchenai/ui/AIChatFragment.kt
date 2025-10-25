package com.example.smartkitchenai.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartkitchenai.R
import com.example.smartkitchenai.data.Item
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIChatFragment : Fragment() {

    private val key = "AIzaSyAbdmAR_mCE67foExIJf9sNVZIhbzQUO7U"

    private lateinit var messageContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val itemList = mutableListOf<Item>()

    // Keep one Gemini chat session active
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = key
        )
    }
    private val chat by lazy { model.startChat() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai_chat, container, false)

        messageContainer = view.findViewById(R.id.messageContainer)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)

        // Load inventory and then start chat
        loadItemsFromFirestore {
            val firstPrompt = generateInventoryPrompt()
            sendMessageToGemini(firstPrompt, showAsUser = false)
        }

        sendButton.setOnClickListener {
            val userText = messageInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                addMessage(userText, isUser = true)
                messageInput.text.clear()
                sendMessageToGemini(userText, showAsUser = false)
            }
        }

        return view
    }

    /**
     * Adds message bubble to chat UI
     */
    private fun addMessage(message: String, isUser: Boolean) {
        val layoutId = if (isUser) R.layout.item_user_message else R.layout.item_ai_message
        val view = layoutInflater.inflate(layoutId, messageContainer, false)
        val textView = view.findViewById<TextView>(if (isUser) R.id.userMessage else R.id.aiMessage)
        textView.text = message
        messageContainer.addView(view)

        // Auto scroll to bottom
        messageContainer.post {
            view.rootView.findViewById<ScrollView>(R.id.chatScrollView)?.fullScroll(View.FOCUS_DOWN)
        }
    }

    /**
     * Sends a message or prompt to Gemini and displays the response
     */
    private fun sendMessageToGemini(prompt: String, showAsUser: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = chat.sendMessage(prompt)
                val reply = response.text ?: "I’m not sure how to respond to that."

                withContext(Dispatchers.Main) {
                    addMessage(reply, isUser = false)
                    Log.d("GeminiAI", "Response: $reply")
                }

            } catch (e: ResponseStoppedException) {
                withContext(Dispatchers.Main) {
                    addMessage("Response stopped unexpectedly. Try again!", isUser = false)
                }
            } catch (e: Exception) {
                Log.e("GeminiAI", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    addMessage("Error: Unable to get a response from AI.", isUser = false)
                }
            }
        }
    }

    /**
     * Loads user's inventory from Firestore
     * Calls onComplete() after loading
     */
    private fun loadItemsFromFirestore(onComplete: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).collection("items")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    itemList.clear()
                    return@addSnapshotListener
                }

                itemList.clear()
                for (doc in snapshots) {
                    val item = doc.toObject(Item::class.java)
                    item.id = doc.id
                    itemList.add(item)
                }

                Log.d("Firestore", "Loaded ${itemList.size} items from Firestore")
                onComplete()
            }
    }

    /**
     * Generates the first Gemini prompt based on inventory
     */
    private fun generateInventoryPrompt(): String {
        if (itemList.isEmpty()) {
            return """
                You are SmartKitchenAI, an intelligent cooking assistant.
                Currently, the user's inventory is empty.
                Greet the user warmly and say:
                "Hey, what do you want to cook today?"
            """.trimIndent()
        }

        val itemDetails = itemList.joinToString(separator = "\n") { item ->
            """
            - Name: ${item.name}
              Quantity: ${item.quantity} ${item.unit}
              Expiry Date: ${item.expiryDate.ifEmpty { "N/A" }}
            """.trimIndent()
        }

        return """
            You are SmartKitchenAI — an expert virtual chef and kitchen manager.
            The user has the following inventory items:

            $itemDetails

            Your task:
            - The user will chat with you about what they want to cook.
            - Check the inventory and tell whether the dish can be made using these ingredients.
            - If some items are missing, mention them and suggest substitutes if possible.
            - Make sure to avoid expired items and consider quantities.
            - Be friendly, helpful, and concise.
            
            Start by greeting the user warmly with:
            "Hey, what do you want to cook today?"
        """.trimIndent()
    }
}