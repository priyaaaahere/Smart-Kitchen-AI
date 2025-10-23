package com.example.smartkitchenai

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartkitchenai.auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val appName = findViewById<TextView>(R.id.appName)
        val tagline = findViewById<TextView>(R.id.tagline)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        logo.startAnimation(scaleUp)
        logo.alpha = 1f

        Handler(Looper.getMainLooper()).postDelayed({
            appName.startAnimation(fadeIn)
            appName.alpha = 1f
        }, 800)

        Handler(Looper.getMainLooper()).postDelayed({
            tagline.startAnimation(fadeIn)
            tagline.alpha = 1f
        }, 800)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 4000)
    }
}