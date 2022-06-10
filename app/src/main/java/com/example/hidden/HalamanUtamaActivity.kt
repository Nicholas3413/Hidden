package com.example.hidden

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_halaman_utama.*

class HalamanUtamaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_utama)

        btnLoginUtama.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnRegisterUtama.setOnClickListener {
            val intent = Intent(this, RegAkunPemilikActivity::class.java)
            startActivity(intent)
        }
    }
    public override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null){
            intent= Intent(this,HomePemilikActivity::class.java)
            startActivity(intent)
        }
    }
}