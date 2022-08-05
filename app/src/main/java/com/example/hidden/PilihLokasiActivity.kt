package com.example.hidden

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_pilih_lokasi.*

class PilihLokasiActivity : AppCompatActivity() {
    private var locationRequest: LocationRequest? = null
    private var xlatitude:String?=""
    private var xlongitude:String?=""
    private var new_latitude_pos:Double?=null
    private var new_latitude_min:Double?=null
    private var new_longitude_pos:Double?=null
    private var new_longitude_min:Double?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_lokasi)
        val sharedPreferences = getSharedPreferences("Location", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear().apply()
        locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest?.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest?.setInterval(5000);
        locationRequest?.setFastestInterval(2000);
        btnDapatkanLokasiSaatIniPilihLokasi.setOnClickListener {
            getCurrentLocation()
            btnLihatLokasiPilihLokasi.visibility= View.VISIBLE
        }
        btnLihatLokasiPilihLokasi.setOnClickListener {
            val list = editLokasiAndaPilihLokasi.text.toString().trim().split(",")
            try{
                if(list.size==2) {
                    if (list[0] != "" || list[1] != "") {
                        var cek1: Double = list[0].toDouble()
                        var cek2: Double = list[1].toDouble()

                        val gmmIntentUri =
                            Uri.parse("geo:" + editLokasiAndaPilihLokasi.text.toString())
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        mapIntent.resolveActivity(packageManager)?.let {
                            startActivity(mapIntent)
                        }
                    }
                    else{
                        Toast.makeText(
                            baseContext, "Silakan cek kembali lokasi input isian, format ada kosong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else{
                    Toast.makeText(
                        baseContext, "Silakan cek kembali lokasi input isian, format ada salah",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }catch (e:Exception){
                Toast.makeText(
                    baseContext, "Silakan cek kembali lokasi input isian, format ada salah",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        btnHitungMeterPilihLokasi.setOnClickListener {
            if(editBerapaMeterPilihLokasi.getText().toString()!="") {
                val list = editLokasiAndaPilihLokasi.text.toString().trim().split(",")
                try {
                    if (list.size == 2) {
                        if (list[0] != "" || list[1] != "") {
                            var meter: Double =
                                editBerapaMeterPilihLokasi.getText().toString().toDouble()
                            var minusmeter = meter * -1
                            val earth = 6378.137
                            //radius of the earth in kilometer
                            val pi = Math.PI
                            val m: Double = 1 / (2 * pi / 360 * earth) / 1000 //1 meter in degree
                            val list = editLokasiAndaPilihLokasi.text.toString().split(",")
                            new_latitude_pos = list[0].toDouble()?.plus(meter * m)
                            new_latitude_min = list[0].toDouble()?.plus(minusmeter * m)
                            new_longitude_pos =
                                list[1].toDouble()
                                    ?.plus(meter * m / Math.cos(list[0].toDouble() * (pi / 180)))
                            new_longitude_min = list[1].toDouble()
                                ?.plus(minusmeter * m / Math.cos(list[0].toDouble() * (pi / 180)))
                            txtNewLokasiMeterPilihLokasi.setText("latitude baru: " + new_latitude_min.toString() + " sampai " + new_latitude_pos.toString() + "\nlongitude baru: " + new_longitude_min.toString() + " sampai " + new_longitude_pos.toString())
                        }
                    } else {
                        Toast.makeText(
                            baseContext, "Silakan cek kembali lokasi input isian, format ada salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        baseContext, "Silakan cek kembali lokasi input isian, format ada salah",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else{
                Toast.makeText(
                    baseContext, "Silakan cek kembali isian tentukan berapa meter, masih kosong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        btnKonfirmasiTitikLokasiPilihLokasi.setOnClickListener {
            if(new_longitude_min!=null) {
                editor.putString("new_latitude_pos", new_latitude_pos.toString())
                editor.putString("new_latitude_min", new_latitude_min.toString())
                editor.putString("new_longitude_pos", new_longitude_pos.toString())
                editor.putString("new_longitude_min", new_longitude_min.toString())
                val list = editLokasiAndaPilihLokasi.text.toString().split(",")
                editor.putString("latitude", list[0])
                editor.putString("longitude", list[1])
                editor.apply()
                Toast.makeText(
                    baseContext, "Konfirmasi titik lokasi berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            else{
                Toast.makeText(
                    baseContext, "Silakan tentukan titik lokasi dan tekan tombol hitung terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(this)
                        .requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                LocationServices.getFusedLocationProviderClient(this@PilihLokasiActivity)
                                    .removeLocationUpdates(this)
                                if (locationResult != null && locationResult.locations.size > 0) {
                                    val index = locationResult.locations.size - 1
                                    val latitude = locationResult.locations[index].latitude
                                    xlatitude=latitude.toString()
                                    val longitude = locationResult.locations[index].longitude
                                    xlongitude=longitude.toString()
//                                    txtLokasiAndaPilihLokasi.setText("Latitude: $latitude\nLongitude: $longitude")
                                    editLokasiAndaPilihLokasi.setText(xlatitude+","+xlongitude)
                                }
                            }
                        }, Looper.getMainLooper())
                } else {
                    turnOnGPS()
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }
    private fun isGPSEnabled(): Boolean {
        var locationManager: LocationManager? = null
        var isEnabled = false
        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled
    }
    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(
            applicationContext
        )
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Toast.makeText(this, "GPS is already turned on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this, 2)
                    } catch (ex: IntentSender.SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation()
                } else {
                    turnOnGPS()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation()
            }
        }
    }
}