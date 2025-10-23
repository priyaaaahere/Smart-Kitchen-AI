package com.example.smartkitchenai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartkitchenai.R
import com.example.smartkitchenai.data.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ItemAdapter(private val itemList: MutableList<Item>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemQty: TextView = itemView.findViewById(R.id.itemQty)
        val itemExpiry: TextView = itemView.findViewById(R.id.itemExpiry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.itemName.text = currentItem.name
        holder.itemQty.text = "Qty: ${currentItem.quantity} ${currentItem.unit}"
        holder.itemExpiry.text = "Expires: ${currentItem.expiryDate}"

        holder.itemView.setOnLongClickListener {
            val db = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && currentItem.id != null) {
                db.collection("users").document(userId).collection("items")
                    .document(currentItem.id!!)
                    .delete()
                    .addOnSuccessListener {
                        itemList.removeAt(position)
                        notifyItemRemoved(position)
                    }
            }
            true
        }
    }

    override fun getItemCount() = itemList.size
}
