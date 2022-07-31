package com.example.hidden

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main2.*
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.*
import org.opencv.core.Core.flip
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class MainActivity2 : AppCompatActivity(), CvCameraViewListener2 {
    var javaCameraView: JavaCameraView? = null
    var caseFile: File? = null
    var faceDetector: CascadeClassifier? = null
    private var mRgba: Mat? = null
    private  var mGrey:Mat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        javaCameraView = findViewById<View>(R.id.javaCameraView) as JavaCameraView
        javaCameraView!!.setCameraIndex(1);
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
            flip(rotated, rotated, 1) //transpose+flip(1)=CW
        } else if (rotflag == 2) {
            rotated = matImage.t()
            flip(rotated, rotated, 0) //transpose+flip(0)=CCW
        } else if (rotflag == 3) {
            flip(matImage, rotated, -1) //flip(-1)=180
        } else if (rotflag != 0) { //if not 0,1,2,3:
            Log.e("rotation", "Unknown rotation flag($rotflag)")
        }
        return rotated
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat? {
        mRgba = rot90(inputFrame.rgba(), 2);
        mGrey = rot90(inputFrame.gray(), 2);

        //detect Face
        val facedetections = MatOfRect()
        faceDetector!!.detectMultiScale(mRgba, facedetections)

        for (react in facedetections.toArray()) {
            if(react==facedetections.toArray()[0]) {
                Log.v("react", react.toString())
                var bmp: Bitmap? = null
                val tmp = Mat(400, 400, CvType.CV_8U, Scalar(4.0))
                var preview = Mat(mRgba, react)
                try {
                    Imgproc.cvtColor(preview, tmp, Imgproc.COLOR_mRGBA2RGBA);
//                Imgproc.cvtColor(preview, tmp, Imgproc.COLOR_GRAY2RGBA, 4)
                    bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(tmp, bmp)
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
}