package com.example.hidden

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_halaman_utama.*

class HalamanUtamaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
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
            val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            if(sharedPreferences.getString("user_role","")=="pemilik"){
                intent= Intent(this,HomePemilikActivity::class.java)
                startActivity(intent)
            }
            else if(sharedPreferences.getString("user_role","")=="karyawan"){
                intent= Intent(this,HomeKaryawanActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(
                    baseContext, "Akun ini belum ter-registrasi sebagai akun pemilik/karyawan.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

//            database = Firebase.database.reference
//            database.child("users").child(userID).child("user_role").get().addOnSuccessListener {
//                if(it.value.toString()=="pemilik"){
//                    intent= Intent(this,HomePemilikActivity::class.java)
//                    startActivity(intent)}
//                else{
//                    intent= Intent(this,HomeKaryawanActivity::class.java)
//                    startActivity(intent)
//                }
//            }.addOnFailureListener{
//
//            }
        }

    }