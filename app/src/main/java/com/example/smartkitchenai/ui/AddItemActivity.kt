package com.example.smartkitchenai.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartkitchenai.R
import com.example.smartkitchenai.auth.LoginActivity
import com.example.smartkitchenai.data.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class AddItemActivity : AppCompatActivity() {

    private lateinit var itemNameEdit: EditText
    private lateinit var itemQtyEdit: EditText
    private lateinit var itemExpiryEdit: EditText
    private lateinit var saveItemBtn: Button
    private lateinit var unitSpinner: Spinner
    private val db = FirebaseFirestore.getInstance()
    private val auth= FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_700)


        itemNameEdit = findViewById(R.id.itemNameEdit)
        itemQtyEdit = findViewById(R.id.itemQtyEdit)
        itemExpiryEdit = findViewById(R.id.itemExpiryEdit)
        saveItemBtn = findViewById(R.id.saveItemBtn)
        unitSpinner = findViewById(R.id.unitSpinner)

        val unitAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        )

        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = unitAdapter





        saveItemBtn.setOnClickListener {
            saveItem()
        }

    }

    private fun saveItem() {
        val name = itemNameEdit.text.toString().trim()
        val quantity = itemQtyEdit.text.toString().trim()
        val expiryDate = itemExpiryEdit.text.toString().trim()
        val unit = unitSpinner.selectedItem.toString()

        if (name.isEmpty() || quantity.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val datePattern = Regex("^\\d{2}-\\d{2}-\\d{4}$")
        if (!expiryDate.matches(datePattern)) {
            Toast.makeText(this, "Enter expiry as DD-MM-YYYY", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You are not logged in!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val item = Item(name = name, quantity = quantity, expiryDate = expiryDate, unit = unit)

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