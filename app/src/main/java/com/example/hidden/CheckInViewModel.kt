package com.example.hidden

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class CheckInViewModel: ViewModel() {
    private var repo: MainRepo? = null

    fun init(context: Context?) {
        repo = MainRepo.getInstance(context)
    }

    fun readFromSP(): java.util.HashMap<String?, RecordRecognition.Recognition?>? {
        return repo!!.readFromSP()
    }

    fun insertToSP(
        jsonMap: HashMap<String?, RecordRecognition.Recognition?>?,
        clear: Boolean,
        registered: HashMap<String?, RecordRecognition.Recognition?>?
    ) {
        repo!!.insertToSP(jsonMap!!, clear, registered)
    }

    @Throws(IOException::class)
    fun getModel(activity: Activity): Interpreter {
        val modelFile = "mobile_face_net.tflite"
        return Interpreter(loadModelFile(activity, modelFile))
    }

    val detector: FaceDetector
        get() {
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
            return FaceDetection.getClient(highAccuracyOpts)
        }

    @Throws(IOException::class)
    fun loadModelFile(activity: Activity, MODEL_FILE: String?): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE!!)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}