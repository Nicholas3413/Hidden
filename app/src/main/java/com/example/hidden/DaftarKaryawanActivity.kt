package com.example.hidden

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_daftar_karyawan.*

class DaftarKaryawanActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    var list=ArrayList<Anggotas>()
    private lateinit var perusahaanID:String
    private lateinit var auth: FirebaseAuth
//    var listUsers=ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_karyawan)
        database = Firebase.database.reference
        var userID= Firebase.auth.currentUser?.uid.toString()
        database.child("users").child(userID).child("perusahaan_id").get().addOnSuccessListener {
            perusahaanID=it.value.toString()
            val usersRef = database.child("perusahaan").child(perusahaanID).child("anggota")
            val valueEventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count=0
                    for (ds in dataSnapshot.children) {
                        val uid = ds.key
                        Log.d("listanggota", uid!!)
                        database.child("perusahaan").child(perusahaanID).child("anggota").child(uid.toString()).get().addOnSuccessListener {
                            var user_id= it.child("user_id").value.toString()
                            database.child("users").child(user_id).get().addOnSuccessListener {
                                var nama_user=it.child("user_name").value.toString()
                                var email_user=it.child("email_user").value.toString()
                                var gambar_user=it.child("gambar_user").value.toString()
                                Log.v("gambar_user",gambar_user)
                                list.add(Anggotas(nama_user,email_user,gambar_user))
                                mRecyclerView.setHasFixedSize(true)
                                mRecyclerView.layoutManager= LinearLayoutManager(this@DaftarKaryawanActivity)
                                val adapter=AdapterRecyclerView(list)
                                adapter.notifyDataSetChanged()
                                mRecyclerView.adapter=adapter
                                adapter.setOnItemClickListener(object:AdapterRecyclerView.onItemClickListener{
                                    override fun onItemClick(position: Int) {
                                        Log.v("hello",list[position].toString())

                                    }
                                })
                            }
                            Log.v("user_id",user_id)

                        }.addOnFailureListener {
                        }
                        count=count+1
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            Log.v("list",list.toString())
            usersRef.addListenerForSingleValueEvent(valueEventListener)
        }




    }
}