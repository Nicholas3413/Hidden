package com.example.hidden

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toIcon
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.hidden.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector

import kotlinx.android.synthetic.main.activity_reg_wajah_pemilik.*
import kotlinx.android.synthetic.main.activity_reg_wajah_pemilik.camera_switch
import org.opencv.android.*
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
import java.util.HashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class RegWajahPemilikActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2  {
    private var viewModel: RegViewModel? = null
    private var tfLite: Interpreter? = null
    private var detector: FaceDetector? = null
    private var registered = HashMap<String?, RecordRecognition.Recognition?>()
    private lateinit var embeddings: Array<FloatArray>
    private lateinit var tempembeddings: Array<FloatArray>
    private var cam_face = CameraSelector.LENS_FACING_FRONT
    private var flipX = true
    private var start = true
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var scaled:Bitmap

    var javaCameraView: JavaCameraView? = null
    var caseFile: File? = null
    var faceDetector: CascadeClassifier? = null
    private var mRgba: Mat? = null
    private  var mGrey:Mat? = null
    private var mCameraIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reg_wajah_pemilik)
        viewModel = ViewModelProvider(this).get(RegViewModel::class.java)
        viewModel!!.init(this)
        embeddings = Array(1) { FloatArray(1) }
        checkCameraPermission()
        registered = viewModel!!.readFromSP()!!
        try {
            tfLite = viewModel!!.getModel(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        onJCameraBind()
//        detector = viewModel!!.detector
//        onCameraBind()
//        camera_switch.setOnClickListener {
//            if (cam_face == CameraSelector.LENS_FACING_BACK) {
//                cam_face = CameraSelector.LENS_FACING_FRONT
//                flipX = true
//            } else {
//                cam_face = CameraSelector.LENS_FACING_BACK
//                flipX = false
//            }
//            cameraProvider!!.unbindAll()
//            onCameraBind()
//        }
        camera_switch.setOnClickListener {
            if (mCameraIndex==1) {
                mCameraIndex = 0
                javaCameraView!!.disableView()
                javaCameraView!!.setCameraIndex(mCameraIndex);
                javaCameraView!!.enableView()
                Log.v("cameraindex",mCameraIndex.toString())
            } else {
                mCameraIndex = 1
                javaCameraView!!.disableView()
                javaCameraView!!.setCameraIndex(mCameraIndex);
                javaCameraView!!.enableView()
                Log.v("cameraindex",mCameraIndex.toString())
            }
        }
        btnRegisterWajahPemilik.setOnClickListener {
            if(embeddings[0].size!=1) {
                tempembeddings = embeddings
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Register Wajah?")
                builder.setMessage("Apakah anda yakin menggunakan wajah ini?")
                builder.setPositiveButton("Ya") { dialog, which ->
                    auth = Firebase.auth
                    var namaUser = auth.currentUser?.displayName.toString()
                    val result = RecordRecognition.Recognition()
                    result.extra = tempembeddings
                    registered[namaUser] = result
                    viewModel!!.insertToSP(registered, false, viewModel!!.readFromSP())
                    val intent = Intent(this, RegPerusahaanActivity::class.java)
                    startActivity(intent)
                }
                builder.setNegativeButton("Tidak") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "Tidak", Toast.LENGTH_SHORT
                    ).show()
                }
                builder.show()
            }
        }
    }

    private fun onJCameraBind(){
        javaCameraView = findViewById<View>(R.id.javaCameraView) as JavaCameraView

        javaCameraView!!.setCameraIndex(mCameraIndex);
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback)
        } else {
            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        javaCameraView!!.setCvCameraViewListener(this)
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
                    javaCameraView!!.enableView()
                }
                else -> super.onManagerConnected(status)
            }
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat()
        mGrey = Mat()
    }

    override fun onCameraViewStopped() {
        mRgba!!.release()
        mGrey!!.release()
    }

    fun rot90(matImage: Mat, rotflag: Int): Mat? {
        //1=CW, 2=CCW, 3=180
        var rotated: Mat? = Mat()
        if (rotflag == 1) {
            rotated = matImage.t()
            Core.flip(rotated, rotated, 1) //transpose+flip(1)=CW
        } else if (rotflag == 2) {
            rotated = matImage.t()
            Core.flip(rotated, rotated, 0) //transpose+flip(0)=CCW
        } else if (rotflag == 3) {
            Core.flip(matImage, rotated, -1) //flip(-1)=180
        } else if (rotflag != 0) { //if not 0,1,2,3:
            Log.e("rotation", "Unknown rotation flag($rotflag)")
        }
        return rotated
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat? {
        if(mCameraIndex==1) {
            mRgba = rot90(inputFrame.rgba(), 2);
            Core.flip(mRgba,mRgba,1)
            mGrey = rot90(inputFrame.gray(), 2);
        }
        else{
            mRgba = rot90(inputFrame.rgba(), 1);
            mGrey = rot90(inputFrame.gray(), 1);
        }
        //detect Face
        val facedetections = MatOfRect()
        faceDetector!!.detectMultiScale(mRgba, facedetections)

        for (react in facedetections.toArray()) {
            if(react==facedetections.toArray()[0]) {
                Log.v("react", react.toString())
                var bmp: Bitmap? = null
                val tmp = Mat(112, 112, CvType.CV_8U, Scalar(4.0))
                var preview = Mat(mRgba, react)
                try {
                    Imgproc.cvtColor(preview, tmp, Imgproc.COLOR_mRGBA2RGBA);
//                Imgproc.cvtColor(preview, tmp, Imgproc.COLOR_GRAY2RGBA, 4)
                    bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(tmp, bmp)
//                    bmp=Bitmap.createBitmap(bmp,0,0,112,112)


//                    val frame_bmp1: Bitmap =
//                        BitmapUtils.rotateBitmap(bmp, 0, false, false)
//
                    scaled= BitmapUtils.getResizedBitmap(bmp, 112, 112)

                    if (start) {
                        recognizeImage(scaled)
                    }

                    runOnUiThread {
                        imagePreview.setImageBitmap(bmp)
                    }


                } catch (e: CvException) {
                    Log.d("Exception", e.message!!)
                }
            }
            Imgproc.rectangle(
                mRgba, Point(react.x.toDouble(), react.y.toDouble()),
                Point((react.x + react.width).toDouble(), (react.y + react.height).toDouble()),
                Scalar(255.0, 0.0, 0.0)
            )
        }
        return mRgba
    }

//    private fun onCameraBind(){
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener(Runnable {
//            cameraProvider = cameraProviderFuture.get()
//            bindPreview(cameraProvider!!)
//        }, ContextCompat.getMainExecutor(this))
//    }

//    @SuppressLint("UnsafeOptInUsageError")
//    fun bindPreview(cameraProvider : ProcessCameraProvider) {
//        Log.v("sudah disini","sudah disini")
//        var preview : Preview = Preview.Builder()
//            .build()
//
//        var cameraSelector : CameraSelector = CameraSelector.Builder()
//            .requireLensFacing(cam_face)
//            .build()
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider())
//        val imageAnalysis:ImageAnalysis=buildImageAnalysisUseCase()
//        val executor: Executor = Executors.newSingleThreadExecutor()
//        imageAnalysis.setAnalyzer(executor) { image ->
//            val rotationDegrees = image.imageInfo.rotationDegrees
//            var imagex: InputImage? = null
//            Log.v("sudah disini2","sudah disini")
//            val mediaImage = image.image
//
//            if (mediaImage != null) {
//                imagex = InputImage.fromMediaImage(
//                    mediaImage,
//                    rotationDegrees
//                )
//            }
//            val result=detector!!.process(imagex!!).addOnSuccessListener { faces: List<Face> ->
//                if (faces.size != 0) {
//                    Log.v("terdeteksi","sudah terdeteksi wajah")
//                    val face = faces[0]
//                    val frame_bmp: Bitmap = BitmapUtils.toBitmap(mediaImage!!)
//                    val frame_bmp1: Bitmap =
//                        BitmapUtils.rotateBitmap(frame_bmp, rotationDegrees, false, false)
//                    val boundingBox = RectF(face.boundingBox)
//                    var cropped_face: Bitmap =
//                        BitmapUtils.getCropBitmapByCPU(frame_bmp1, boundingBox)
//                    if (flipX) {
//                        cropped_face =
//                            BitmapUtils.rotateBitmap(cropped_face, 0, true, false)
//                    }
//                    scaled= BitmapUtils.getResizedBitmap(cropped_face, 112, 112)
//                    if (start) {
//                        recognizeImage(scaled)
//                    }
//                    face_preview.setImageBitmap(scaled)
//                    try {
//                        Thread.sleep(10)
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
//                }
//            }.addOnCompleteListener {  image.close() }
//        }
//        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview,imageAnalysis)
//    }

    private lateinit var auth: FirebaseAuth
    fun recognizeImage(
        bitmap: Bitmap?
    ) {
        val inputSize = 112
        val OUTPUT_SIZE = 192
        val IMAGE_MEAN = 128.0f
        val IMAGE_STD = 128.0f
        val imgData: ByteBuffer =
            getImgData(inputSize, bitmap!!, IMAGE_MEAN, IMAGE_STD)
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: MutableMap<Int, Any> = HashMap()
        embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
        outputMap[0] = embeddings
        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap)
        for (row in embeddings) {
            Log.v("iniisiembeddings",row.contentToString())
        }
        //tes add face ke database

//        auth= Firebase.auth
//        var namaUser = auth.currentUser?.displayName.toString()
//        val result = RecordRecognition.Recognition(
//        )
//        result.extra = embeddings
//        registered[namaUser] = result
//        viewModel!!.insertToSP(registered, false, viewModel!!.readFromSP())

    }
    private fun getImgData(inputSize: Int,
                           bitmap: Bitmap,
                           IMAGE_MEAN: Float,
                           IMAGE_STD: Float
    ): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        Log.v("xtesimgdata", imgData.toString())
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
    fun buildImageAnalysisUseCase(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    private fun checkCameraPermission() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
            }
        }
    }
    companion object {
        private const val MY_CAMERA_REQUEST_CODE = 100
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}