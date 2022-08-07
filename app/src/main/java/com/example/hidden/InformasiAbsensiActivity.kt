package com.example.hidden

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
    private var workHoursDay:Double=0.0
    private var workHoursWeek:Double=0.0
    private lateinit var arr1:List<String>
    private var tempWorkHoursWeek:Double=0.0
    private val dataentry: ArrayList<BarEntry> = ArrayList()
    private var countData:Float=1F
    private val DAYS = arrayOf("SEN","SEL","RAB","KAM","JUM","SAB","MIN")
    private val hari = arrayOf("Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu")
    private val listTanggal = arrayOf("a","b","c","d","e","f","g")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_absensi)
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        countData=1F
        txtNamaInfoAbsensi.setOnClickListener {
            val barDataSet = BarDataSet(dataentry, "Waktu Kerja (jam)")
            barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
            barDataSet.setValueTextColor(Color.WHITE)
            barDataSet.valueTextSize = 16f

            val barData = BarData(barDataSet)

            barchartInfoAbsensi.setFitBars(true)
            barchartInfoAbsensi.setData(barData)
            barchartInfoAbsensi.getDescription().setText("Chart Waktu Kerja")
            barchartInfoAbsensi.animateY(2000)
            val xAxis: XAxis = barchartInfoAbsensi.getXAxis()
            xAxis.textColor=Color.WHITE
            xAxis.setGranularity(1f);
            val yAxis: YAxis = barchartInfoAbsensi.axisLeft
            yAxis.textColor=Color.WHITE
            val yAxis2: YAxis = barchartInfoAbsensi.axisRight
            yAxis2.textColor=Color.WHITE
            val l: Legend = barchartInfoAbsensi.getLegend()
            l.textColor=Color.WHITE
        }
        database = Firebase.database.reference
        var userID= Firebase.auth.currentUser?.uid.toString()
        database.child("users").child(userID).get().addOnSuccessListener {
            perusahaanID=it.child("perusahaan_id").value.toString()
            anggotaPerusahaanID=it.child("anggota_perusahaan_id").value.toString()
            txtNamaInfoAbsensi.setText(it.child("user_name").value.toString())
            database.child("perusahaan").child(perusahaanID).get().addOnSuccessListener {
                workHoursDay = it.child("work_hours_day").value.toString().toDouble()
                workHoursWeek = it.child("work_hours_week").value.toString().toDouble()
                database.child("timestamp").setValue(ServerValue.TIMESTAMP)
                database.child("timestamp").get().addOnSuccessListener {
                    var timestamp=it.value
                    tanggal=getDate(timestamp as Long)
                    editTanggalInfoAbsensi.setText(tanggal)
                    tempWorkHoursWeek=0.0
                    val calendar: Calendar = Calendar.getInstance(Locale.UK)
                    calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                    val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]

                    txtInformasiIsiInfoAbsensi.setText("")
                    val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                    c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                    val t = c.time;
                    val firstDay = c.firstDayOfWeek
                    c.set(Calendar.DAY_OF_WEEK,firstDay)
                    var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=startDate.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[0]=startDate
                    Log.v("arr1",arr1.toString())
                    c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                    var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=endDate1.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[1]=endDate1
                    c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                    var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=endDate2.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[2]=endDate2
                    c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                    var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=endDate3.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[3]=endDate3
                    c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                    var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=endDate4.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[4]=endDate4
                    c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                    var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=endDate5.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[5]=endDate5
                    c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                    var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                    arr1=endDate6.split("-")
                    hitungLamaWaktu(arr1)
                    listTanggal[6]=endDate6
//                    val barDataSet = BarDataSet(dataentry, "Waktu Kerja (jam)")
//                    barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
//                    barDataSet.setValueTextColor(Color.WHITE)
//                    barDataSet.valueTextSize = 16f
//
//                    val barData = BarData(barDataSet)
//
//                    barchartInfoAbsensi.setFitBars(true)
//                    barchartInfoAbsensi.setData(barData)
//                    barchartInfoAbsensi.getDescription().setText("Chart Waktu Kerja")
//                    barchartInfoAbsensi.animateY(2000)
//                    val xAxis: XAxis = barchartInfoAbsensi.getXAxis()
//                    xAxis.textColor=Color.WHITE
//                    xAxis.setLabelCount(1)
//                    xAxis.setGranularity(1f);
//                    val yAxis: YAxis = barchartInfoAbsensi.axisLeft
//                    yAxis.textColor=Color.WHITE
//                    val yAxis2: YAxis = barchartInfoAbsensi.axisRight
//                    yAxis2.textColor=Color.WHITE

                    txtWaktuSemingguInfoAbsensi.setText("Senin, "+startDate+" sampai Minggu, "+endDate6)
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
                                    txtLokasiMasukIsiInfoAbsensi.setText(lokasiLatitudeMasuk+","+lokasiLongitudeMasuk)
                                    txtLokasiMasukIsiInfoAbsensi.setOnClickListener {
                                        val gmmIntentUri = Uri.parse(
                                            "geo:" + lokasiLatitudeMasuk + "," + lokasiLongitudeMasuk
                                        )
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        mapIntent.resolveActivity(packageManager)?.let {
                                            startActivity(mapIntent)
                                        }
                                    }
                                }
                                else{
                                    lokasiLatitudeMasuk=""
                                    lokasiLongitudeMasuk=""
                                    txtLokasiMasukIsiInfoAbsensi.setText("-")
                                    txtLokasiMasukIsiInfoAbsensi.setOnClickListener(null)
                                }
                                if(it.child(anggotaPerusahaanID).child("lokasi_latitude_keluar").value!=null){
                                    lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                    lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                    txtLokasiKeluarIsiInfoAbsensi.setText(lokasiLatitudeKeluar+","+lokasiLongitudeKeluar)
                                    txtLokasiKeluarIsiInfoAbsensi.setOnClickListener {
                                        val gmmIntentUri = Uri.parse(
                                            "geo:" + lokasiLatitudeKeluar + "," + lokasiLongitudeKeluar
                                        )
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        mapIntent.resolveActivity(packageManager)?.let {
                                            startActivity(mapIntent)
                                        }
                                    }
                                }
                                else{
                                    lokasiLatitudeKeluar=""
                                    lokasiLongitudeKeluar=""
                                    txtLokasiKeluarIsiInfoAbsensi.setText("-")
                                    txtLokasiKeluarIsiInfoAbsensi.setOnClickListener(null)
                                }

                                Log.v("listdata","${nama_user}+,+${wjm}+,+$wjk+,+$wmm+,+$wmk+,+$wdm+,+$wdk+,+$lokasiLatitudeMasuk+,+$lokasiLatitudeKeluar+,+$lokasiLongitudeMasuk+,+$lokasiLongitudeKeluar")
                                Log.v("jammasuk",wjm)
                                txtAbsensiMasukIsiInfoAbsensi.setText(wjm.toString().padStart(2, '0')+":"+wmm.toString().padStart(2, '0')+":"+wdm.toString().padStart(2, '0'))
                                txtAbsensiKeluarIsiInfoAbsensi.setText(wjk.toString().padStart(2, '0')+":"+wmk.toString().padStart(2, '0')+":"+wdk.toString().padStart(2, '0'))

                                if(longWaktuKeluar!=null&&longWaktuMasuk!=null) {
                                    var longWaktuSelisih = longWaktuKeluar as Long - longWaktuMasuk as Long
                                    getTime(longWaktuSelisih as Long)
                                    var sj=(wjk.toInt()-wjm.toInt())
                                    var sm=(wmk.toInt()-wmm.toInt())
                                    var sd=(wdk.toInt()-wdm.toInt())
                                    if(sm<0){
                                        sm=60+sm
                                        sj=sj-1
                                    }
                                    if(sd<0){
                                        sd=60+sd
                                        sm=sm-1
                                    }
                                    if(longWaktuMasuk<=longWaktuKeluar){
                                        txtSelisihWaktuIsiInfoAbsensi.setText(sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))

                                    }

                                }
                            }
                        }
                        Log.v("user_id",user_id)

                    }.addOnFailureListener {
                    }
                }.addOnFailureListener{
                    Log.e("timestampfromdatabase", "Error getting data", it)
                }
            }
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
                dataentry.clear()
                txtSelisihWaktuIsiInfoAbsensi.setText("00:00")
                countData=1F
                tempWorkHoursWeek=0.0
                val calendar: Calendar = Calendar.getInstance(Locale.UK)
