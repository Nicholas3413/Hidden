package com.example.hidden

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_daftarkan_karyawan.*
import kotlinx.android.synthetic.main.activity_reg_akun_pemilik.*
import kotlin.text.Typography.registered

class RegAkunPemilikActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_akun_pemilik)
        auth = Firebase.auth
        database = Firebase.database.reference
        btnRegisterRegPemilik.setOnClickListener {
            if (editPasswordRegPemilik.getText().toString() != editCPasswordRegPemilik.getText()
                    .toString()
            ) {
                Toast.makeText(
                    baseContext, "Isi Password dan Confirm Password Berbeda",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                try {
                    auth.createUserWithEmailAndPassword(
                        editEmailRegisterRegPemilik.getText().toString(),
                        editCPasswordRegPemilik.getText().toString()
                    )
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                var user = auth.currentUser
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = editNamaPemilikRegPemilik.getText().toString()
                                }
                                user!!.updateProfile(profileUpdates)
                                database.child("users").child(user.uid).child("user_name")
                                    .setValue(editNamaPemilikRegPemilik.getText().toString())
                                database.child("users").child(user.uid).child("user_role")
                                    .setValue("pemilik")
                                database.child("users").child(user.uid).child("email_user")
                                    .setValue(editEmailRegisterRegPemilik.text.toString())

                                Toast.makeText(
                                    baseContext, "Registrasi Akun Pemilik Berhasil",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, RegWajahPemilikActivity::class.java)
                                startActivity(intent)
                            } else {
                                auth.fetchSignInMethodsForEmail(editEmailRegisterRegPemilik.text.toString()).addOnSuccessListener {
                                    auth.signInWithEmailAndPassword(editEmailRegisterRegPemilik.getText().toString(),
                                        editCPasswordRegPemilik.getText().toString()).addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            val userPemilik = auth.currentUser
//                                            var perusahaanId = ""
                                            database = Firebase.database.reference
                                            database.child("users").child(userPemilik!!.uid).child("user_role").get().addOnSuccessListener {
                                                if(it.value.toString()=="null"||it.value.toString()==""){
                                                    Toast.makeText(baseContext, "Akun belum terdaftar sebagai jenis akun mana pun",
                                                        Toast.LENGTH_SHORT).show()
                                                    val profileUpdates = userProfileChangeRequest {
                                                        displayName = editNamaPemilikRegPemilik.getText().toString()
                                                    }
                                                    var user = auth.currentUser
                                                    user!!.updateProfile(profileUpdates)
                                                    database.child("users").child(user.uid).child("user_name")
                                                        .setValue(editNamaPemilikRegPemilik.getText().toString())
                                                    database.child("users").child(user.uid).child("user_role")
                                                        .setValue("pemilik")
                                                    database.child("users").child(user.uid).child("email_user")
                                                        .setValue(editEmailRegisterRegPemilik.text.toString())
                                                    Toast.makeText(
                                                        baseContext, "Registrasi Akun Pemilik Berhasil",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    val intent = Intent(this, RegWajahPemilikActivity::class.java)
                                                    startActivity(intent)
                                                }
                                                else{
                                                    Toast.makeText(baseContext, "Akun telah terdaftar",
                                                        Toast.LENGTH_SHORT).show()
                                                }

                                            }

                                        } else {
                                            Toast.makeText(baseContext, "Password ke akun salah",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    Toast.makeText(
                                        baseContext, "Akun telah terdaftarkan sebelumnya",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        baseContext,
                                        "Registrasi Akun tidak berhasil, silakan cek kembali data isian",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

//                                Toast.makeText(
//                                    baseContext,
//                                    "Registrasi Akun tidak berhasil, silakan cek kembali data isian",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        }
                }catch (e:Exception){
                    Toast.makeText(
                        baseContext, "Registrasi gagal, silakan periksa kembali data isian",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }
}