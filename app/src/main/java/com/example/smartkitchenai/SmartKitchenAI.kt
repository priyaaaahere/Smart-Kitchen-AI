package com.example.smartkitchenai

import android.app.Application
import com.google.firebase.FirebaseApp

class SmartKitchenAI: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}