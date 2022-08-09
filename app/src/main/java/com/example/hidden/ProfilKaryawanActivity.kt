package com.example.hidden

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_profil_karyawan.*


class ProfilKaryawanActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var storage = Firebase.storage
    private var REQUEST_CODE=103
    private var gambardata: Uri?=null
    private var imageUrl:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil_karyawan)
        loadUserDetail()
        btnEditProfilKaryawan.setOnClickListener {
            if(btnEditProfilKaryawan.text.toString()=="Edit"){
//                btnEditProfilKaryawan.setBackgroundColor(getResources().getColor(R.color.blue))
                btnEditProfilKaryawan.setText("Simpan")
                editNamaPemilikProfilKaryawan.isEnabled=true
                editNIKProfilKaryawan.isEnabled=true
                editAlamatProfilKaryawan.isEnabled=true
                editNoTeleponProfilKaryawan.isEnabled=true
                imageGambarPemilikProfilKaryawan.setOnClickListener {
                    openGalleryForImage()
                }
                imageEditPenProfilKaryawan.isVisible=true
                btnGantiPasswordProfilKaryawan.isVisible=true
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
                        containerRelativeUploadingProfilKaryawan.isVisible=true
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
                            progressUploadProfilKaryawan.setText(progress.toString() + "%")

                        }.addOnPausedListener {
                        }.addOnCompleteListener {
                            containerRelativeUploadingProfilKaryawan.isVisible=false
                        }
                    }
                    database = Firebase.database.reference
                    if(editNamaPemilikProfilKaryawan.getText().toString()!=""){
                        var user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = editNamaPemilikProfilKaryawan.getText().toString()
                        }
                        user!!.updateProfile(profileUpdates)
                        database.child("users").child(userId).child("user_name").setValue(editNamaPemilikProfilKaryawan.text.toString())

                    }
                    if(editNIKProfilKaryawan.text.toString()!=""){
                        database.child("users").child(userId).child("nik").setValue(editNIKProfilKaryawan.text.toString())
                    }
                    if(editNoTeleponProfilKaryawan.text.toString()!=""){
                        database.child("users").child(userId).child("no_telepon_user").setValue(editNoTeleponProfilKaryawan.text.toString())
                    }
                    if(editAlamatProfilKaryawan.text.toString()!=""){
                        database.child("users").child(userId).child("alamat_user").setValue(editAlamatProfilKaryawan.text.toString())
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
        btnGantiPasswordProfilKaryawan.setOnClickListener {
            val alert = AlertDialog.Builder(this)
            val edittext = EditText(this)
//                alert.setMessage("Masukkan password untuk " + txtEmailAnggotaInfoAnggota.text.toString() + ":")
            alert.setMessage("Masukkan password lama anda:")
            alert.setTitle("Ubah Password?")

            alert.setView(edittext)

            alert.setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val isipas = edittext.text.toString()
                    Log.v("isipass", isipas)
                    auth= Firebase.auth
                    var userEmail=Firebase.auth.currentUser?.email.toString()

                    try{
                        val credential = EmailAuthProvider
                            .getCredential(userEmail, isipas)
                        Firebase.auth.currentUser!!.reauthenticate(credential)
                            .addOnSuccessListener { Log.v("credential","berhasil")
                                val alert2 = AlertDialog.Builder(this)
                                val edittext2 = EditText(this)

                                alert2.setMessage("Masukkan password baru anda:")
                                alert2.setTitle("Input Password Baru")
                                alert2.setView(edittext2)
                                alert2.setNegativeButton("Batal",
                                    DialogInterface.OnClickListener { dialog, whichButton ->
                                        Toast.makeText(
                                            baseContext, "Batal Ubah Password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                                alert2.setPositiveButton("Simpan",
                                    DialogInterface.OnClickListener { dialog, whichButton ->
                                        try{
                                            val isipas2 = edittext2.text.toString()
                                            Firebase.auth.currentUser!!.updatePassword(isipas2)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(
                                                            baseContext, "Ubah Password Berhasil!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }catch(e:Exception){
                                            Toast.makeText(
                                                baseContext, "Password tidak boleh kosong",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                                alert2.show()

//                                finish()

                            }.addOnFailureListener {
                                Toast.makeText(
                                    baseContext, "Password salah.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }catch (e:Exception){
                        Toast.makeText(
                            baseContext, "Password salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            alert.setNegativeButton("Batal",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    Toast.makeText(
                        baseContext, "Batal Ubah Password",
                        Toast.LENGTH_SHORT
                    ).show()
                })

            alert.show()
        }
    }
    private fun ubahkesebelumtampilanedit(){
        btnEditProfilKaryawan.setBackgroundColor(getResources().getColor(R.color.black))
        btnEditProfilKaryawan.setText("Edit")
        editNamaPemilikProfilKaryawan.isEnabled=false
        editNIKProfilKaryawan.isEnabled=false
        editAlamatProfilKaryawan.isEnabled=false
        editNoTeleponProfilKaryawan.isEnabled=false
        imageEditPenProfilKaryawan.isVisible=false
        imageGambarPemilikProfilKaryawan.setOnClickListener(null)
        btnGantiPasswordProfilKaryawan.isVisible=false
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
            Glide.with(this).load(data?.data).circleCrop().into(imageGambarPemilikProfilKaryawan)
        }
    }
    private fun loadUserDetail(){
        database = Firebase.database.reference
        auth= Firebase.auth
        var userId=Firebase.auth.currentUser?.uid.toString()
        database.child("users").child(userId).get().addOnSuccessListener {
            if(it.child("user_name").value!=null){
                editNamaPemilikProfilKaryawan.setText(it.child("user_name").value.toString())
            }
            else{
                editNamaPemilikProfilKaryawan.text.clear()
                editNamaPemilikProfilKaryawan.setHint("Belum diisi")
            }
            editEmailPemilikProfilKaryawan.setText(it.child("email_user").value.toString())
            if(it.child("nik").value!=null){
                editNIKProfilKaryawan.setText(it.child("nik").value.toString())
            }
            else{
                editNIKProfilKaryawan.text.clear()
                editNIKProfilKaryawan.setHint("Belum diisi")
            }
            if(it.child("alamat_user").value!=null){
                editAlamatProfilKaryawan.setText(it.child("alamat_user").value.toString())
            }
            else{
                editAlamatProfilKaryawan.text.clear()
                editAlamatProfilKaryawan.setHint("Belum diisi")
            }
            if(it.child("no_telepon_user").value!=null){
                editNoTeleponProfilKaryawan.setText(it.child("no_telepon_user").value.toString())
            }
            else{
                editNoTeleponProfilKaryawan.text.clear()
                editNoTeleponProfilKaryawan.setHint("Belum diisi")
            }
            Glide.with(this)
                .load(it.child("gambar_user").value.toString())
                .circleCrop()
                .placeholder(R.drawable.avatar)
                .into(imageGambarPemilikProfilKaryawan)
        }
    }
}