package com.example.smartkitchenai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartkitchenai.R
import com.example.smartkitchenai.adapter.ItemAdapter
import com.example.smartkitchenai.data.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class InventoryFragment : Fragment() {

    private lateinit var inventoryRecycler: RecyclerView
    private lateinit var addItemBtn: FloatingActionButton
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)
        inventoryRecycler = view.findViewById(R.id.inventoryRecycler)
        addItemBtn = view.findViewById(R.id.addItemBtn)

        setupRecyclerView()
        addItemBtn.setOnClickListener {
            startActivity(Intent(activity, AddItemActivity::class.java))
        }

        loadItemsFromFirestore()
        return view
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter(itemList)
        inventoryRecycler.layoutManager = LinearLayoutManager(context)
        inventoryRecycler.adapter = itemAdapter

        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        inventoryRecycler.layoutAnimation = controller

        inventoryRecycler.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 250
            removeDuration = 250
        }
    }

    private fun loadItemsFromFirestore() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
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
                    itemAdapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                itemList.clear()
                for (doc in snapshots) {
                    val item = doc.toObject(Item::class.java)
                    item.id = doc.id
                    itemList.add(item)
                }

                itemAdapter.notifyDataSetChanged()
                inventoryRecycler.scheduleLayoutAnimation()
            }
    }

}
