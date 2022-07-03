package com.example.hidden

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_pemilik.*
import kotlin.text.Typography.registered

class HomePemilikActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_pemilik)
        btnLogOutHomePemilik.setOnClickListener {
            val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear().apply()
            val sharedPreferences2 = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val editor2 = sharedPreferences2.edit()
            editor2.clear().apply()
            Firebase.auth.signOut()
            val intent = Intent(this, HalamanUtamaActivity::class.java)
            startActivity(intent)
        }
        btnCheckInHomePemilik.setOnClickListener {
            val intent = Intent(this, CheckInActivity::class.java)
            startActivity(intent)
        }
        btnEditPerusahaanHomePemilik.setOnClickListener {
            val intent = Intent(this, EditPerusahaanActivity::class.java)
            startActivity(intent)
        }
        btnInformasiHomePemilik.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pilih Informasi:")
            val names = arrayOf(
                "Informasi Anggota",
                "Informasi Perusahaan"
            )
            builder.setItems(
                names
            ) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 ->{val intent = Intent(this, DaftarKaryawanActivity::class.java)
                        startActivity(intent)}
                    1 -> {val intent = Intent(this, InformasiPerusahaanActivity::class.java)
                        startActivity(intent)}
                }
            }
            var dialog = builder.create()
            dialog.show()
        }


        btnCheckOutHomePemilik.setOnClickListener {
            val intent = Intent(this, CheckOutActivity::class.java)
            startActivity(intent)
        }

        btnAbsensiHarianHomePemilik.setOnClickListener {
            val intent = Intent(this, AbsensiHarianActivity::class.java)
            startActivity(intent)
        }
        imageButton8.setOnClickListener {
            val intent = Intent(this, ProfilPemilikActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        auth= Firebase.auth
        var namaUser = auth.currentUser?.displayName.toString()
        txtnamapemilik.setText(namaUser)
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