package com.example.smartkitchenai.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.smartkitchenai.R
import com.example.smartkitchenai.data.Item
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class AIChatFragment : Fragment() {

    private lateinit var messageContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatScrollView: ScrollView
    private lateinit var suggestionScrollView: HorizontalScrollView
    private lateinit var suggestionChips: List<TextView>
    private lateinit var startChatLayout: LinearLayout
    private lateinit var startChatButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val itemList = mutableListOf<Item>()

    private val key = "AIzaSyAbdmAR_mCE67foExIJf9sNVZIhbzQUO7U"
    private val model by lazy { GenerativeModel(modelName = "gemini-2.5-pro", apiKey = key) }
    private val chat by lazy { model.startChat() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_ai_chat, container, false)

        // UI elements
        messageContainer = view.findViewById(R.id.messageContainer)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)
        chatScrollView = view.findViewById(R.id.chatScrollView)
        suggestionScrollView = view.findViewById(R.id.suggestionScrollView)
        startChatLayout = view.findViewById(R.id.startChatLayout)
        startChatButton = view.findViewById(R.id.startChatButton)

        suggestionChips = listOf(
            view.findViewById(R.id.suggestionChip1),
            view.findViewById(R.id.suggestionChip2),
            view.findViewById(R.id.suggestionChip3)
        )

        suggestionChips.forEach { chip ->
            chip.setOnClickListener {
                val text = chip.text.toString()
                addMessage(text, isUser = true)
                sendMessageToGemini(text)
            }
        }

        sendButton.setOnClickListener {
            val userText = messageInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                addMessage(userText, isUser = true)
                messageInput.text.clear()
                sendMessageToGemini(userText)
            }
        }

        startChatLayout.visibility = View.VISIBLE
        startChatButton.setOnClickListener {
            startChatLayout.visibility = View.GONE
            loadItemsFromFirestore {
                val prompt = generateInventoryPrompt()
                sendMessageToGemini(prompt)
            }
        }

        return view
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val layoutId = if (isUser) R.layout.item_user_message else R.layout.item_ai_message
        val view = layoutInflater.inflate(layoutId, messageContainer, false)
        val textView = view.findViewById<TextView>(if (isUser) R.id.userMessage else R.id.aiMessage)
        textView.text = message
        messageContainer.addView(view)

        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun addErrorMessage(message: String, prompt: String) {
        val errorLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_ai_message)
        }

        val errorText = TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            textSize = 15f
        }

        val reloadButton = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_reload)
            background = null
            setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange_700))
            setPadding(24, 0, 0, 0)
            setOnClickListener {
                messageContainer.removeView(errorLayout)
                sendMessageToGemini(prompt)
            }
        }

        errorLayout.addView(errorText)
        errorLayout.addView(reloadButton)

        messageContainer.addView(errorLayout)
        chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun sendMessageToGemini(prompt: String) {
        // Show typing indicator
        val typingView = layoutInflater.inflate(R.layout.item_ai_typing, messageContainer, false)
        messageContainer.addView(typingView)
        val dot1 = typingView.findViewById<View>(R.id.dot1)
        val dot2 = typingView.findViewById<View>(R.id.dot2)
        val dot3 = typingView.findViewById<View>(R.id.dot3)

        val anim1 = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce_dot)
        val anim2 = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce_dot).apply { startOffset = 200 }
        val anim3 = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce_dot).apply { startOffset = 400 }

        dot1.startAnimation(anim1)
        dot2.startAnimation(anim2)
        dot3.startAnimation(anim3)

        chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = chat.sendMessage(prompt)
                val reply = response.text ?: "I’m not sure how to respond to that."

                withContext(Dispatchers.Main) {
                    // Remove typing animation
                    messageContainer.removeView(typingView)
                    addMessage(reply, isUser = false)
                }

            } catch (e: ResponseStoppedException) {
                withContext(Dispatchers.Main) {
                    messageContainer.removeView(typingView)
                    addErrorMessage("Response stopped unexpectedly.", prompt)
                }
            } catch (e: Exception) {
                Log.d("gemini", e.message.toString())
                withContext(Dispatchers.Main) {
                    messageContainer.removeView(typingView)
                    addErrorMessage("Error: Unable to get a response from AI.", prompt)
                }
            }
        }
    }

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

                itemList.clear()
                snapshots?.forEach { doc ->
                    val item = doc.toObject(Item::class.java)
                    item.id = doc.id
                    itemList.add(item)
                }
                onComplete()
            }
    }

    private fun generateInventoryPrompt(): String {
        if (itemList.isEmpty()) {
            return """ You are SmartKitchenAI, an intelligent cooking assistant. Currently, the user's inventory is empty. Greet the user warmly and say: "Hey, what do you want to cook today?" """.trimIndent()
        }
        val itemDetails = itemList.joinToString(separator = "\n") { item ->
            """ - Name: ${item.name} Quantity: ${item.quantity} ${item.unit} Expiry Date: ${item.expiryDate.ifEmpty { "N/A" }} """.trimIndent()
        }
        return """ You are SmartKitchenAI — an expert virtual chef and kitchen manager. The user has the following inventory items: $itemDetails Your task: - The user will chat with you about what they want to cook. - Check the inventory and tell whether the dish can be made using these ingredients. - If some items are missing, mention them and suggest substitutes if possible. - Make sure to avoid expired items and consider quantities. - Be friendly, helpful, and concise. Start by greeting the user warmly with: "Hey, what do you want to cook today?" 
            
            You need to manage the inventory also. track expired items (use today's date).
            Answer all the questions related to inventory, cooking and recipes.
            
        """.trimIndent()
    }
}
