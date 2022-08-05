package com.example.hidden

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth

        btnLoginLogin.setOnClickListener {
            val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear().apply()
            try {
                auth.signInWithEmailAndPassword(
                    editEmailLogin.getText().toString(),
                    editPasswordLogin.getText().toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            var userID = Firebase.auth.currentUser?.uid.toString()
                            database = Firebase.database.reference
                            database.child("users").child(userID).child("user_role").get()
                                .addOnSuccessListener {
                                    editor.putString("user_role", it.value.toString())
                                    editor.apply()
                                    if (sharedPreferences.getString("user_role", "") == "pemilik") {
                                        intent = Intent(this, HomePemilikActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        intent = Intent(this, HomeKaryawanActivity::class.java)
                                        startActivity(intent)
                                    }
                                }.addOnFailureListener {

                            }
                            database.child("users").child(userID).child("perusahaan_id").get()
                                .addOnSuccessListener {
                                    editor.putString("perusahaan_id", it.value.toString())
                                    editor.apply()
                                    database.child("perusahaan").child(it.value.toString()).get()
                                        .addOnSuccessListener {
                                            editor.putString(
                                                "jam_masuk",
                                                it.child("jam_masuk").value.toString()
                                            )
                                            editor.putString(
                                                "menit_masuk",
                                                it.child("menit_masuk").value.toString()
                                            )
                                            editor.putString(
                                                "jam_pulang",
                                                it.child("jam_pulang").value.toString()
                                            )
                                            editor.putString(
                                                "menit_pulang",
                                                it.child("menit_pulang").value.toString()
                                            )
                                            editor.putString(
                                                "loclamin",
                                                it.child("loclamin").value.toString()
                                            )
                                            editor.putString(
                                                "loclapos",
                                                it.child("loclapos").value.toString()
                                            )
                                            editor.putString(
                                                "loclongmin",
                                                it.child("loclongmin").value.toString()
                                            )
                                            editor.putString(
                                                "loclongpos",
                                                it.child("loclongpos").value.toString()
                                            )
                                            editor.apply()
                                        }
                                }

                        } else {
                            Toast.makeText(
                                baseContext, "Log in gagal, silakan coba lagi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }catch (e:Exception){
                Toast.makeText(
                    baseContext, "Log in gagal, silakan periksa kembali data isian",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}