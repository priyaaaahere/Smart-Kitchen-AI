package com.example.smartkitchenai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smartkitchenai.R
import com.example.smartkitchenai.auth.LoginActivity
import com.example.smartkitchenai.data.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var emailText: TextView
    private lateinit var totalItemsText: TextView
    private lateinit var expiredItemsText: TextView
    private lateinit var appVersionText: TextView
    private lateinit var footerText: TextView
    private lateinit var logoutButton: Button

    private val itemList = mutableListOf<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        emailText = view.findViewById(R.id.emailText)
        totalItemsText = view.findViewById(R.id.totalItemsText)
        expiredItemsText = view.findViewById(R.id.expiredItemsText)
        appVersionText = view.findViewById(R.id.appVersionText)
        footerText = view.findViewById(R.id.footerText)
        logoutButton = view.findViewById(R.id.logoutBtn)

        // Show email
        val user = auth.currentUser
        emailText.text = "Email: ${user?.email}" ?: "Email: No email"

        // Show app version
        appVersionText.text = "App Version: ${requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName}"

        // Load inventory stats
        loadInventoryStats()

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        footerText.text = "Made with 💓 by Priya"

        return view
    }

    private fun loadInventoryStats() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("items")
            .get()
            .addOnSuccessListener { snapshot ->
                itemList.clear()
                for (doc in snapshot.documents) {
                    val item = doc.toObject(Item::class.java)
                    item?.id = doc.id
                    if (item != null) itemList.add(item)
                }

                totalItemsText.text = "Total Items: ${itemList.size}"
                expiredItemsText.text = "Expired Items: ${countExpiredItems()}"
            }
            .addOnFailureListener {
                totalItemsText.text = "Total Items: 0"
                expiredItemsText.text = "Expired Items: 0"
            }
    }

    private fun countExpiredItems(): Int {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = Calendar.getInstance().time
        return itemList.count { item ->
            try {
                val expiryDate = sdf.parse(item.expiryDate ?: "")
                expiryDate != null && expiryDate.before(today)
            } catch (e: Exception) {
                false
            }
        }
    }
}