//                calendar[tanggalTahun.toInt(), tanggalBulan.toInt()] = tanggalHari.toInt()
                calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]
//                calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
//                val firstDayOfWeek: Int = calendar.getFirstDayOfWeek()
//
//                // Start date
//
//                // Start date
//                calendar.set(Calendar.DAY_OF_WEEK, 1)
//                val startDate= calendar
//                // End date
//
//                // End date
////                calendar.set(Calendar.DAY_OF_WEEK, 7)
//                val endDate = calendar
                txtInformasiIsiInfoAbsensi.setText("")
                val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                val t = c.time;
                val firstDay = c.firstDayOfWeek
                c.set(Calendar.DAY_OF_WEEK,firstDay)
                var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=startDate.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[0]=startDate
                Log.v("arr1",arr1.toString())
                c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate1.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[1]=endDate1
                c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate2.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[2]=endDate2
                c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate3.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[3]=endDate3
                c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate4.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[4]=endDate4
                c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate5.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[5]=endDate5
                c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate6.split("-")
                hitungLamaWaktu(arr1)
                listTanggal[6]=endDate6

                val barDataSet = BarDataSet(dataentry, "Waktu Kerja (jam)")
                barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                barDataSet.setValueTextColor(Color.WHITE)
                barDataSet.valueTextSize = 16f

                val barData = BarData(barDataSet)

                barchartInfoAbsensi.setFitBars(true)
                barchartInfoAbsensi.setData(null)
