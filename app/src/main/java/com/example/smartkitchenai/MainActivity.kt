package com.example.smartkitchenai

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smartkitchenai.ui.AIChatFragment
import com.example.smartkitchenai.R
import com.example.smartkitchenai.ui.InventoryFragment
import com.example.smartkitchenai.ui.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.orange_700)

        val db = FirebaseFirestore.getInstance()
        db.collection("test").get()
            .addOnSuccessListener { Toast.makeText(this, "Firestore connected!", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }


        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)

        //default fragment InventoryFragment when the app starts
        if (savedInstanceState == null) {
            loadFragment(InventoryFragment())
        }

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_inventory -> {
                    loadFragment(InventoryFragment())
                    true
                }
                R.id.navigation_ai -> {
                    loadFragment(AIChatFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

    }

    /**
     * A helper function to replace the current fragment in the container.
     * @param fragment The fragment to display.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}