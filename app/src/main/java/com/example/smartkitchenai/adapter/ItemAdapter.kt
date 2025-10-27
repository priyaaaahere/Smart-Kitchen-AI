package com.example.smartkitchenai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
        val itemDelete: ImageButton = itemView.findViewById(R.id.deleteBtn)
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

        holder.itemDelete.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                currentItem.id?.let { itemId ->
                    db.collection("users")
                        .document(userId)
                        .collection("items")
                        .document(itemId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Item deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Error deleting item", Toast.LENGTH_SHORT).show()
                        }
                } ?: Toast.makeText(holder.itemView.context, "Item ID missing!", Toast.LENGTH_SHORT).show()
            }


        }
    }

    override fun getItemCount() = itemList.size

}
