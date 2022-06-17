package com.example.hidden

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_reg_perusahaan.*
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.File
import java.util.*


class RegPerusahaanActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var REQUEST_CODE=100
    private var storage = Firebase.storage
    private var imageUrl:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_perusahaan)
        val mTimePicker: TimePickerDialog
        val nTimePicker: TimePickerDialog
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)
        var totjammasuk=0
        var totjampulang=0

        mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjammasuk=(hourOfDay*60+minute)*60
                txtWaktuJamMasukRegPerusahaan.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)
        nTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjampulang=(hourOfDay*60+minute)*60
                txtWaktuJamPulangRegPerusahaan.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)
        btnAturJamMasukRegPerusahaan.setOnClickListener { v ->
            mTimePicker.show()
        }
        btnAturJamPulangRegPerusahaan.setOnClickListener {
                v-> nTimePicker.show()
        }
        btnLokasiRegPerusahaan.setOnClickListener {
            val intent = Intent(this, PilihLokasiActivity::class.java)
            startActivity(intent)
        }
        btnPilihGambarRegPerusahaan.setOnClickListener {
            openGalleryForImage()
        }
        btnRegisterPerusahaanRegPerusahaan.setOnClickListener {
            auth= Firebase.auth
            var userID=Firebase.auth.currentUser?.uid.toString()
            writeNewPerusahaan(userID,editNamaPerusahaanRegPerusahaan.getText().toString(),totjammasuk.toLong(),totjampulang.toLong())
            val intent = Intent(this, HomePemilikActivity::class.java)
            startActivity(intent)
        }


    }
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            imgPreviewPilihGambarRegPerusahaan.setImageURI(data?.data)

            var storageRef = storage.reference
            var imagesRef: StorageReference? = storageRef.child("images")
            var spaceRef = storageRef.child("images/space.jpg")
            var uploadTask = spaceRef.putFile(data?.data!!)
            val ref = storageRef.child("images/space.jpg")
// Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                Log.v("upload","gagal")
            }.addOnSuccessListener { taskSnapshot ->
                Log.v("upload","berhasil")
                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        imageUrl=downloadUri.toString()
                        Log.v("upload",downloadUri.toString())
                        Glide.with(this).load(imageUrl).into(tesimgPreviewPilihGambarRegPerusahaan)

                    } else {

                    }
                }
            }

        }
    }


    fun writeNewPerusahaan(userId: String, name: String,jam_masuk: Long?,jam_pulang: Long? ) {
        database = Firebase.database.reference
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var randomstring=""
        for (i in 1..6) {
            val random1 = (0..charPool.size).shuffled().last()
            var tempchar=charPool[random1]
            randomstring=randomstring+tempchar
        }
        Log.v("randomstring",randomstring)

        val sharedPreferences = getSharedPreferences("Location", Context.MODE_PRIVATE)
        var loclapos=sharedPreferences.getString("new_latitude_pos","")
        var loclamin=sharedPreferences.getString("new_latitude_min","")
        var loclongpos=sharedPreferences.getString("new_longitude_pos","")
        var loclongmin=sharedPreferences.getString("new_longitude_min","")
        Log.v("4loc",loclapos.toString()+loclamin.toString()+loclongpos.toString()+loclongmin.toString())
        val perusahaan = Perusahaan(name,jam_masuk,jam_pulang,editEmailPerusahaanRegPerusahaan.getText().toString()
            ,editAlamatPerusahaanRegPerusahaan.getText().toString(),editNoTelpPerusahaanRegPerusahaan.getText().toString()
            ,editTahunBerdiriPerusahaanRegPerusahaan.getText().toString(),editBidangPerusahaanRegPerusahaan.getText().toString()
            ,userId,imageUrl,loclapos,loclamin,loclongpos,loclongmin)
        database.child("perusahaan").child(randomstring).setValue(perusahaan)
        database.child("users").child(userId).child("perusahaan_id").setValue(randomstring)
    }
    @IgnoreExtraProperties
    data class Perusahaan(val nama_perusahaan: String? = null, val jam_masuk: Long? = null, val jam_pulang: Long? = null,
                          val email_perusahaan:String?=null, val alamat_perusahaan:String?=null, val no_telepon_perusahaan:String?=null,
                          val tahun_berdiri:String?=null,val bidang_perusahaan:String?=null, val pemilik_id:String?=null,
                          val gambar_perusahaan:String?=null,
                          val loclapos:String?=null,val loclamin:String?=null ,val loclongpos:String?=null,val loclongmin:String?=null) {
    }
}