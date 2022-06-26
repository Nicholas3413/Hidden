package com.example.hidden

import android.content.ContentValues
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
        btnLoginLogin.setOnClickListener { auth.signInWithEmailAndPassword(editEmailLogin.getText().toString(), editPasswordLogin.getText().toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var userID=Firebase.auth.currentUser?.uid.toString()
                    database = Firebase.database.reference
                    database.child("users").child(userID).child("user_role").get().addOnSuccessListener {
                        if(it.value.toString()=="pemilik"){
                        intent= Intent(this,HomePemilikActivity::class.java)
                        startActivity(intent)}
                        else{
                            intent= Intent(this,HomeKaryawanActivity::class.java)
                            startActivity(intent)
                        }
                    }.addOnFailureListener{

                    }

                } else {
                    Toast.makeText(baseContext, "Log in gagal, silakan coba lagi",
                        Toast.LENGTH_SHORT).show()
                }
            } }
    }
}