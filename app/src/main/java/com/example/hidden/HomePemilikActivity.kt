package com.example.hidden

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_pemilik.*

class HomePemilikActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_pemilik)
        auth= Firebase.auth
        var namaUser = auth.currentUser?.displayName.toString()
        txtnamapemilik.setText(namaUser)
        btnLogOutHomePemilik.setOnClickListener {
            val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear().apply()
            Firebase.auth.signOut()
            val intent = Intent(this, HalamanUtamaActivity::class.java)
            startActivity(intent)
        }
        btnCheckInHomePemilik.setOnClickListener {
            val intent = Intent(this, CheckInActivity::class.java)
            startActivity(intent)
        }
    }
}