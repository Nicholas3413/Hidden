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
import com.google.firebase.database.ServerValue
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
        var totmenitmasuk=0
        var totjampulang=0
        var totmenitpulang=0

        mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjammasuk=hourOfDay
                totmenitmasuk=minute
                txtWaktuJamMasukRegPerusahaan.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)
        nTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjampulang=hourOfDay
                totmenitpulang=minute
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
            writeNewPerusahaan(userID,editNamaPerusahaanRegPerusahaan.getText().toString(),totjammasuk.toInt(),totmenitmasuk.toInt(),totjampulang.toInt(),totmenitpulang.toInt())
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
            var filepath=""
            auth= Firebase.auth
            var userID=Firebase.auth.currentUser?.uid.toString()
            filepath="perusahaan/"+userID+"/gambar.jpg"
            var storageRef = storage.reference
            var spaceRef = storageRef.child(filepath)
            var uploadTask = spaceRef.putFile(data?.data!!)
            val ref = storageRef.child(filepath)
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
//                        Glide.with(this).load(imageUrl).into(tesimgPreviewPilihGambarRegPerusahaan)
                    } else {

                    }
                }
            }

        }
    }


    fun writeNewPerusahaan(userId: String, name: String,jam_masuk: Int?,menit_masuk: Int?,jam_pulang: Int?,menit_pulang: Int? ) {
        database = Firebase.database.reference
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var randomstring=""
        for (i in 1..6) {
            val random1 = (0..(charPool.size-1)).shuffled().last()
            var tempchar=charPool[random1]
            randomstring=randomstring+tempchar
        }
        Log.v("randomstring",randomstring)
        var randomstringA=""
        for (i in 1..6) {
            val random2 = (0..(charPool.size-1)).shuffled().last()
            var tempcharA=charPool[random2]
            randomstringA=randomstringA+tempcharA
        }
        randomstringA="A"+randomstringA

        val sharedPreferences = getSharedPreferences("Location", Context.MODE_PRIVATE)
        var loclapos=sharedPreferences.getString("new_latitude_pos","")
        var loclamin=sharedPreferences.getString("new_latitude_min","")
        var loclongpos=sharedPreferences.getString("new_longitude_pos","")
        var loclongmin=sharedPreferences.getString("new_longitude_min","")
        var loclatitude=sharedPreferences.getString("latitude","")
        var loclongitude=sharedPreferences.getString("longitude","")
        val sharedPreferencesSettings = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editorSettings = sharedPreferencesSettings.edit()
        editorSettings.clear().apply()
        editorSettings.putString("jam_masuk", jam_masuk.toString())
        editorSettings.putString("menit_masuk", menit_masuk.toString())
        editorSettings.putString("jam_pulang", jam_pulang.toString())
        editorSettings.putString("menit_pulang", menit_pulang.toString())
        editorSettings.putString("loclamin", loclamin)
        editorSettings.putString("loclapos", loclapos)
        editorSettings.putString("loclongmin", loclongmin)
        editorSettings.putString("loclongpos", loclongpos)
        editorSettings.putString("user_role", "pemilik")
        Log.v("4loc",loclapos.toString()+loclamin.toString()+loclongpos.toString()+loclongmin.toString())
        val perusahaan = Perusahaan(name,jam_masuk,menit_masuk,jam_pulang,menit_pulang,editEmailPerusahaanRegPerusahaan.getText().toString()
            ,editAlamatPerusahaanRegPerusahaan.getText().toString(),editNoTelpPerusahaanRegPerusahaan.getText().toString()
            ,editTahunBerdiriPerusahaanRegPerusahaan.getText().toString(),editBidangPerusahaanRegPerusahaan.getText().toString()
            ,userId,imageUrl,loclapos,loclamin,loclongpos,loclongmin,loclatitude,loclongitude)
        database.child("perusahaan").child(randomstring).setValue(perusahaan)
        database.child("users").child(userId).child("perusahaan_id").setValue(randomstring)
        database.child("users").child(userId).child("anggota_perusahaan_id").setValue(randomstringA)
        database.child("perusahaan").child(randomstring).child("anggota").child(randomstringA).child("anggota_id").setValue(randomstringA)
        database.child("perusahaan").child(randomstring).child("anggota").child(randomstringA).child("perusahaan_id").setValue(randomstring)
        database.child("perusahaan").child(randomstring).child("anggota").child(randomstringA).child("user_id").setValue(userId)
        database.child("perusahaan").child(randomstring).child("anggota").child(randomstringA).child("status_anggota").setValue("aktif")
        database.child("perusahaan").child(randomstring).child("anggota").child(randomstringA).child("bagian").setValue("pemilik")
        database.child("perusahaan").child(randomstring).child("anggota").child(randomstringA).child("tanggal_masuk_perusahaan").setValue(ServerValue.TIMESTAMP)
        if(editWorkHoursDayRegPerusahaan.text.toString().trim()==""){
            database.child("perusahaan").child(randomstring).child("work_hours_day").setValue("0")

        }else {
            database.child("perusahaan").child(randomstring).child("work_hours_day")
                .setValue(editWorkHoursDayRegPerusahaan.text.toString())
        }
        if(editWorkHoursWeekRegPerusahaan.text.toString().trim()==""){
            database.child("perusahaan").child(randomstring).child("work_hours_week").setValue("0")
        }else {
            database.child("perusahaan").child(randomstring).child("work_hours_week")
                .setValue(editWorkHoursWeekRegPerusahaan.text.toString())
        }
        editorSettings.putString("perusahaan_id", randomstring)
        editorSettings.apply()
    }
    @IgnoreExtraProperties
    data class Perusahaan(val nama_perusahaan: String? = null, val jam_masuk: Int? = null, val menit_masuk: Int? = null, val jam_pulang: Int? = null,
                          val menit_pulang: Int? = null,
                          val email_perusahaan:String?=null, val alamat_perusahaan:String?=null, val no_telepon_perusahaan:String?=null,
                          val tahun_berdiri:String?=null,val bidang_perusahaan:String?=null, val pemilik_id:String?=null,
                          val gambar_perusahaan:String?=null,
                          val loclapos:String?=null,val loclamin:String?=null ,val loclongpos:String?=null,val loclongmin:String?=null,
                          val loclatitude:String?=null,val loclongitude:String?=null) {
    }
}