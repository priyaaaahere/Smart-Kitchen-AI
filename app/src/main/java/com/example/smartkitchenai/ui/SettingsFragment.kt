package com.example.smartkitchenai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.smartkitchenai.R
import com.example.smartkitchenai.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val logOutButton = view.findViewById<Button>(R.id.logoutBtn)
        val fb = FirebaseAuth.getInstance()

        logOutButton.setOnClickListener {
            // logout the user
            fb.signOut()
            // move to login page
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }


        return view

    }
}