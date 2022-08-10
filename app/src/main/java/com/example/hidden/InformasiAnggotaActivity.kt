package com.example.hidden

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.EmailAuthProvider
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
import kotlinx.android.synthetic.main.activity_informasi_anggota.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
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

    var caseFile: File? = null
    var faceDetector: CascadeClassifier? = null
    private lateinit var scaled: Bitmap
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
        btnHapusInfoAnggota.setOnClickListener {
            if(txtUserRoleInfoAnggota.text.toString()=="karyawan") {
                val alert = AlertDialog.Builder(this)
                val edittext = EditText(this)
//                alert.setMessage("Masukkan password untuk " + txtEmailAnggotaInfoAnggota.text.toString() + ":")
                alert.setMessage("Masukkan password anda:")
                alert.setTitle("Hapus " + editNamaAnggotaInfoAnggota.text.toString() + "?")

                alert.setView(edittext)

                alert.setPositiveButton("Hapus",
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
                                database.child("users").child(tempUserId).child("anggota_perusahaan_id").removeValue()
                                database.child("users").child(tempUserId).child("perusahaan_id").removeValue()
                                database.child("users").child(tempUserId).child("user_role").removeValue()
                                database.child("perusahaan").child(perusahaanId).child("anggota").child(txtIdAnggotaInfoAnggota.text.toString()).removeValue()
                                 Toast.makeText(
                                    baseContext, "Hapus Akun dari Perusahaan Berhasil",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()

                            }.addOnFailureListener {
                                Toast.makeText(
                                    baseContext, "Hapus Akun Gagal, Silakan Cek Kembali Password isian.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }catch (e:Exception){
                            Toast.makeText(
                                baseContext, "Hapus Akun Gagal, Silakan Cek Kembali Password isian.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
//                        val firebaseAppList = FirebaseApp.getApps(this)
//                        for (app in firebaseAppList) {
//                            if (app.name == "secondary_db_auth") {
//                                app.delete()
//                                break
//                            }
//                        }
//                        val firebaseOptionsBuilder = FirebaseOptions.Builder()
//                        firebaseOptionsBuilder.setApiKey("AIzaSyClgoyKN62rIiY45cjFRDDxM1Vf5QIE0fQ")
//                        firebaseOptionsBuilder.setDatabaseUrl("https://hidden-ad93d-default-rtdb.asia-southeast1.firebasedatabase.app")
//                        firebaseOptionsBuilder.setProjectId("hidden-ad93d")
//                        firebaseOptionsBuilder.setApplicationId("1:951410585581:android:2766f28c0558022e0dc1e4") //not sure if this one is needed
//                        val firebaseOptions = firebaseOptionsBuilder.build()
//
//                        val newAuth = FirebaseApp.initializeApp(this, firebaseOptions, "secondary_db_auth")
//
//                        try {
//                            FirebaseAuth.getInstance(newAuth).signInWithEmailAndPassword(txtEmailAnggotaInfoAnggota.text.toString(), isipas)
//                                .addOnCompleteListener(this) { task ->
//                                    if (task.isSuccessful) {
//                                        val userKaryawan = FirebaseAuth.getInstance(newAuth).currentUser
//
//                                        userKaryawan!!.delete()
//                                            .addOnCompleteListener { task ->
//                                                if (task.isSuccessful) {
//                                                    database.child("users").child(tempUserId).removeValue()
//                                                    database.child("perusahaan").child(perusahaanId).child("anggota").child(txtIdAnggotaInfoAnggota.text.toString()).removeValue()
//                                                    FirebaseAuth.getInstance(newAuth).signOut()
//                                                    finish()
//                                                    Toast.makeText(
//                                                        baseContext, "Hapus Akun Berhasil.",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                }
//                                            }
//                                    } else {
//                                        Toast.makeText(
//                                            baseContext, "Hapus Akun Gagal, Silakan Cek Kembali Password isian.",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//
//                        }catch (e:Exception){
//                            Toast.makeText(
//                                baseContext, "Hapus Akun Gagal",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
                    })

                alert.setNegativeButton("Batal",
                    DialogInterface.OnClickListener { dialog, whichButton ->

                    })

                alert.show()
            }
            else{
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Tidak Dapat Menghapus Akun Pemilik.")

                alert.setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, whichButton ->
                    })
                alert.show()
            }
        }
        btnEditInfoAnggota.setOnClickListener {
            if(btnEditInfoAnggota.text.toString()=="Edit"){
                btnHapusInfoAnggota.isVisible=true
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
//                detector = viewModel!!.detector
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
                if (!OpenCVLoader.initDebug()) {
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback)
                } else {
                    try {
                        baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                image = InputImage.fromFilePath(this, data?.data!!)
                val imageUri: Uri = data?.data!!
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val tmp = Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1)
                Utils.bitmapToMat(bitmap, tmp)
                val facedetections = MatOfRect()
                var smat:Mat=Mat(112,112,CvType.CV_8U, Scalar(4.0))
                faceDetector!!.detectMultiScale(tmp, facedetections,1.1,20,0,smat.size())
                for (react in facedetections.toArray()) {
                    if(react==facedetections.toArray()[0]) {
                        Log.v("react", react.toString())
                        var bmp: Bitmap? = null
                        val tmpx = Mat(400, 400, CvType.CV_8U, Scalar(4.0))
                        var preview = Mat(tmp, react)
                        try {
                            Imgproc.cvtColor(preview, tmpx, Imgproc.COLOR_mRGBA2RGBA);
//                Imgproc.cvtColor(preview, tmp, Imgproc.COLOR_GRAY2RGBA, 4)
                            bmp = Bitmap.createBitmap(tmpx.cols(), tmpx.rows(), Bitmap.Config.ARGB_8888)
                            Utils.matToBitmap(tmpx, bmp)
                            runOnUiThread {
                                imageGambarWajahHasilInfoAnggota.setImageBitmap(bmp)
                            }
                            scaled= BitmapUtils.getResizedBitmap(bmp, 112, 112)
                            val imgData: ByteBuffer =
                                getImgData(inputSize, scaled, IMAGE_MEAN, IMAGE_STD)
                            val inputArray = arrayOf<Any>(imgData)
                            val outputMap: MutableMap<Int, Any> = HashMap()
                            embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
                            outputMap[0] = embeddings
                            tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap!!)
                            Toast.makeText(
                                baseContext, "Wajah terdeteksi",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: CvException) {
                            Log.d("Exception", e.message!!)
                        }
                    }
                }
//                image = InputImage.fromFilePath(this, data?.data!!)
//                val result = detector!!.process(image).addOnSuccessListener { faces ->
//                    for (face in faces) {
//                        val imageUri: Uri = data?.data!!
//                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
//                        val boundingBox = RectF(face.boundingBox)
//                        var cropped_face: Bitmap =
//                            BitmapUtils.getCropBitmapByCPU(bitmap, boundingBox)
//                        val scaled: Bitmap =
//                            BitmapUtils.getResizedBitmap(cropped_face, 112, 112)
//                        imageGambarWajahHasilInfoAnggota.setImageBitmap(scaled)
//                        val imgData: ByteBuffer =
//                            getImgData(inputSize, scaled, IMAGE_MEAN, IMAGE_STD)
//                        val inputArray = arrayOf<Any>(imgData)
//                        val outputMap: MutableMap<Int, Any> = HashMap()
//                        embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
//                        outputMap[0] = embeddings
//                        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap!!)
//
//                    }
//
//                }
//                    .addOnFailureListener { e ->
//
//                    }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private val baseCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        @Throws(IOException::class)
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    val `is`: InputStream =
                        resources.openRawResource(R.raw.haarcascade_frontalface_alt2)
                    val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
                    caseFile = File(cascadeDir, "haarcascade_frontalface_alt2.xml")
                    val fos = FileOutputStream(caseFile)
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (`is`.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                    `is`.close()
                    fos.close()
                    faceDetector = CascadeClassifier(caseFile!!.absolutePath)
                    if (faceDetector!!.empty()) {
                        faceDetector = null
                    } else {
                        cascadeDir.delete()
                    }
                }
                else -> super.onManagerConnected(status)
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
        btnEditInfoAnggota.setText("Edit")
        btnHapusInfoAnggota.isVisible=false
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