//                barchartInfoAbsensi.setData(barData)
                barchartInfoAbsensi.getDescription().setText("Chart Waktu Kerja")
                barchartInfoAbsensi.animateY(2000)
                val xAxis: XAxis = barchartInfoAbsensi.getXAxis()
                xAxis.textColor=Color.WHITE
                xAxis.setLabelCount(7)
                xAxis.setGranularity(1f);
                val yAxis: YAxis = barchartInfoAbsensi.axisLeft
                yAxis.textColor=Color.WHITE
                val yAxis2: YAxis = barchartInfoAbsensi.axisRight
                yAxis2.textColor=Color.WHITE
                txtWaktuSemingguInfoAbsensi.setText("Senin, "+startDate+" sampai Minggu, "+endDate6)
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
                                            txtLokasiMasukIsiInfoAbsensi.setText(lokasiLatitudeMasuk+","+lokasiLongitudeMasuk)
                                            txtLokasiMasukIsiInfoAbsensi.setOnClickListener {
                                                val gmmIntentUri = Uri.parse(
                                                    "geo:" + lokasiLatitudeMasuk + "," + lokasiLongitudeMasuk
                                                )
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                mapIntent.setPackage("com.google.android.apps.maps")
                                                mapIntent.resolveActivity(packageManager)?.let {
                                                    startActivity(mapIntent)
                                                }
                                            }
                                        }
                                        else{
                                            lokasiLatitudeMasuk=""
                                            lokasiLongitudeMasuk=""
                                            txtLokasiMasukIsiInfoAbsensi.setText("-")
                                            txtLokasiMasukIsiInfoAbsensi.setOnClickListener(null)
                                        }
                                        if(it.child(anggotaPerusahaanID).child("lokasi_latitude_keluar").value!=null){
                                            lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                            lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                            txtLokasiKeluarIsiInfoAbsensi.setText(lokasiLatitudeKeluar+","+lokasiLongitudeKeluar)
                                            txtLokasiKeluarIsiInfoAbsensi.setOnClickListener {
                                                val gmmIntentUri = Uri.parse(
                                                    "geo:" + lokasiLatitudeKeluar + "," + lokasiLongitudeKeluar
                                                )
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                mapIntent.setPackage("com.google.android.apps.maps")
                                                mapIntent.resolveActivity(packageManager)?.let {
                                                    startActivity(mapIntent)
                                                }
                                            }
                                        }
                                        else{
                                            lokasiLatitudeKeluar=""
                                            lokasiLongitudeKeluar=""
                                            txtLokasiKeluarIsiInfoAbsensi.setText("-")
                                            txtLokasiKeluarIsiInfoAbsensi.setOnClickListener(null)
                                        }
                                        Log.v("listdata","${nama_user}+,+${wjm}+,+$wjk+,+$wmm+,+$wmk+,+$wdm+,+$wdk+,+$lokasiLatitudeMasuk+,+$lokasiLatitudeKeluar+,+$lokasiLongitudeMasuk+,+$lokasiLongitudeKeluar")
                                        txtAbsensiMasukIsiInfoAbsensi.setText(wjm.toString().padStart(2, '0')+":"+wmm.toString().padStart(2, '0')+":"+wdm.toString().padStart(2, '0'))
                                        txtAbsensiKeluarIsiInfoAbsensi.setText(wjk.toString().padStart(2, '0')+":"+wmk.toString().padStart(2, '0')+":"+wdk.toString().padStart(2, '0'))

                                        if(longWaktuKeluar!=null&&longWaktuMasuk!=null) {
                                            var longWaktuSelisih = longWaktuKeluar as Long - longWaktuMasuk as Long
                                            getTime(longWaktuSelisih as Long)
                                            var sj=(wjk!!.toInt()-wjm!!.toInt())
                                            var sm=(wmk!!.toInt()-wmm!!.toInt())
                                            var sd=(wdk!!.toInt()-wdm!!.toInt())
                                            if(sm<0){
                                                sm=60+sm
                                                sj=sj-1
                                            }
                                            if(sd<0){
                                                sd=60+sd
                                                sm=sm-1
                                            }
                                            if(longWaktuMasuk<=longWaktuKeluar){
                                                txtSelisihWaktuIsiInfoAbsensi.setText(sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))

                                            }

                                        }
                                    }
