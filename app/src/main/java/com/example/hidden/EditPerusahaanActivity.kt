package com.example.hidden

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_edit_perusahaan.*
import kotlinx.android.synthetic.main.activity_edit_perusahaan.view.*
import kotlinx.android.synthetic.main.activity_reg_perusahaan.*
import java.util.*


class EditPerusahaanActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var storage = Firebase.storage
    private var REQUEST_CODE=101
    private var gambardata: Uri?=null
    private var imageUrl:String?=null
    var totjammasuk=0
    var totmenitmasuk=0
    var totjampulang=0
    var totmenitpulang=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_perusahaan)
        val mTimePicker: TimePickerDialog
        val nTimePicker: TimePickerDialog
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        gambardata=null
        loadPerusahaanDetail()
        mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjammasuk=hourOfDay
                totmenitmasuk=minute
                txtWaktuJamMasukEditPerusahaan.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)
        nTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjampulang=hourOfDay
                totmenitpulang=minute
                txtWaktuJamPulangEditPerusahaan.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)
        btnAturJamMasukEditPerusahaan.setOnClickListener { v ->
            mTimePicker.show()
        }
        btnAturJamPulangEditPerusahaan.setOnClickListener {
                v-> nTimePicker.show()
        }
        btnCekLokasiPerusahaanEditPerusahaan.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:"+editLokasiPerusahaanEditPerusahaan.text.toString())
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.resolveActivity(packageManager)?.let {
                startActivity(mapIntent)
            }
        }
        btnEditEditPerusahaan.setOnClickListener {
            if(btnEditEditPerusahaan.text.toString()=="Edit"){
                btnEditEditPerusahaan.setBackgroundColor(getResources().getColor(R.color.green))
                btnEditEditPerusahaan.setText("Simpan")
                editNamaPerusahaanEditPerusahaan.isEnabled=true
                editAlamatPerusahaanEditPerusahaan.isEnabled=true
                editNoTeleponPerusahaanEditPerusahaan.isEnabled=true
                editTahunBerdiriPerusahaanEditPerusahaan.isEnabled=true
                editBidangPerusahaanEditPerusahaan.isEnabled=true
                editNamaPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)))
                editAlamatPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)))
                editNoTeleponPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)))
                editTahunBerdiriPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)))
                editBidangPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)))
                imageGambarPerusahaanEditPerusahaan.setOnClickListener {
                    openGalleryForImage()
                }
                imageGambarPerusahaanEditPerusahaan.setBackgroundColor(getResources().getColor(R.color.white))
                btnAturJamMasukEditPerusahaan.isVisible=true
                btnAturJamPulangEditPerusahaan.isVisible=true
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Simpan?")
                builder.setMessage("Apakah anda yakin menyimpan data terbaru?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    containerRelativeUploading.isVisible=true
                    var filepath=""
                    auth= Firebase.auth
                    var userId=Firebase.auth.currentUser?.uid.toString()
                    filepath="perusahaan/"+userId+"/gambar.jpg"
                    var storageRef = storage.reference
                    var spaceRef = storageRef.child(filepath)
                    if(gambardata!=null) {
                        var uploadTask = spaceRef.putFile(gambardata!!)
                        val ref = storageRef.child(filepath)
                        uploadTask.addOnFailureListener {
                            Log.v("upload", "gagal")
                        }.addOnSuccessListener { taskSnapshot ->
                            Log.v("upload", "berhasil")
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
                                    imageUrl = downloadUri.toString()
                                    database = Firebase.database.reference
                                    var perusahaanId = ""
                                    database.child("users").child(userId).child("perusahaan_id")
                                        .get().addOnSuccessListener {
                                        perusahaanId = it.value.toString()
                                        if (perusahaanId != "null") {
                                            database.child("perusahaan").child(perusahaanId)
                                                .child("gambar_perusahaan").setValue(imageUrl)
                                        } else {
                                        }
                                    }.addOnFailureListener {

                                    }
                                } else {

                                }
                            }
                        }
                        uploadTask.addOnProgressListener { it ->
                            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                            progressUploadEditPerusahaan.setText(progress.toString() + "%")
                        }.addOnPausedListener {
                        }
                    }
                    database = Firebase.database.reference
                    var perusahaanId = ""
                    database.child("users").child(userId).child("perusahaan_id")
                        .get().addOnSuccessListener {
                            perusahaanId = it.value.toString()
                            if (perusahaanId != "null") {
                                database.child("perusahaan").child(perusahaanId)
                                    .child("nama_perusahaan")
                                    .setValue(editNamaPerusahaanEditPerusahaan.text.toString())
                                database.child("perusahaan").child(perusahaanId)
                                    .child("alamat_perusahaan")
                                    .setValue(editAlamatPerusahaanEditPerusahaan.text.toString())
                                database.child("perusahaan").child(perusahaanId)
                                    .child("no_telepon_perusahaan")
                                    .setValue(editNoTeleponPerusahaanEditPerusahaan.text.toString())
                                database.child("perusahaan").child(perusahaanId)
                                    .child("tahun_berdiri")
                                    .setValue(editTahunBerdiriPerusahaanEditPerusahaan.text.toString())
                                database.child("perusahaan").child(perusahaanId)
                                    .child("bidang_perusahaan")
                                    .setValue(editBidangPerusahaanEditPerusahaan.text.toString())
                                database.child("perusahaan").child(perusahaanId).child("jam_masuk").setValue(totjammasuk)
                                database.child("perusahaan").child(perusahaanId).child("menit_masuk").setValue(totmenitmasuk)
                                database.child("perusahaan").child(perusahaanId).child("jam_pulang").setValue(totjampulang)
                                database.child("perusahaan").child(perusahaanId).child("menit_pulang").setValue(totmenitpulang)
                                btnEditEditPerusahaan.setBackgroundColor(
                                    getResources().getColor(
                                        R.color.purple_500
                                    )
                                )
                                ubahkesebelumtampilanedit()
                            } else {
                            }
                        }.addOnFailureListener {

                        }

                    containerRelativeUploading.visibility = View.GONE
                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    Toast.makeText(applicationContext,
                        "Tidak", Toast.LENGTH_SHORT).show()
                    ubahkesebelumtampilanedit()
                    loadPerusahaanDetail()

                }
                builder.show()
                imageGambarPerusahaanEditPerusahaan.setOnClickListener(null)
            }
        }
        btnTambahKaryawanEditPerusahaan.setOnClickListener {
            val intent = Intent(this, DaftarkanKaryawanActivity::class.java)
            startActivity(intent)
        }
    }
    private fun ubahkesebelumtampilanedit(){
        imageGambarPerusahaanEditPerusahaan.setBackgroundColor(getResources().getColor(R.color.black))
        btnEditEditPerusahaan.setBackgroundColor(getResources().getColor(R.color.purple_500))
        btnEditEditPerusahaan.setText("Edit")
        editNamaPerusahaanEditPerusahaan.isEnabled=false
        editAlamatPerusahaanEditPerusahaan.isEnabled=false
        editNoTeleponPerusahaanEditPerusahaan.isEnabled=false
        editTahunBerdiriPerusahaanEditPerusahaan.isEnabled=false
        editBidangPerusahaanEditPerusahaan.isEnabled=false
        editNamaPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)))
        editAlamatPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)))
        editNoTeleponPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)))
        editTahunBerdiriPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)))
        editBidangPerusahaanEditPerusahaan.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)))
        btnAturJamMasukEditPerusahaan.isVisible=false
        btnAturJamPulangEditPerusahaan.isVisible=false

    }
    private fun loadPerusahaanDetail(){
        auth= Firebase.auth
        var userID=Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        var perusahaanId=""
        database.child("users").child(userID).child("perusahaan_id").get().addOnSuccessListener {  perusahaanId=it.value.toString()
            if(perusahaanId!="null"){
                 database.child("perusahaan").child(perusahaanId).get().addOnSuccessListener {
                     Glide.with(this).load(it.child("gambar_perusahaan").value.toString()).into(imageGambarPerusahaanEditPerusahaan)
                     editNamaPerusahaanEditPerusahaan.setText(it.child("nama_perusahaan").value.toString())
                    editNamaPemilikEditPerusahaan.setText(auth.currentUser?.displayName.toString())
                    editAlamatPerusahaanEditPerusahaan.setText(it.child("alamat_perusahaan").value.toString())
                    editNoTeleponPerusahaanEditPerusahaan.setText(it.child("no_telepon_perusahaan").value.toString())
                    editTahunBerdiriPerusahaanEditPerusahaan.setText(it.child("tahun_berdiri").value.toString())
                    editBidangPerusahaanEditPerusahaan.setText(it.child("bidang_perusahaan").value.toString())
                     editLokasiPerusahaanEditPerusahaan.setText(it.child("loclatitude").value.toString()+","+it.child("loclongitude").value.toString())
                     totjammasuk=it.child("jam_masuk").value.toString().toInt()
                     totmenitmasuk=it.child("menit_masuk").value.toString().toInt()
                     totjampulang=it.child("jam_pulang").value.toString().toInt()
                     totmenitpulang=it.child("menit_pulang").value.toString().toInt()
                     txtWaktuJamMasukEditPerusahaan.setText(it.child("jam_masuk").value.toString()+":"+it.child("menit_masuk").value.toString())
                     txtWaktuJamPulangEditPerusahaan.setText(it.child("jam_pulang").value.toString()+":"+it.child("menit_pulang").value.toString())
                }.addOnFailureListener{
                }
            }
            else{
            }
        }.addOnFailureListener {

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
            imageGambarPerusahaanEditPerusahaan.setImageURI(data?.data)
            gambardata= data?.data!!
            Glide.with(this).load(data?.data).into(imageGambarPerusahaanEditPerusahaan)

        }
    }
    private fun uploadGambar(){

    }
}