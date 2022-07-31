package com.example.hidden

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.android.synthetic.main.activity_daftarkan_karyawan.*
import kotlinx.android.synthetic.main.activity_main2.*
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


class DaftarkanKaryawanActivity : AppCompatActivity() {
    private var viewModel: RegViewModel? = null
    private var tfLite: Interpreter? = null
    private var detector: FaceDetector? = null
    private var registered = HashMap<String?, RecordRecognition.Recognition?>()
    private lateinit var embeddings: Array<FloatArray>
    private var REQUEST_CODE=102
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var useridx:String=""
    private var userxemail:String=""
    private var userxpass:String=""
    private var regipass=0

    var caseFile: File? = null
    var faceDetector: CascadeClassifier? = null
    private lateinit var scaled: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftarkan_karyawan)
        viewModel = ViewModelProvider(this).get(RegViewModel::class.java)
        viewModel!!.init(this)
//        detector = viewModel!!.detector
        auth= Firebase.auth
        useridx=Firebase.auth.currentUser?.uid.toString()
        embeddings = Array(1) { FloatArray(1) }
        regipass=0
//        database = Firebase.database.reference
//        database.child("perusahaan").child("tCukQl").child("anggota").get().addOnSuccessListener {
//            it.child("").key
//            Log.v("valueanggota",it.value.toString())
//            Log.v("valueanggotakey",it.key.toString())
//        }.addOnFailureListener {
//        }
//        val usersRef: DatabaseReference = database.child("perusahaan").child("tCukQl").child("anggota")
//        val valueEventListener: ValueEventListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (ds in dataSnapshot.children) {
//                    val uid = ds.key
//                    Log.d("listanggota", uid!!)
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) {}
//        }
//        usersRef.addListenerForSingleValueEvent(valueEventListener)


        try {
            tfLite = viewModel!!.getModel(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        btnMasukkanGambarWajahDaftarkan.setOnClickListener {
            openGalleryForImage()
        }
        btnDaftarkanKaryawanDaftarkan.setOnClickListener {

            if (editPasswordKaryawanDaftarkan.getText().toString() != editKonfirmasiPasswordKaryawanDaftarkan.getText()
                    .toString()
            ) {
                Toast.makeText(
                    baseContext, "Isi Password dan Confirm Password Berbeda",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val firebaseAppList = FirebaseApp.getApps(this)
                for (app in firebaseAppList) {
                    if (app.name == "secondary_db_auth") {
                        app.delete()
                        break
                    }
                }
                val firebaseOptionsBuilder = FirebaseOptions.Builder()
                firebaseOptionsBuilder.setApiKey("AIzaSyClgoyKN62rIiY45cjFRDDxM1Vf5QIE0fQ")
                firebaseOptionsBuilder.setDatabaseUrl("https://hidden-ad93d-default-rtdb.asia-southeast1.firebasedatabase.app")
                firebaseOptionsBuilder.setProjectId("hidden-ad93d")
                firebaseOptionsBuilder.setApplicationId("1:951410585581:android:2766f28c0558022e0dc1e4") //not sure if this one is needed
                val firebaseOptions = firebaseOptionsBuilder.build()

                val newAuth = FirebaseApp.initializeApp(this, firebaseOptions, "secondary_db_auth")

                try {
                    FirebaseAuth.getInstance(newAuth).createUserWithEmailAndPassword(
                        editEmailKaryawanDaftarkan.text.toString(),
                        editKonfirmasiPasswordKaryawanDaftarkan.text.toString()
                    )
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                val userKaryawan = FirebaseAuth.getInstance(newAuth).currentUser
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = editNamaKaryawanDaftarkan.text.toString()
                                }
                                userKaryawan!!.updateProfile(profileUpdates)
                                Log.v("namakaryawan", editNamaKaryawanDaftarkan.text.toString())
                                database = Firebase.database.reference
                                var perusahaanId = ""
                                Log.v("daftaruserid", useridx)
                                Log.v("daftaruseridkary", userKaryawan.uid)
                                database.child("users").child(useridx).child("perusahaan_id").get()
                                    .addOnSuccessListener {
                                        perusahaanId = it.value.toString()
                                        if (perusahaanId != "null") {
                                            writeNewAnggotaPerusahaan(
                                                userKaryawan.uid,
                                                perusahaanId
                                            )
                                            if(regipass==1){
                                                val result = RecordRecognition.Recognition()
                                                result.extra = embeddings
                                                registered[editNamaKaryawanDaftarkan.text.toString()] =
                                                    result
                                                Log.v(
                                                    "newinputjsonnama",
                                                    editNamaKaryawanDaftarkan.text.toString()
                                                )
                                                val newinputjsonstring = Gson().toJson(registered)
                                                Log.v("newinputjson", newinputjsonstring)
                                                database.child("users").child(userKaryawan.uid)
                                                    .child("registered_face")
                                                    .setValue(newinputjsonstring)
                                            }
                                            FirebaseAuth.getInstance(newAuth).signOut()
                                            Log.v("akhir", auth.currentUser!!.uid)
                                            Toast.makeText(
                                                baseContext, "Registrasi Akun Karyawan Berhasil",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        } else {
                                        }
                                    }.addOnFailureListener {
                                }
                            } else {

                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                baseContext, "Registrasi Akun Karyawan Gagal",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }catch (e:Exception){
                    Toast.makeText(
                        baseContext, "Registrasi Akun Karyawan Gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
//                auth2.createUserWithEmailAndPassword(
//                    editEmailKaryawanDaftarkan.getText().toString(), editKonfirmasiPasswordKaryawanDaftarkan.getText().toString()
//                )
//                    .addOnCompleteListener(this) { task ->
//                        if (task.isSuccessful) {
//                            val profileUpdates = userProfileChangeRequest {
//                                displayName = editNamaKaryawanDaftarkan.getText().toString()
//                            }
//                            auth2.currentUser!!.updateProfile(profileUpdates)
//                            database = Firebase.database.reference
//                            var perusahaanId=""
//                            Log.v("daftaruserid",useridx)
//                            Log.v("daftaruseridkary",auth2.currentUser!!.uid)
//                            database.child("users").child(useridx).child("perusahaan_id").get().addOnSuccessListener {
//                                perusahaanId=it.value.toString()
//                                if(perusahaanId!="null"){
//                                    writeNewAnggotaPerusahaan(auth2.currentUser!!.uid,perusahaanId)
//                                    auth2.signOut()
//                                    Log.v("akhir",auth.currentUser!!.uid)
//                                    Toast.makeText(
//                                        baseContext, "Registrasi Akun Karyawan Berhasil",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    finish()
//                                }
//                                else{
//                                }
//                            }.addOnFailureListener {
//                            }
//                        } else {
//                            Toast.makeText(
//                                baseContext, "Registrasi Akun tidak berhasil, silakan cek kembali data isian",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
            }
        }

    }
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val inputSize = 112
        val OUTPUT_SIZE = 192
        val IMAGE_MEAN = 128.0f
        val IMAGE_STD = 128.0f
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            imageGambarWajahDaftarkan.setImageURI(data?.data)
            val image: InputImage
            try {
//                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//                System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
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
                faceDetector!!.detectMultiScale(tmp, facedetections)
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
                                imageGambarWajahHasilDaftarkan.setImageBitmap(bmp)
                            }
                            scaled= BitmapUtils.getResizedBitmap(bmp, 112, 112)
                            val imgData: ByteBuffer =
                                getImgData(inputSize, scaled, IMAGE_MEAN, IMAGE_STD)
                            val inputArray = arrayOf<Any>(imgData)
                            val outputMap: MutableMap<Int, Any> = HashMap()
                            embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
                            outputMap[0] = embeddings
                            tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap!!)
                            regipass=1

                        } catch (e: CvException) {
                            Log.d("Exception", e.message!!)
                        }
                    }
                }
