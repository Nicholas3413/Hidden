package com.example.hidden

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_edit_perusahaan.*
import kotlinx.android.synthetic.main.activity_profil_pemilik.*
import kotlinx.android.synthetic.main.activity_reg_akun_pemilik.*
import kotlinx.android.synthetic.main.activity_reg_wajah_pemilik.*
import kotlinx.android.synthetic.main.list_absensi_harian.view.*

class ProfilPemilikActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var storage = Firebase.storage
    private var REQUEST_CODE=102
    private var gambardata: Uri?=null
    private var imageUrl:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_pemilik)
        loadUserDetail()
        btnEditProfilPemilik.setOnClickListener {
//            if(btnEditProfilPemilik.equals(R.drawable.edit_profil))

            if(btnEditProfilPemilik.text.toString()=="Edit"){
//                btnEditProfilPemilik.setBackgroundColor(getResources().getColor(R.color.blue))
                btnEditProfilPemilik.setText("Simpan")
                editNamaPemilikProfilPemilik.isEnabled=true
                editNIKProfilPemilik.isEnabled=true
                editAlamatProfilPemilik.isEnabled=true
                editNoTeleponProfilPemilik.isEnabled=true
                imageGambarPemilikProfilPemilik.setOnClickListener {
                    openGalleryForImage()
                }
                imageEditPenProfilPemilik.isVisible=true
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Simpan?")
                builder.setMessage("Apakah anda yakin menyimpan data terbaru?")
                builder.setPositiveButton("Ya") { dialog, which ->

                    var filepath=""
                    auth= Firebase.auth
                    var userId=Firebase.auth.currentUser?.uid.toString()
                    filepath="users/"+userId+"/gambar.jpg"
                    var storageRef = storage.reference
                    var spaceRef = storageRef.child(filepath)
                    if(gambardata!=null) {
                        containerRelativeUploadingProfil.isVisible=true
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
                                    database.child("users").child(userId).child("gambar_user").setValue(imageUrl)
                                } else {

                                }
                            }
                        }
                        uploadTask.addOnProgressListener { it ->
                            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                            progressUploadProfilPemilik.setText(progress.toString() + "%")

                        }.addOnPausedListener {
                        }.addOnCompleteListener {
                            containerRelativeUploadingProfil.isVisible=false
                        }
                    }
                    database = Firebase.database.reference
                    if(editNamaPemilikProfilPemilik.getText().toString()!=""){
                        var user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = editNamaPemilikProfilPemilik.getText().toString()
                        }
                        user!!.updateProfile(profileUpdates)
                        database.child("users").child(userId).child("user_name").setValue(editNamaPemilikProfilPemilik.text.toString())

                    }
                    if(editNIKProfilPemilik.text.toString()!=""){
                        database.child("users").child(userId).child("nik").setValue(editNIKProfilPemilik.text.toString())
                    }
                    if(editNoTeleponProfilPemilik.text.toString()!=""){
                        database.child("users").child(userId).child("no_telepon_user").setValue(editNoTeleponProfilPemilik.text.toString())
                    }
                    if(editAlamatProfilPemilik.text.toString()!=""){
                        database.child("users").child(userId).child("alamat_user").setValue(editAlamatProfilPemilik.text.toString())
                    }

                    ubahkesebelumtampilanedit()
                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    Toast.makeText(applicationContext,
                        "Tidak", Toast.LENGTH_SHORT).show()
                    ubahkesebelumtampilanedit()
                    loadUserDetail()

                }
                builder.show()
            }
        }
        btnRegisterWajahProfilPemilik.setOnClickListener {
            val intent = Intent(this, EditWajahPemilikActivity::class.java)
            startActivity(intent)
        }
    }
    private fun ubahkesebelumtampilanedit(){
//        btnEditProfilPemilik.setBackgroundColor(getResources().getColor(R.color.black))
        btnEditProfilPemilik.setText("Edit")
        editNamaPemilikProfilPemilik.isEnabled=false
        editNIKProfilPemilik.isEnabled=false
        editAlamatProfilPemilik.isEnabled=false
        editNoTeleponProfilPemilik.isEnabled=false
        imageEditPenProfilPemilik.isVisible=false
        imageGambarPemilikProfilPemilik.setOnClickListener(null)
    }
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            gambardata= data?.data!!
            Glide.with(this).load(data?.data).circleCrop().into(imageGambarPemilikProfilPemilik)

        }
    }
    private fun loadUserDetail(){
        database = Firebase.database.reference
        auth= Firebase.auth
        var userId=Firebase.auth.currentUser?.uid.toString()
        database.child("users").child(userId).get().addOnSuccessListener {
            if(it.child("user_name").value!=null){
                editNamaPemilikProfilPemilik.setText(it.child("user_name").value.toString())
            }
            else{
                editNamaPemilikProfilPemilik.text.clear()
                editNamaPemilikProfilPemilik.setHint("Belum diisi")
            }
            editEmailPemilikProfilPemilik.setText(it.child("email_user").value.toString())
            if(it.child("nik").value!=null){
                editNIKProfilPemilik.setText(it.child("nik").value.toString())
            }
            else{
                editNIKProfilPemilik.text.clear()
                editNIKProfilPemilik.setHint("Belum diisi")
            }
            if(it.child("alamat_user").value!=null){
                editAlamatProfilPemilik.setText(it.child("alamat_user").value.toString())
            }
            else{
                editAlamatProfilPemilik.text.clear()
                editAlamatProfilPemilik.setHint("Belum diisi")
            }
            if(it.child("no_telepon_user").value!=null){
                editNoTeleponProfilPemilik.setText(it.child("no_telepon_user").value.toString())
            }
            else{
                editNoTeleponProfilPemilik.text.clear()
                editNoTeleponProfilPemilik.setHint("Belum diisi")
            }
            if(it.child("registered_face").value!=null){
                txtStatusRegistrasiWajahProfilPemilik.setText("Absensi Wajah Sudah Registrasi")
                txtStatusRegistrasiWajahProfilPemilik.setTextColor(Color.parseColor("green"))
            }
            else{
                txtStatusRegistrasiWajahProfilPemilik.setText("Absensi Wajah Belum Registrasi")
                txtStatusRegistrasiWajahProfilPemilik.setTextColor(Color.parseColor("red"))
            }
            Glide.with(this)
                .load(it.child("gambar_user").value.toString())
                .circleCrop()
                .placeholder(R.drawable.avatar)
                .into(imageGambarPemilikProfilPemilik)
        }
    }

    override fun onStart() {
        super.onStart()

//        gambardata=null
    }
}