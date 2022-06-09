package com.example.hidden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                startActivity(Intent(applicationContext, HalamanUtamaActivity::class.java))
            }
        }, 3000)
    }
}