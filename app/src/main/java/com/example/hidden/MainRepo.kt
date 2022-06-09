package com.example.hidden

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList
import java.util.HashMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainRepo {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    fun readFromSP(): HashMap<String?, RecordRecognition.Recognition?>?{
        val OUTPUT_SIZE = 192
        val sharedPreferences = mContext!!.getSharedPreferences("HashMap", Context.MODE_PRIVATE)
        val defValue = Gson().toJson(HashMap<String?, RecordRecognition.Recognition?>())
        val json = sharedPreferences.getString("map", defValue)
        val token: TypeToken<HashMap<String?, RecordRecognition.Recognition?>?> =
            object : TypeToken<HashMap<String?, RecordRecognition.Recognition?>?>() {}
        val retrievedMap = Gson().fromJson<HashMap<String?, RecordRecognition.Recognition?>?>(json, token.type)
        for ((_, value) in retrievedMap) {
            val output = Array(1) {
                FloatArray(
                    OUTPUT_SIZE
                )
            }
            var arrayList = value?.extra as ArrayList<*>?
            arrayList = arrayList!![0] as ArrayList<*>
            for (counter in arrayList.indices) {
                output[0][counter] = (arrayList[counter] as Double).toFloat()
            }
            value?.extra = output
        }
        Toast.makeText(mContext, "Recognitions Loaded", Toast.LENGTH_SHORT).show()
        return retrievedMap
    }

    fun insertToSP(
        jsonMap: HashMap<String?, RecordRecognition.Recognition?>,
        clear: Boolean,
        registered: HashMap<String?, RecordRecognition.Recognition?>?
    ) {
        //untuk send ke database jsonmap
        val newinputjsonstring = Gson().toJson(jsonMap)
        Log.v("iniyangdisend",newinputjsonstring)
        var userID= Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        database.child(userID).child("registered_face").setValue(newinputjsonstring)

        if (clear) jsonMap.clear() else jsonMap.putAll(registered!!)
        val jsonString = Gson().toJson(jsonMap)
        val sharedPreferences = mContext!!.getSharedPreferences("HashMap", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("map", jsonString)
        editor.apply()
        Toast.makeText(mContext, "Recognitions Saved", Toast.LENGTH_SHORT).show()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var repo: MainRepo? = null
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null
        fun getInstance(context: Context?): MainRepo? {
            mContext = context
            if (repo == null) {
                repo = MainRepo()
            }
            return repo
        }
    }
}