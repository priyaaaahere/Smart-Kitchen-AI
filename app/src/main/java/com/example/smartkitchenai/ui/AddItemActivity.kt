package com.example.smartkitchenai.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartkitchenai.R
import com.example.smartkitchenai.data.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddItemActivity : AppCompatActivity() {

    private lateinit var itemNameEdit: EditText
    private lateinit var itemQtyEdit: EditText
    private lateinit var itemExpiryEdit: EditText
    private lateinit var saveItemBtn: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth= FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        itemNameEdit = findViewById(R.id.itemNameEdit)
        itemQtyEdit = findViewById(R.id.itemQtyEdit)
        itemExpiryEdit = findViewById(R.id.itemExpiryEdit)
        saveItemBtn = findViewById(R.id.saveItemBtn)




        saveItemBtn.setOnClickListener {
            saveItem()
        }

    }

    private fun saveItem() {
        val name = itemNameEdit.text.toString().trim()
        val quantity = itemQtyEdit.text.toString().trim()
        val expiryDate = itemExpiryEdit.text.toString().trim()

        if (name.isEmpty() || quantity.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val item = Item(name = name, quantity = quantity, expiryDate = expiryDate)

        db.collection("users").document(userId).collection("items")
            .add(item)
            .addOnSuccessListener {
                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
