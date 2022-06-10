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
                    database.child("users").child(userID).child("registered_face").get().addOnSuccessListener {
                        if(it.value!=null){
                            val sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.clear().apply()
                            editor.putString("map", it.value.toString())
                            editor.apply()
                        }
                        else{
                            val sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.clear().apply()
                        }
                    }.addOnFailureListener{

                    }
                    intent= Intent(this,HomePemilikActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, "Log in gagal, silakan coba lagi",
                        Toast.LENGTH_SHORT).show()
                }
            } }
    }
}