package com.example.hidden

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_absensi_harian.*

import kotlinx.android.synthetic.main.activity_informasi_absensi.*
import java.text.SimpleDateFormat
import java.util.*

class InformasiAbsensiActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    
    private lateinit var database: DatabaseReference
    private lateinit var perusahaanID:String
    private lateinit var anggotaPerusahaanID:String
    private lateinit var tanggal:String
    private lateinit var tanggalTahun:String
    private lateinit var tanggalBulan:String
    private lateinit var tanggalHari:String
    private lateinit var waktuJam:String
    private lateinit var waktuMenit:String
    private lateinit var waktuDetik:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_absensi)
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        database = Firebase.database.reference
        var userID= Firebase.auth.currentUser?.uid.toString()
        database.child("users").child(userID).get().addOnSuccessListener {
            perusahaanID=it.child("perusahaan_id").value.toString()
            anggotaPerusahaanID=it.child("anggota_perusahaan_id").value.toString()
        }
        database.child("timestamp").setValue(ServerValue.TIMESTAMP)
        database.child("timestamp").get().addOnSuccessListener {
            var timestamp=it.value
            tanggal=getDate(timestamp as Long)
            editTanggalInfoAbsensi.setText(tanggal)

        }.addOnFailureListener{
            Log.e("timestampfromdatabase", "Error getting data", it)
        }
        editTanggalInfoAbsensi.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, myear, mmonth, mdayOfMonth ->

                tanggalTahun=myear.toString()
                var intTanggalBulan=mmonth+1
                var intTanggalHari=mdayOfMonth
                if(intTanggalBulan-10>=0){
                    tanggalBulan=intTanggalBulan.toString()
                }else{
                    tanggalBulan="0"+intTanggalBulan.toString()
                }
                if(intTanggalHari-10>=0){
                    tanggalHari=intTanggalHari.toString()
                }
                else{
                    tanggalHari="0"+intTanggalHari.toString()
                }
                editTanggalInfoAbsensi.setText(""+ tanggalHari +"/"+ tanggalBulan +"/"+ tanggalTahun)
                
                            database.child("perusahaan").child(perusahaanID).child("anggota").child(anggotaPerusahaanID).get().addOnSuccessListener {
                                var user_id= it.child("user_id").value.toString()
                                database.child("users").child(user_id).get().addOnSuccessListener {
                                    var nama_user=it.child("user_name").value.toString()
                                    var gambar_user=it.child("gambar_user").value.toString()
                                    database.child("perusahaan").child(perusahaanID).child("absensi").child(tanggalTahun).child(tanggalBulan).child(tanggalHari).get().addOnSuccessListener {
                                        var wjm:String?
                                        var wmm:String?
                                        var wdm:String?
                                        var wjk:String?
                                        var wmk:String?
                                        var wdk:String?

                                        var longWaktuMasuk=it.child(anggotaPerusahaanID).child("jam_masuk").value
                                        var longWaktuKeluar=it.child(anggotaPerusahaanID).child("jam_keluar").value
                                        if(longWaktuMasuk!=null){
                                            getTime(longWaktuMasuk as Long)
                                            wjm=waktuJam
                                            wmm=waktuMenit
                                            wdm=waktuDetik
                                        }
                                        else{
                                            wjm=""
                                            wmm=""
                                            wdm=""
                                        }
                                        if(longWaktuKeluar!=null){
                                            getTime(longWaktuKeluar as Long)
                                            wjk=waktuJam
                                            wmk=waktuMenit
                                            wdk=waktuDetik
                                        }
                                        else{
                                            wjk=""
                                            wmk=""
                                            wdk=""
                                        }
                                        var lokasiLatitudeMasuk:String?
                                        var lokasiLongitudeMasuk:String?
                                        var lokasiLatitudeKeluar:String?
                                        var lokasiLongitudeKeluar:String?

                                        var strlokasiLatitudeMasuk=it.child(anggotaPerusahaanID).child("lokasi_latitude_masuk").value.toString()
                                        var strlokasiLongitudeMasuk=it.child(anggotaPerusahaanID).child("lokasi_longitude_masuk").value.toString()
                                        var strlokasiLatitudeKeluar=it.child(anggotaPerusahaanID).child("lokasi_latitude_keluar").value.toString()
                                        var strlokasiLongitudeKeluar=it.child(anggotaPerusahaanID).child("lokasi_longitude_keluar").value.toString()
                                        if(it.child(anggotaPerusahaanID).child("lokasi_latitude_masuk").value!=null){
                                            lokasiLatitudeMasuk=strlokasiLatitudeMasuk
                                            lokasiLongitudeMasuk=strlokasiLongitudeMasuk
                                        }
                                        else{
                                            lokasiLatitudeMasuk=""
                                            lokasiLongitudeMasuk=""
                                        }
                                        if(it.child(anggotaPerusahaanID).child("lokasi_latitude_keluar").value!=null){
                                            lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                            lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                        }
                                        else{
                                            lokasiLatitudeKeluar=""
                                            lokasiLongitudeKeluar=""
                                        }
                                        Log.v("listdata","${nama_user}+,+${wjm}+,+$wjk+,+$wmm+,+$wmk+,+$wdm+,+$wdk+,+$lokasiLatitudeMasuk+,+$lokasiLatitudeKeluar+,+$lokasiLongitudeMasuk+,+$lokasiLongitudeKeluar")
                                        Log.v("jammasuk",wjm)
                                    }

                                }
                                Log.v("user_id",user_id)

                            }.addOnFailureListener {
                            }
            }, year, month, day)
            datePickerDialog.show()

        }
        
    }
    private fun getDate(time: Long?): String {
        val format = "dd/MM/yyyy"
        val formatTahun = "yyyy"
        val formatBulan = "MM"
        val formatHari = "dd"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val sdfTahun = SimpleDateFormat(formatTahun, Locale.getDefault())
        val sdfBulan = SimpleDateFormat(formatBulan, Locale.getDefault())
        val sdfHari = SimpleDateFormat(formatHari, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        //        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        tanggalTahun=sdfTahun.format(Date(time!!))
        tanggalBulan=sdfBulan.format(Date(time))
        tanggalHari=sdfHari.format(Date(time))
        Log.v("tanggalTahunBulanHari", tanggalTahun+" "+tanggalBulan+" "+tanggalHari)
        return sdf.format(Date(time))
    }
    private fun getTime(time: Long?): String {
        val format = "HH:mm:ss"
        val formatJam = "HH"
        val formatMenit = "mm"
        val formatDetik = "ss"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val sdfJam = SimpleDateFormat(formatJam, Locale.getDefault())
        val sdfMenit = SimpleDateFormat(formatMenit, Locale.getDefault())
        val sdfDetik = SimpleDateFormat(formatDetik, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
//        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        waktuJam=sdfJam.format(Date(time!!))
        waktuMenit=sdfMenit.format(Date(time))
        waktuDetik=sdfDetik.format(Date(time))
        Log.v("gettime", sdf.format(Date(time)))
        return sdf.format(Date(time))
    }
}