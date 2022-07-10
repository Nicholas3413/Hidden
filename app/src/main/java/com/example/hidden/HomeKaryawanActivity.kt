package com.example.hidden

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_karyawan.*

class HomeKaryawanActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_karyawan)
        auth= Firebase.auth
        var userID=Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        database.child("users").child(userID).child("user_name").get().addOnSuccessListener {
            txtnamakaryawanhomekaryawan.setText(it.value.toString())
        }
        btnLogOutHomeKaryawan.setOnClickListener {
            val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear().apply()
            Firebase.auth.signOut()
            val intent = Intent(this, HalamanUtamaActivity::class.java)
            startActivity(intent)
        }
        btnCheckInHomeKaryawan.setOnClickListener {
            val intent = Intent(this, CheckInActivity::class.java)
            startActivity(intent)
        }
        btnCheckOutHomeKaryawan.setOnClickListener {
            val intent = Intent(this, CheckOutActivity::class.java)
            startActivity(intent)
        }
        btnProfilHomeKaryawan.setOnClickListener {
            val intent = Intent(this, ProfilKaryawanActivity::class.java)
            startActivity(intent)
        }
        btnInfoPerusahaanHomeKaryawan.setOnClickListener {
            val intent = Intent(this, InformasiPerusahaanActivity::class.java)
            startActivity(intent)
        }
        txtnamakaryawanhomekaryawan.setOnClickListener {
            val intent = Intent(this, InformasiAbsensiActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onStart() {
        super.onStart()
        auth= Firebase.auth
        var userID=Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        database.child("users").child(userID).child("registered_face").get().addOnSuccessListener {
            if(it.value!=null){
                val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear().apply()
                editor.putString("map", it.value.toString())
                editor.apply()
            }
            else{
                val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear().apply()
            }
        }.addOnFailureListener{

        }
    }
}