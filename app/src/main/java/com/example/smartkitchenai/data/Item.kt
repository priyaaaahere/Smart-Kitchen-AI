package com.example.smartkitchenai.data

data class Item(
    var id: String? = null,
    val name: String = "",
    val quantity: String = "",
    val expiryDate: String = "",
    val unit: String = ""
)