package com.example.hidden

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_perusahaan.*
import kotlinx.android.synthetic.main.activity_informasi_perusahaan.*

class InformasiPerusahaanActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_perusahaan)
        loadPerusahaanDetail()
    }
    private fun loadPerusahaanDetail(){
        auth= Firebase.auth
        var userID= Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        var perusahaanId=""
        database.child("")
        database.child("users").child(userID).child("perusahaan_id").get().addOnSuccessListener {  perusahaanId=it.value.toString()
            if(perusahaanId!="null"){
                database.child("perusahaan").child(perusahaanId).get().addOnSuccessListener {
                    Glide.with(this).load(it.child("gambar_perusahaan").value.toString()).into(imageGambarPerusahaanInfoPerusahaan)
                    editNamaPerusahaanInfoPerusahaan.setText(it.child("nama_perusahaan").value.toString())
                    database.child("users").child(it.child("pemilik_id").value.toString()).child("user_name").get().addOnSuccessListener {
                        editNamaPemilikInfoPerusahaan.setText(it.value.toString())
                    }
                    editEmailInfoPerusahaan.setText(it.child("email_perusahaan").value.toString())
                    editAlamatPerusahaanInfoPerusahaan.setText(it.child("alamat_perusahaan").value.toString())
                    editNoTeleponPerusahaanInfoPerusahaan.setText(it.child("no_telepon_perusahaan").value.toString())
                    editTahunBerdiriPerusahaanInfoPerusahaan.setText(it.child("tahun_berdiri").value.toString())
                    editBidangPerusahaanInfoPerusahaan.setText(it.child("bidang_perusahaan").value.toString())
                    editLokasiPerusahaanInfoPerusahaan.setText(it.child("loclatitude").value.toString()+","+it.child("loclongitude").value.toString())
                    txtWaktuJamMasukInfoPerusahaan.setText(it.child("jam_masuk").value.toString()+":"+it.child("menit_masuk").value.toString())
                    txtWaktuJamPulangInfoPerusahaan.setText(it.child("jam_pulang").value.toString()+":"+it.child("menit_pulang").value.toString())
                    editWorkHoursDayInfoPerusahaan.setText(it.child("work_hours_day").value.toString())
                    editWorkHoursWeekInfoPerusahaan.setText(it.child("work_hours_week").value.toString())
                }.addOnFailureListener{
                }
            }
            else{
            }
        }.addOnFailureListener {

        }
        btnCekLokasiPerusahaanInfoPerusahaan.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:"+editLokasiPerusahaanInfoPerusahaan.text.toString())
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.resolveActivity(packageManager)?.let {
                startActivity(mapIntent)
            }
        }
    }
}