package com.example.hidden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_halaman_utama.*

class HalamanUtamaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_utama)
        btnLoginUtama.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnRegisterUtama.setOnClickListener {
            val intent = Intent(this, RegWajahPemilikActivity::class.java)
            startActivity(intent)
        }
    }
}