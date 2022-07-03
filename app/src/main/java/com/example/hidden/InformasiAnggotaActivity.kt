package com.example.hidden

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.android.synthetic.main.activity_daftarkan_karyawan.*
import kotlinx.android.synthetic.main.activity_edit_perusahaan.*
import kotlinx.android.synthetic.main.activity_informasi_anggota.*
import kotlinx.android.synthetic.main.activity_profil_pemilik.*
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class InformasiAnggotaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var storage = Firebase.storage
    private var REQUEST_CODE=104
    private var REQUEST_CODE2=105
    private var gambardata: Uri?=null
    private var imageUrl:String?=null
    private var viewModel: RegViewModel? = null
    private var tfLite: Interpreter? = null
    private var detector: FaceDetector? = null
    private var registered = HashMap<String?, RecordRecognition.Recognition?>()
    private lateinit var embeddings: Array<FloatArray>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_anggota)
        loadAnggotaDetail()
        val intentextra:String = intent.getStringExtra("anggota_id").toString()
        val sharedPreferences =getSharedPreferences("Settings", Context.MODE_PRIVATE)
        var perusahaanId=sharedPreferences.getString("perusahaan_id","")
        var tempUserId:String=""
        database.child("perusahaan").child(perusahaanId!!).child("anggota").child(intentextra).child("user_id").get().addOnSuccessListener {
            tempUserId=it.value.toString()
        }
        btnEditInfoAnggota.setOnClickListener {
            if(btnEditInfoAnggota.text.toString()=="Edit"){
                btnEditInfoAnggota.setBackgroundColor(getResources().getColor(R.color.green))
                btnEditInfoAnggota.setText("Simpan")
                editNamaAnggotaInfoAnggota.isEnabled=true
                editNamaAnggotaInfoAnggota.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)))
                editBagianInfoAnggota.isEnabled=true
                editNoTeleponInfoAnggota.isEnabled=true
                imageEditPenInfoAnggota.isVisible=true
                imageGambarWajahInfoAnggota.isVisible=true
                imageGambarWajahHasilInfoAnggota.isVisible=true
                btnMasukkanGambarWajahInfoAnggota.isVisible=true
                txtGambarWajahInfoAnggota.isVisible=true
                imageGambarAnggotaInfoAnggota.setOnClickListener {
                    openGalleryForImage()
                }
                btnMasukkanGambarWajahInfoAnggota.setOnClickListener {
                    openGalleryForImage2()
                }
                viewModel = ViewModelProvider(this).get(RegViewModel::class.java)
                viewModel!!.init(this)
                detector = viewModel!!.detector
                try {
                    tfLite = viewModel!!.getModel(this)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                embeddings = Array(1) { FloatArray(1) }
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Simpan?")
                builder.setMessage("Apakah anda yakin menyimpan data terbaru?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    var filepath=""
                    auth= Firebase.auth
                    filepath="users/"+tempUserId+"/gambar.jpg"
                    var storageRef = storage.reference
                    var spaceRef = storageRef.child(filepath)
                    if(gambardata!=null) {
                        containerRelativeUploadingInfoAnggota.isVisible=true
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
                                    database.child("users").child(tempUserId!!).child("gambar_user").setValue(imageUrl)
                                } else {

                                }
                            }
                        }
                        uploadTask.addOnProgressListener { it ->
                            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                            progressUploadInfoAnggota.setText(progress.toString() + "%")

                        }.addOnPausedListener {
                        }.addOnCompleteListener {
                            containerRelativeUploadingInfoAnggota.isVisible=false
                        }
                    }
                    database = Firebase.database.reference
                    if(editNamaAnggotaInfoAnggota.getText().toString()!=""){
                        database.child("users").child(tempUserId).child("user_name").setValue(editNamaAnggotaInfoAnggota.text.toString())
                    }
                    if(editNoTeleponInfoAnggota.text.toString()!=""){
                        database.child("users").child(tempUserId).child("no_telepon_user").setValue(editNoTeleponInfoAnggota.text.toString())
                    }
                    if(editBagianInfoAnggota.text.toString()!=""){
                        database.child("perusahaan").child(perusahaanId!!).child("anggota").child(intentextra).child("bagian").setValue(editBagianInfoAnggota.text.toString())
                    }
                    if(embeddings[0].size!=1) {
                        val result = RecordRecognition.Recognition()
                        result.extra = embeddings
                        registered[editNamaAnggotaInfoAnggota.text.toString()] = result
                        val newinputjsonstring = Gson().toJson(registered)
                        Log.v("sudahdiembeddings","sudahdiembeddings")
                        database.child("users").child(tempUserId).child("registered_face")
                            .setValue(newinputjsonstring)
                    }
                    ubahkesebelumtampilanedit()
                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    Toast.makeText(applicationContext,
                        "Tidak", Toast.LENGTH_SHORT).show()
                    ubahkesebelumtampilanedit()
                    loadAnggotaDetail()

                }
                builder.show()
            }
        }
    }
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }
    private fun openGalleryForImage2() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE2)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            gambardata= data?.data!!
            Glide.with(this).load(data?.data).circleCrop().into(imageGambarAnggotaInfoAnggota)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE2) {
            val inputSize = 112
            val OUTPUT_SIZE = 192
            val IMAGE_MEAN = 128.0f
            val IMAGE_STD = 128.0f
            Glide.with(this).load(data?.data).into(imageGambarWajahInfoAnggota)
            val image: InputImage
            try {
                image = InputImage.fromFilePath(this, data?.data!!)
                val result = detector!!.process(image).addOnSuccessListener { faces ->
                    for (face in faces) {
                        val imageUri: Uri = data?.data!!
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                        val boundingBox = RectF(face.boundingBox)
                        var cropped_face: Bitmap =
                            BitmapUtils.getCropBitmapByCPU(bitmap, boundingBox)
                        val scaled: Bitmap =
                            BitmapUtils.getResizedBitmap(cropped_face, 112, 112)
                        imageGambarWajahHasilInfoAnggota.setImageBitmap(scaled)
                        val imgData: ByteBuffer =
                            getImgData(inputSize, scaled, IMAGE_MEAN, IMAGE_STD)
                        val inputArray = arrayOf<Any>(imgData)
                        val outputMap: MutableMap<Int, Any> = HashMap()
                        embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
                        outputMap[0] = embeddings
                        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap!!)

                    }

                }
                    .addOnFailureListener { e ->

                    }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun getImgData(inputSize: Int,
                           bitmap: Bitmap,
                           IMAGE_MEAN: Float,
                           IMAGE_STD: Float
    ): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        imgData.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return imgData
    }
    private fun ubahkesebelumtampilanedit(){
        btnEditInfoAnggota.setBackgroundColor(getResources().getColor(R.color.black))
        btnEditInfoAnggota.setText("Edit")
        editNamaAnggotaInfoAnggota.isEnabled=false
        editNoTeleponInfoAnggota.isEnabled=false
        editBagianInfoAnggota.isEnabled=false
        imageEditPenInfoAnggota.isVisible=false
        imageGambarAnggotaInfoAnggota.setOnClickListener(null)
        btnMasukkanGambarWajahInfoAnggota.setOnClickListener(null)
        imageEditPenInfoAnggota.isVisible=false
        imageGambarWajahInfoAnggota.isVisible=false
        imageGambarWajahHasilInfoAnggota.isVisible=false
        btnMasukkanGambarWajahInfoAnggota.isVisible=false
        txtGambarWajahInfoAnggota.isVisible=false
        editNamaAnggotaInfoAnggota.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)))
    }
    private fun loadAnggotaDetail(){
        database = Firebase.database.reference
        val intentextra:String = intent.getStringExtra("anggota_id").toString()
        val sharedPreferences =getSharedPreferences("Settings", Context.MODE_PRIVATE)
        var perusahaanId=sharedPreferences.getString("perusahaan_id","")
        database.child("perusahaan").child(perusahaanId!!).child("anggota").child(intentextra).get().addOnSuccessListener {
            if(it.child("bagian").value!=null){
                editBagianInfoAnggota.setText(it.child("bagian").value.toString())
            }
            else{
                editBagianInfoAnggota.text.clear()
                editBagianInfoAnggota.setHint("Belum diisi")
            }
            txtIdAnggotaInfoAnggota.setText(it.child("anggota_id").value.toString())
            txtStatusInfoAnggota.setText(it.child("status_anggota").value.toString())
            editTanggalMasukInfoAnggota.setText(getDate(it.child("tanggal_masuk_perusahaan").value.toString().toLong()))
            database.child("users").child(it.child("user_id").value.toString()).get().addOnSuccessListener {
                if(it.child("user_name").value!=null){
                    editNamaAnggotaInfoAnggota.setText(it.child("user_name").value.toString())
                }
                else{
                    editNamaAnggotaInfoAnggota.text.clear()
                    editNamaAnggotaInfoAnggota.setHint("Belum diisi")
                }
                if(it.child("no_telepon_user").value!=null){
                    editNoTeleponInfoAnggota.setText(it.child("no_telepon_user").value.toString())
                }
                else{
                    editNoTeleponInfoAnggota.text.clear()
                    editNoTeleponInfoAnggota.setHint("Belum diisi")
                }
                txtEmailAnggotaInfoAnggota.setText(it.child("email_user").value.toString())
                txtUserRoleInfoAnggota.setText(it.child("user_role").value.toString())
                if(it.child("registered_face").value!=null){
                    txtStatusRegisterWajahInfoAnggota.setText("Absensi Wajah Sudah Registrasi")
                    txtStatusRegisterWajahInfoAnggota.setTextColor(Color.parseColor("green"))
                }
                else{
                    txtStatusRegisterWajahInfoAnggota.setText("Absensi Wajah Belum Registrasi")
                    txtStatusRegisterWajahInfoAnggota.setTextColor(Color.parseColor("red"))
                }
                Glide.with(this)
                    .load(it.child("gambar_user").value.toString())
                    .circleCrop()
                    .placeholder(R.drawable.avatar)
                    .into(imageGambarAnggotaInfoAnggota)
            }


        }
    }
    private fun getDate(time: Long?): String {
        val format = "dd MMMM yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(time!!))
    }
}