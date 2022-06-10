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
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_reg_akun_pemilik.*

class RegAkunPemilikActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_akun_pemilik)
        auth = Firebase.auth
        btnRegisterRegPemilik.setOnClickListener {
            if (editPasswordRegPemilik.getText().toString() != editCPasswordRegPemilik.getText()
                    .toString()
            ) {
                Toast.makeText(
                    baseContext, "Isi Password dan Confirm Password Berbeda",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.createUserWithEmailAndPassword(
                    editEmailRegisterRegPemilik.getText().toString(), editCPasswordRegPemilik.getText().toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            var user = auth.currentUser
                            val profileUpdates = userProfileChangeRequest {
                                displayName = editNamaPemilikRegPemilik.getText().toString()
                            }
                            user!!.updateProfile(profileUpdates)
                            Toast.makeText(
                                baseContext, "Registrasi Akun Pemilik Berhasil",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, RegWajahPemilikActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                baseContext, "Registrasi Akun tidak berhasil, silakan cek kembali data isian",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}