//                                    database.child("perusahaan").child(perusahaanID).child("absensi").child(arr5[0]).child(arr5[1]).child(arr5[2]).get().addOnSuccessListener {
//                                        var wjm:String?
//                                        var wmm:String?
//                                        var wdm:String?
//                                        var wjk:String?
//                                        var wmk:String?
//                                        var wdk:String?
//
//                                        var longWaktuMasuk=it.child(anggotaPerusahaanID).child("jam_masuk").value
//                                        var longWaktuKeluar=it.child(anggotaPerusahaanID).child("jam_keluar").value
//                                        if(longWaktuMasuk!=null){
//                                            getTime(longWaktuMasuk as Long)
//                                            wjm=waktuJam
//                                            wmm=waktuMenit
//                                            wdm=waktuDetik
//                                        }
//                                        else{
//                                            wjm=""
//                                            wmm=""
//                                            wdm=""
//                                        }
//                                        if(longWaktuKeluar!=null){
//                                            getTime(longWaktuKeluar as Long)
//                                            wjk=waktuJam
//                                            wmk=waktuMenit
//                                            wdk=waktuDetik
//                                        }
//                                        else{
//                                            wjk=""
//                                            wmk=""
//                                            wdk=""
//                                        }
//                                        if(longWaktuKeluar!=null&&longWaktuMasuk!=null) {
//                                            var longWaktuSelisih = longWaktuKeluar as Long - longWaktuMasuk as Long
//                                            getTime(longWaktuSelisih as Long)
//                                            var sj=(wjk!!.toInt()-wjm!!.toInt())
//                                            var sm=(wmk!!.toInt()-wmm!!.toInt())
//                                            var sd=(wdk!!.toInt()-wdm!!.toInt())
//                                            if(sm<0){
//                                                sm=60+sm
//                                                sj=sj-1
//                                            }
//                                            if(sd<0){
//                                                sd=60+sd
//                                                sm=sm-1
//                                            }
//                                            if(longWaktuMasuk<=longWaktuKeluar){
//                                                Log.v("selisihwaktusenin",sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))
//
//                                            }
//
//                                        }
//                                    }

                                }
                                Log.v("user_id",user_id)

                            }.addOnFailureListener {
                            }
            }, year, month, day)
            datePickerDialog.show()

        }
        
    }
    private fun createBar(){
        barchartInfoAbsensi.getDescription().setEnabled(false);
        barchartInfoAbsensi.setDrawValueAboveBar(false);
        val barDataSet = BarDataSet(dataentry, "Waktu Kerja (jam)")
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        barDataSet.setValueTextColor(Color.WHITE)
        barDataSet.valueTextSize = 12f
        val barData = BarData(barDataSet)

        barchartInfoAbsensi.setFitBars(true)
        barchartInfoAbsensi.setData(barData)
        barchartInfoAbsensi.getDescription().setText("Chart Waktu Kerja")
        barchartInfoAbsensi.animateY(2000)
        val xAxis: XAxis = barchartInfoAbsensi.getXAxis()
        xAxis.textColor=Color.WHITE
        xAxis.setGranularity(1f);
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return DAYS[value.toInt()-1]
            }
        }
        val yAxis: YAxis = barchartInfoAbsensi.axisLeft
        yAxis.textColor=Color.WHITE
        val yAxis2: YAxis = barchartInfoAbsensi.axisRight
        yAxis2.textColor=Color.WHITE
        val l: Legend = barchartInfoAbsensi.getLegend()
        l.textColor=Color.WHITE
    }
    private fun hitungLamaWaktu(arr:List<String>){
        database.child("perusahaan").child(perusahaanID).child("absensi").child(arr[0]).child(arr[1]).child(arr[2]).get().addOnSuccessListener {
            txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\nHari ${hari[countData.toInt()-1]}, ${listTanggal[countData.toInt()-1]}:")
            var wjm:String?
            var wmm:String?
            var wdm:String?
            var wjk:String?
            var wmk:String?
            var wdk:String?
            Log.v("infoabsensi","sudah disini")
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
                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Belum Absensi Masuk")
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
                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Belum Absensi Keluar")
            }
            Log.v("infoabsensilongmasuk",longWaktuMasuk.toString())
            Log.v("infoabsensilongkeluar",longWaktuKeluar.toString())
            if(longWaktuKeluar!=null&&longWaktuMasuk!=null) {
                var longWaktuSelisih = longWaktuKeluar as Long - longWaktuMasuk as Long
                getTime(longWaktuSelisih as Long)
                val sharedPreferences =getSharedPreferences("Settings", Context.MODE_PRIVATE)
                var jamMasuk=sharedPreferences.getString("jam_masuk","")
                var menitMasuk=sharedPreferences.getString("menit_masuk","")
                var jamKeluar=sharedPreferences.getString("jam_pulang","")
                var menitKeluar=sharedPreferences.getString("menit_pulang","")
                if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()<=jamMasuk!!.toInt()*3600+menitMasuk!!.toInt()*60){

                    wjm=jamMasuk
                    wmm=menitMasuk
                    wdm="0"
                    Log.v("xy",wjm+":"+wmm+":"+wdm)
                }
                else{
                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Telat Absensi Masuk")
                }
                if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()>=jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
                    wjk=jamKeluar
                    wmk=menitKeluar
                    wdk="0"
                }
                if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()<jamMasuk!!.toInt()*3600+menitMasuk!!.toInt()*60){
                    wjk=jamMasuk
                    wmk=menitMasuk
                    wdk="0"
                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Keluar Sebelum Jam Masuk")
                }
                if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()>jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
                    wjm=jamKeluar
                    wmm=menitKeluar
                    wdm="0"
                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Masuk Setelah Jam Keluar")

                }
                var sj=(wjk!!.toInt()-wjm!!.toInt())
                var sm=(wmk!!.toInt()-wmm!!.toInt())
                var sd=(wdk!!.toInt()-wdm!!.toInt())
                var tot:Double=0.0
                if(sm<0){
                    sm=60+sm
                    sj=sj-1
                }
                if(sd<0){
                    sd=60+sd
                    sm=sm-1
                }
                if(longWaktuMasuk<=longWaktuKeluar){
                    tot=(sj*3600+sm*60+sd).toDouble()
                    if(tot>workHoursDay*3600){
                        var seltot=tot-workHoursDay*3600
                        tot=workHoursDay*3600
                        tempWorkHoursWeek=tempWorkHoursWeek+tot
                        Log.v("temphours",tempWorkHoursWeek.toString())
                        dataentry.add(BarEntry(countData, tot.toString().toFloat()/3600))
                        txtTotalWaktuIsiInfoAbsensi.setText((tempWorkHoursWeek.toInt()/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek.toInt()/60)%60).toString().padStart(2, '0')+":"+((tempWorkHoursWeek.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Lama Waktu Kerja: "+(tot.toInt()/3600).toString().padStart(2, '0')+":"+((tot.toInt()/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Surplus Waktu Kerja: "+(seltot.toInt()/3600).toString().padStart(2, '0')+":"+((seltot.toInt()/60)%60).toString().padStart(2, '0')+":"+((seltot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        countData=countData+1
                    }else{
                        tempWorkHoursWeek=tempWorkHoursWeek+tot
                        Log.v("temphours",tempWorkHoursWeek.toString())
                        dataentry.add(BarEntry(countData, tot.toString().toFloat()/3600))
                        Log.v("tesChart",countData.toString()+(tot.toString().toFloat()/3600).toString())
                        txtTotalWaktuIsiInfoAbsensi.setText((tempWorkHoursWeek.toInt()/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek.toInt()/60)%60).toString().padStart(2,'0')+":"+((tempWorkHoursWeek.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Lama Waktu Kerja: "+(tot.toInt()/3600).toString().padStart(2, '0')+":"+((tot.toInt()/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        countData=countData+1
                    }

                }
                else{
                    Log.v("tesChart",countData.toString()+0)
                    dataentry.add(BarEntry(countData, 0F))
                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Keluar terlebih dahulu")
                    countData=countData+1
                }

            }
            else{
                Log.v("tesChart",countData.toString()+0)
                dataentry.add(BarEntry(countData, 0F))
                countData=countData+1
            }
            if(countData==8F){
                countData=1F
                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n\n\n\n\n\n\n")
                createBar()
            }else{
                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n")

            }

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
        return sdf.format(Date(time))
    }
}