//                val result = detector!!.process(image).addOnSuccessListener { faces ->
//                        for (face in faces) {
//                            val imageUri: Uri = data?.data!!
//                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
//                            val boundingBox = RectF(face.boundingBox)
//                            var cropped_face: Bitmap =
//                                BitmapUtils.getCropBitmapByCPU(bitmap, boundingBox)
//                            val scaled: Bitmap =
//                                BitmapUtils.getResizedBitmap(cropped_face, 112, 112)
//                            imageGambarWajahHasilDaftarkan.setImageBitmap(scaled)
//                            val imgData: ByteBuffer =
//                                getImgData(inputSize, scaled, IMAGE_MEAN, IMAGE_STD)
//                            val inputArray = arrayOf<Any>(imgData)
//                            val outputMap: MutableMap<Int, Any> = HashMap()
//                            embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
//                            outputMap[0] = embeddings
//                            tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap!!)
//
//                        }
//
//                    }
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
    fun writeNewAnggotaPerusahaan(userId: String,perusahaanId:String) {
        database = Firebase.database.reference
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var randomstring=""
        for (i in 1..6) {
            val random1 = (0..charPool.size).shuffled().last()
            var tempchar=charPool[random1]
            randomstring=randomstring+tempchar
        }
        randomstring="A"+randomstring

        var userx=User(editNamaKaryawanDaftarkan.text.toString(),editEmailKaryawanDaftarkan.text.toString(),perusahaanId,randomstring,editNIKKaryawanDaftarkan.text.toString(),editAlamatKaryawanDaftarkan.text.toString(),
        editNoTeleponKaryawanDaftarkan.text.toString(),"karyawan")
        database.child("users").child(userId).setValue(userx)
        val anggota = AnggotaPerusahaan(
            randomstring,perusahaanId,userId,editBagianKaryawanDaftarkan.text.toString(),editAlamatKaryawanDaftarkan.text.toString(),
            editNoTeleponKaryawanDaftarkan.text.toString(),"aktif"
        )
        database.child("perusahaan").child(perusahaanId).child("anggota").child(randomstring).setValue(anggota)
        database.child("perusahaan").child(perusahaanId).child("anggota").child(randomstring).child("tanggal_masuk_perusahaan").setValue(ServerValue.TIMESTAMP)
    }
    data class AnggotaPerusahaan(val anggota_id: String? = null, val perusahaan_id:String?=null, val user_id:String?=null,
                          val bagian:String?=null,val alamat_anggota:String?=null, val no_telepon_anggota:String?=null,
                          val status_anggota:String?=null) {
    }
    data class User(val user_name:String?=null,val email_user:String?=null, val perusahaan_id:String?=null,val anggota_perusahaan_id:String?=null, val NIK:String?=null,
                                 val alamat_user:String?=null,
                                 val no_telepon_user:String?=null,val user_role:String?=null) {
    }

    override fun onStart() {
        super.onStart()
        regipass=0
    }

}