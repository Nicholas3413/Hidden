package com.example.hidden

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_absensi_harian.*
import kotlinx.android.synthetic.main.activity_detail_absensi.*
import kotlinx.android.synthetic.main.activity_informasi_absensi.*
import kotlinx.android.synthetic.main.activity_rekap_absensi.*
import java.text.SimpleDateFormat
import java.util.*


class RekapAbsensiActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private val datatable = arrayOf("1","nama","AAAAAA","0","0","0","0","0","0","0","0","0","0","0")
    private lateinit var tanggal:String
    private lateinit var tanggalTahun:String
    private lateinit var tanggalBulan:String
    private lateinit var tanggalHari:String
    private lateinit var waktuJam:String
    private lateinit var waktuMenit:String
    private lateinit var waktuDetik:String
    private lateinit var arr1:List<String>
    private val DAYS = arrayOf("SEN","SEL","RAB","KAM","JUM","SAB","MIN")
    private val hari = arrayOf("Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu")
    val listTempWeek: MutableList<Int> = ArrayList()
    private val listTanggal = arrayOf("a","b","c","d","e","f","g")
    private var tempWorkHoursWeek:Int=0
    private val dataentry: ArrayList<BarEntry> = ArrayList()
    private var countData:Float=1F
    private var workHoursDay:Int=0
    private var workHoursWeek:Int=0
    private lateinit var perusahaanID:String
    private lateinit var anggotaPerusahaanID:String
    var m = Array(1) {Array<String>(1) {"0"} }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rekap_absensi)
        init()
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        database = Firebase.database.reference
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        perusahaanID=sharedPreferences.getString("perusahaan_id","")!!
        database.child("perusahaan").child(perusahaanID!!).get().addOnSuccessListener {
            workHoursDay=it.child("work_hours_day").value.toString().toInt()
            workHoursWeek=it.child("work_hours_week").value.toString().toInt()
            database.child("timestamp").setValue(ServerValue.TIMESTAMP)
            database.child("timestamp").get().addOnSuccessListener {
                var timestamp=it.value
                tanggal=getDate(timestamp as Long)
                editTanggalRekapAbsensi.setText(tanggal)


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

//                txtWaktuSemingguInfoAbsensi.setText("Senin, "+startDate+" sampai Minggu, "+endDate6)
                val usersRef = database.child("perusahaan").child(perusahaanID!!).child("anggota")
                val valueEventListener: ValueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var counti=0
                        var size = 0
                        for (ds in dataSnapshot.children) {
                            size++ ;

                        }
                        m = Array(size) {Array<String>(14) {"0"} }
                        for (ds in dataSnapshot.children) {
                            val uid = ds.key
                            Log.d("listanggota", uid!!)
                            database.child("perusahaan").child(perusahaanID).child("anggota").child(uid).get().addOnSuccessListener {
                                var user_id= it.child("user_id").value.toString()
                                database.child("users").child(user_id).get().addOnSuccessListener {
                                    var nama_user=it.child("user_name").value.toString()

                                    counti=counti+1
                                    m[counti-1][1]=nama_user
                                    m[counti-1][2]=uid
                                    m[counti-1][0]=counti.toString()+"."
                                    tempWorkHoursWeek=0
                                    val calendar: Calendar = Calendar.getInstance(Locale.UK)
                                    calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                                    val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]
                                    Log.v("countii",counti.toString())
//                txtInformasiIsiInfoAbsensi.setText("")
                                    val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                                    c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                                    val t = c.time;
                                    val firstDay = c.firstDayOfWeek
                                    c.set(Calendar.DAY_OF_WEEK,firstDay)
                                    var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=startDate.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,false)
                                    listTanggal[0]=startDate
                                    Log.v("arr1",arr1.toString())
                                    c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                                    var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=endDate1.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,false)
                                    listTanggal[1]=endDate1
                                    c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                                    var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=endDate2.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,false)
                                    listTanggal[2]=endDate2
                                    c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                                    var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=endDate3.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,false)
                                    listTanggal[3]=endDate3
                                    c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                                    var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=endDate4.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,false)
                                    listTanggal[4]=endDate4
                                    c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                                    var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=endDate5.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,false)
                                    listTanggal[5]=endDate5
                                    c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                                    var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                    arr1=endDate6.split("-")
                                    hitungLamaWaktu(arr1,uid,counti-1,true)
                                    listTanggal[6]=endDate6
                                    var gambar_user=it.child("gambar_user").value.toString()
                                    database.child("perusahaan").child(perusahaanID).child("absensi").child(tanggalTahun).child(tanggalBulan).child(tanggalHari).get().addOnSuccessListener {
                                        var wjm:String?
                                        var wmm:String?
                                        var wdm:String?
                                        var wjk:String?
                                        var wmk:String?
                                        var wdk:String?

                                        var longWaktuMasuk=it.child(uid).child("jam_masuk").value
                                        var longWaktuKeluar=it.child(uid).child("jam_keluar").value
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

                                        var strlokasiLatitudeMasuk=it.child(uid).child("lokasi_latitude_masuk").value.toString()
                                        var strlokasiLongitudeMasuk=it.child(uid).child("lokasi_longitude_masuk").value.toString()
                                        var strlokasiLatitudeKeluar=it.child(uid).child("lokasi_latitude_keluar").value.toString()
                                        var strlokasiLongitudeKeluar=it.child(uid).child("lokasi_longitude_keluar").value.toString()
                                        if(it.child(uid).child("lokasi_latitude_masuk").value!=null){
                                            lokasiLatitudeMasuk=strlokasiLatitudeMasuk
                                            lokasiLongitudeMasuk=strlokasiLongitudeMasuk
                                        }
                                        else{
                                            lokasiLatitudeMasuk=""
                                            lokasiLongitudeMasuk=""
                                        }
                                        if(it.child(uid).child("lokasi_latitude_keluar").value!=null){
                                            lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                            lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                        }
                                        else{
                                            lokasiLatitudeKeluar=""
                                            lokasiLongitudeKeluar=""
                                        }
                                        Log.v("listdata","${nama_user}+,+${wjm}+,+$wjk+,+$wmm+,+$wmk+,+$wdm+,+$wdk+,+$lokasiLatitudeMasuk+,+$lokasiLatitudeKeluar+,+$lokasiLongitudeMasuk+,+$lokasiLongitudeKeluar")
//                                            txtAbsensiMasukIsiInfoAbsensi.setText(wjm.toString().padStart(2, '0')+":"+wmm.toString().padStart(2, '0')+":"+wdm.toString().padStart(2, '0'))
//                                            txtAbsensiKeluarIsiInfoAbsensi.setText(wjk.toString().padStart(2, '0')+":"+wmk.toString().padStart(2, '0')+":"+wdk.toString().padStart(2, '0'))

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
//                                                    txtSelisihWaktuIsiInfoAbsensi.setText(sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))
                                                Log.v("selisihwaktu",sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))
                                            }

                                        }
                                    }
                                }
                                Log.v("user_id",user_id)

                            }.addOnFailureListener {
                            }



                        }

                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                usersRef.addListenerForSingleValueEvent(valueEventListener)
            }.addOnFailureListener{
                Log.e("timestampfromdatabase", "Error getting data", it)
            }

        }

//        val usersRef = database.child("perusahaan").child(perusahaanID!!).child("anggota")
//        val valueEventListener: ValueEventListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                var counti=0
//                for (ds in dataSnapshot.children) {
//                    val uid = ds.key
//                    counti=counti+1
//                    Log.d("listanggota", uid!!)
//                    database.child("perusahaan").child(perusahaanID).child("anggota").child(uid).get().addOnSuccessListener {
//                        database.child("users").child(it.child("user_id").value.toString()).child("user_name").get().addOnSuccessListener {
//                            datatable[0]=counti.toString()+"."
//                            datatable[1]=it.value.toString()
//
//                        }
//                        datatable[2]=uid
//                    }
//
//
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) {}
//        }
//        usersRef.addListenerForSingleValueEvent(valueEventListener)
        editTanggalRekapAbsensi.setOnClickListener {

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
                    editTanggalRekapAbsensi.setText(""+ tanggalHari +"/"+ tanggalBulan +"/"+ tanggalTahun)
                    dataentry.clear()
//                    txtSelisihWaktuIsiInfoAbsensi.setText("00:00")
                    countData=1F

                    val count: Int = table_main.getChildCount()
                    for (i in 1 until count) {
                        val child: View = table_main.getChildAt(i)
                        if (child is TableRow) (child as ViewGroup).removeAllViews()
                    }


                    val usersRef = database.child("perusahaan").child(perusahaanID!!).child("anggota")
                    val valueEventListener: ValueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var counti=0
                            var size = 0
                            for (ds in dataSnapshot.children) {
                                size++ ;

                            }
                            m = Array(size) {Array<String>(14) {"0"} }
                            for (ds in dataSnapshot.children) {
                                val uid = ds.key
                                Log.d("listanggota", uid!!)
                                    database.child("perusahaan").child(perusahaanID).child("anggota").child(uid).get().addOnSuccessListener {
                                        var user_id= it.child("user_id").value.toString()
                                        database.child("users").child(user_id).get().addOnSuccessListener {
                                            var nama_user=it.child("user_name").value.toString()

                                            counti=counti+1
                                            m[counti-1][1]=nama_user
                                            m[counti-1][2]=uid
                                            m[counti-1][0]=counti.toString()+"."

                                            tempWorkHoursWeek=0
                                            val calendar: Calendar = Calendar.getInstance(Locale.UK)
                                            calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                                            val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]

//                txtInformasiIsiInfoAbsensi.setText("")
                                            val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                                            c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                                            val t = c.time;
                                            val firstDay = c.firstDayOfWeek
                                            c.set(Calendar.DAY_OF_WEEK,firstDay)
                                            var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=startDate.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,false)
                                            listTanggal[0]=startDate
                                            Log.v("arr1",arr1.toString())
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                                            var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate1.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,false)
                                            listTanggal[1]=endDate1
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                                            var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate2.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,false)
                                            listTanggal[2]=endDate2
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                                            var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate3.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,false)
                                            listTanggal[3]=endDate3
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                                            var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate4.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,false)
                                            listTanggal[4]=endDate4
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                                            var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate5.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,false)
                                            listTanggal[5]=endDate5
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                                            var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate6.split("-")
                                            hitungLamaWaktu(arr1,uid,counti-1,true)
                                            listTanggal[6]=endDate6
                                            var gambar_user=it.child("gambar_user").value.toString()
                                            database.child("perusahaan").child(perusahaanID).child("absensi").child(tanggalTahun).child(tanggalBulan).child(tanggalHari).get().addOnSuccessListener {
                                                var wjm:String?
                                                var wmm:String?
                                                var wdm:String?
                                                var wjk:String?
                                                var wmk:String?
                                                var wdk:String?

                                                var longWaktuMasuk=it.child(uid).child("jam_masuk").value
                                                var longWaktuKeluar=it.child(uid).child("jam_keluar").value
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

                                                var strlokasiLatitudeMasuk=it.child(uid).child("lokasi_latitude_masuk").value.toString()
                                                var strlokasiLongitudeMasuk=it.child(uid).child("lokasi_longitude_masuk").value.toString()
                                                var strlokasiLatitudeKeluar=it.child(uid).child("lokasi_latitude_keluar").value.toString()
                                                var strlokasiLongitudeKeluar=it.child(uid).child("lokasi_longitude_keluar").value.toString()
                                                if(it.child(uid).child("lokasi_latitude_masuk").value!=null){
                                                    lokasiLatitudeMasuk=strlokasiLatitudeMasuk
                                                    lokasiLongitudeMasuk=strlokasiLongitudeMasuk
                                                }
                                                else{
                                                    lokasiLatitudeMasuk=""
                                                    lokasiLongitudeMasuk=""
                                                }
                                                if(it.child(uid).child("lokasi_latitude_keluar").value!=null){
                                                    lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                                    lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                                }
                                                else{
                                                    lokasiLatitudeKeluar=""
                                                    lokasiLongitudeKeluar=""
                                                }
                                                Log.v("listdata","${nama_user}+,+${wjm}+,+$wjk+,+$wmm+,+$wmk+,+$wdm+,+$wdk+,+$lokasiLatitudeMasuk+,+$lokasiLatitudeKeluar+,+$lokasiLongitudeMasuk+,+$lokasiLongitudeKeluar")
//                                            txtAbsensiMasukIsiInfoAbsensi.setText(wjm.toString().padStart(2, '0')+":"+wmm.toString().padStart(2, '0')+":"+wdm.toString().padStart(2, '0'))
//                                            txtAbsensiKeluarIsiInfoAbsensi.setText(wjk.toString().padStart(2, '0')+":"+wmk.toString().padStart(2, '0')+":"+wdk.toString().padStart(2, '0'))

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
//                                                    txtSelisihWaktuIsiInfoAbsensi.setText(sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))
                                                        Log.v("selisihwaktu",sj.toString().padStart(2, '0')+":"+sm.toString().padStart(2, '0')+":"+sd.toString().padStart(2, '0'))
                                                    }

                                                }
                                            }
                                        }
                                        Log.v("user_id",user_id)

                                    }.addOnFailureListener {
                                    }



                            }

                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    }
                    usersRef.addListenerForSingleValueEvent(valueEventListener)

                }, year, month, day)
                datePickerDialog.show()

            }
    }
    private fun hitungLamaWaktu(arr:List<String>,angid:String,orderid:Int,render:Boolean?){
        var temphoursweek=0
        database.child("perusahaan").child(perusahaanID).child("absensi").child(arr[0]).child(arr[1]).child(arr[2]).get().addOnSuccessListener {
//            txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\nHari ${hari[countData.toInt()-1]}, ${listTanggal[countData.toInt()-1]}:")
            var wjm:String?
            var wmm:String?
            var wdm:String?
            var wjk:String?
            var wmk:String?
            var wdk:String?
            var tot:Int=0
            var longWaktuMasuk=it.child(angid).child("jam_masuk").value
            var longWaktuKeluar=it.child(angid).child("jam_keluar").value
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
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Belum Absensi Masuk")
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
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Belum Absensi Keluar")
            }
            tot=0
            Log.v("infoabsensilongmasuk",longWaktuMasuk.toString()+"x"+angid)
            Log.v("infoabsensilongkeluar",longWaktuKeluar.toString()+"x"+angid)
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
//                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Telat Absensi Masuk")
                    m[orderid][11]=(m[orderid][11].toInt()+1).toString()
                }
                if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()>=jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
                    wjk=jamKeluar
                    wmk=menitKeluar
                    wdk="0"
                }
                else{
                    m[orderid][12]=(m[orderid][12].toInt()+1).toString()
                }
                if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()<jamMasuk!!.toInt()*3600+menitMasuk!!.toInt()*60){
                    wjk=jamMasuk
                    wmk=menitMasuk
                    wdk="0"
//                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Keluar Sebelum Jam Masuk")
                }
                if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()>jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
                    wjm=jamKeluar
                    wmm=menitKeluar
                    wdm="0"
//                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Masuk Setelah Jam Keluar")

                }
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
                    tot=sj*3600+sm*60+sd
                    if(tot>workHoursDay*3600){
                        var seltot=tot-workHoursDay*3600
                        tot=workHoursDay*3600
                        m[orderid][10]=(m[orderid][10].toInt()+tot).toString()
//                        Log.v("temphours",tempWorkHoursWeek.toString())
                        m[orderid][2+countData.toInt()]=(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')

//                        txtTotalWaktuIsiInfoAbsensi.setText((tempWorkHoursWeek/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek/60)%60).toString().padStart(2, '0')+":"+((tempWorkHoursWeek.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
//                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Lama Waktu Kerja: "+(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
//                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Surplus Waktu Kerja: "+(seltot/3600).toString().padStart(2, '0')+":"+((seltot/60)%60).toString().padStart(2, '0')+":"+((seltot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        countData=countData+1


                    }else{
                        m[orderid][10]=(m[orderid][10].toInt()+tot).toString()
//                        Log.v("temphours",tempWorkHoursWeek.toString())

                        Log.v("tesChart",countData.toString()+(tot.toString().toFloat()/3600).toString())
                        m[orderid][2+countData.toInt()]=(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
//                        txtTotalWaktuIsiInfoAbsensi.setText((tempWorkHoursWeek/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek/60)%60).toString().padStart(2,'0')+":"+((tempWorkHoursWeek.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
//                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Lama Waktu Kerja: "+(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
                        countData=countData+1
                        Log.v("morderida",m[orderid][10]+"x"+angid+"x"+countData+"x"+orderid)
                    }

                }
                else{
                    Log.v("tesChart",countData.toString()+0)

                    m[orderid][2+countData.toInt()]="0"
//                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Keluar terlebih dahulu")
                    countData=countData+1
                }

            }
            else{
                Log.v("tesChart",countData.toString()+0)
                m[orderid][2+countData.toInt()]="0"
                m[orderid][13]=(m[orderid][13].toInt()+1).toString()
                countData=countData+1
            }
            if(countData==8F){
                countData=1F
                Log.v("temphoursweek",(tempWorkHoursWeek/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek/60)%60).toString().padStart(2,'0')+":"+((tempWorkHoursWeek.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')+"x"+angid)
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n\n\n\n\n\n\n")
//                createBar()
                Log.v("morder",m[orderid][10].toString()+"x"+angid+"x"+m[orderid][1])
                Log.v("morder7hari",m[orderid][3]+"-"+m[orderid][4]+"-"+m[orderid][5]+"-"+m[orderid][6]+"-"+m[orderid][7]+"-"+m[orderid][8]+"-"+m[orderid][9])
                dataentry.add(BarEntry(orderid.toFloat(), m[orderid][10].toFloat()/3600))
                Log.v("cekdataentry",orderid.toFloat().toString()+m[orderid][10].toFloat()/3600)
                m[orderid][10]=(m[orderid][10].toInt()/3600).toString().padStart(2, '0')+":"+((m[orderid][10].toInt()/60)%60).toString().padStart(2, '0')+":"+((m[orderid][10].toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
                if(render==true){
                    createBar()
                }
                writetable(orderid)

            }else{
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n")

            }

        }

    }
    fun init() {
        val gradientDrawableDefault = GradientDrawable()
        gradientDrawableDefault.setStroke(
            1,
            getResources().getColor(R.color.black)
        )

        val tbrow0 = TableRow(this)
        val tv0 = TextView(this)
        tv0.text = " No. "
        tv0.background=gradientDrawableDefault
        tv0.setPadding(10,10,10,10)
        tv0.setTextColor(getResources().getColor(R.color.white))
        tbrow0.addView(tv0)
        val tv1 = TextView(this)
        tv1.text = " Nama "
        tv1.setPadding(10,10,10,10)
        tv1.setTextColor(getResources().getColor(R.color.white))
        tv1.background=gradientDrawableDefault
        tbrow0.addView(tv1)
        val tv2 = TextView(this)
        tv2.text = " ID Anggota "
        tv2.setPadding(10,10,10,10)
        tv2.setTextColor(getResources().getColor(R.color.white))
        tv2.background=gradientDrawableDefault
        tbrow0.addView(tv2)
        val tv3 = TextView(this)
        tv3.text = " SENIN "
        tv3.setPadding(10,10,10,10)
        tv3.setTextColor(getResources().getColor(R.color.white))
        tv3.background=gradientDrawableDefault
        tbrow0.addView(tv3)
        val tv4 = TextView(this)
        tv4.text = " SELASA "
        tv4.setPadding(10,10,10,10)
        tv4.setTextColor(getResources().getColor(R.color.white))
        tv4.background=gradientDrawableDefault
        tbrow0.addView(tv4)
        val tv5 = TextView(this)
        tv5.text = " RABU "
        tv5.setPadding(10,10,10,10)
        tv5.setTextColor(getResources().getColor(R.color.white))
        tv5.background=gradientDrawableDefault
        tbrow0.addView(tv5)
        val tv6 = TextView(this)
        tv6.text = " KAMIS "
        tv6.setPadding(10,10,10,10)
        tv6.setTextColor(getResources().getColor(R.color.white))
        tv6.background=gradientDrawableDefault
        tbrow0.addView(tv6)
        val tv7 = TextView(this)
        tv7.text = " JUMAT "
        tv7.setPadding(10,10,10,10)
        tv7.setTextColor(getResources().getColor(R.color.white))
        tv7.background=gradientDrawableDefault
        tbrow0.addView(tv7)
        val tv8 = TextView(this)
        tv8.text = " SABTU "
        tv8.setPadding(10,10,10,10)
        tv8.setTextColor(getResources().getColor(R.color.white))
        tv8.background=gradientDrawableDefault
        tbrow0.addView(tv8)
        val tv9 = TextView(this)
        tv9.text = " MINGGU "
        tv9.setPadding(10,10,10,10)
        tv9.setTextColor(getResources().getColor(R.color.white))
        tv9.background=gradientDrawableDefault
        tbrow0.addView(tv9)
        val tv10 = TextView(this)
        tv10.text = " Total Waktu Kerja "
        tv10.setPadding(10,10,10,10)
        tv10.setTextColor(getResources().getColor(R.color.white))
        tv10.background=gradientDrawableDefault
        tbrow0.addView(tv10)
        val tv11 = TextView(this)
        tv11.text = " Telat Absensi Masuk "
        tv11.setPadding(10,10,10,10)
        tv11.setTextColor(getResources().getColor(R.color.white))
        tv11.background=gradientDrawableDefault
        tbrow0.addView(tv11)
        val tv12 = TextView(this)
        tv12.text = " Cepat Absensi Keluar "
        tv12.setPadding(10,10,10,10)
        tv12.setTextColor(getResources().getColor(R.color.white))
        tv12.background=gradientDrawableDefault
        tbrow0.addView(tv12)
        val tv13 = TextView(this)
        tv13.text = " Absensi Kosong "
        tv13.setPadding(10,10,10,10)
        tv13.setTextColor(getResources().getColor(R.color.white))
        tv13.background=gradientDrawableDefault
        tbrow0.addView(tv13)
        table_main.addView(tbrow0)
//        for (i in 0..40) {
//            val tbrow = TableRow(this)
//            val t1v = TextView(this)
//            t1v.text = "" + i
//            t1v.setTextColor(getResources().getColor(R.color.white))
//            t1v.gravity = Gravity.CENTER
//            tbrow.addView(t1v)
//            val t2v = TextView(this)
//            t2v.text = "Product $i"
//            t2v.setTextColor(getResources().getColor(R.color.white))
//            t2v.gravity = Gravity.CENTER
//            tbrow.addView(t2v)
//            val t3v = TextView(this)
//            t3v.text = "Rs.$i"
//            t3v.setTextColor(getResources().getColor(R.color.white))
//            t3v.gravity = Gravity.CENTER
//            tbrow.addView(t3v)
//            val t4v = TextView(this)
//            t4v.text = "" + i * 15 / 32 * 10
//            t4v.setTextColor(getResources().getColor(R.color.white))
//            t4v.gravity = Gravity.CENTER
//            tbrow.addView(t4v)
//            table_main.addView(tbrow)
//        }
    }
    fun writetable(orderanggota:Int){
        val tbrow = TableRow(this)
            val t1v = TextView(this)
            t1v.text = m[orderanggota][0]
            t1v.setTextColor(getResources().getColor(R.color.white))
            t1v.gravity = Gravity.CENTER
            tbrow.addView(t1v)
            val t2v = TextView(this)
            t2v.text = m[orderanggota][1]
            t2v.setTextColor(getResources().getColor(R.color.white))
            t2v.gravity = Gravity.CENTER
            tbrow.addView(t2v)
            val t3v = TextView(this)
            t3v.text = m[orderanggota][2]
            t3v.setTextColor(getResources().getColor(R.color.white))
            t3v.gravity = Gravity.CENTER
            tbrow.addView(t3v)
            val t4v = TextView(this)
            t4v.text = m[orderanggota][3]
            t4v.setTextColor(getResources().getColor(R.color.white))
            t4v.gravity = Gravity.CENTER
            tbrow.addView(t4v)
        val t5v = TextView(this)
        t5v.text = m[orderanggota][4]
        t5v.setTextColor(getResources().getColor(R.color.white))
        t5v.gravity = Gravity.CENTER
        tbrow.addView(t5v)
        val t6v = TextView(this)
        t6v.text = m[orderanggota][5]
        t6v.setTextColor(getResources().getColor(R.color.white))
        t6v.gravity = Gravity.CENTER
        tbrow.addView(t6v)
        val t7v = TextView(this)
        t7v.text = m[orderanggota][6]
        t7v.setTextColor(getResources().getColor(R.color.white))
        t7v.gravity = Gravity.CENTER
        tbrow.addView(t7v)
        val t8v = TextView(this)
        t8v.text = m[orderanggota][7]
        t8v.setTextColor(getResources().getColor(R.color.white))
        t8v.gravity = Gravity.CENTER
        tbrow.addView(t8v)
        val t9v = TextView(this)
        t9v.text = m[orderanggota][8]
        t9v.setTextColor(getResources().getColor(R.color.white))
        t9v.gravity = Gravity.CENTER
        tbrow.addView(t9v)
        val t10v = TextView(this)
        t10v.text = m[orderanggota][9]
        t10v.setTextColor(getResources().getColor(R.color.white))
        t10v.gravity = Gravity.CENTER
        tbrow.addView(t10v)
        val t11v = TextView(this)
        t11v.text = m[orderanggota][10]
        t11v.setTextColor(getResources().getColor(R.color.white))
        t11v.gravity = Gravity.CENTER
        tbrow.addView(t11v)
        val t12v = TextView(this)
        t12v.text = m[orderanggota][11]
        t12v.setTextColor(getResources().getColor(R.color.white))
        t12v.gravity = Gravity.CENTER
        tbrow.addView(t12v)
        val t13v = TextView(this)
        t13v.text = m[orderanggota][12]
        t13v.setTextColor(getResources().getColor(R.color.white))
        t13v.gravity = Gravity.CENTER
        tbrow.addView(t13v)
        val t14v = TextView(this)
        t14v.text = m[orderanggota][13]
        t14v.setTextColor(getResources().getColor(R.color.white))
        t14v.gravity = Gravity.CENTER
        tbrow.addView(t14v)
            table_main.addView(tbrow)
    }
    private fun createBar(){
        barchartRekapAbsensi.getDescription().setEnabled(false);
        barchartRekapAbsensi.setDrawValueAboveBar(false);
        val barDataSet = BarDataSet(dataentry, "Total Waktu Kerja (jam)")
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        barDataSet.setValueTextColor(Color.WHITE)
        barDataSet.valueTextSize = 12f
        val barData = BarData(barDataSet)

        barchartRekapAbsensi.setFitBars(true)
        barchartRekapAbsensi.setData(barData)
        barchartRekapAbsensi.getDescription().setText("Chart Waktu Kerja")
        barchartRekapAbsensi.animateY(2000)
        val xAxis: XAxis = barchartRekapAbsensi.getXAxis()
        xAxis.textColor= Color.WHITE
        xAxis.setGranularity(1f);
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return m[value.toInt()][1]
            }
        }
        val yAxis: YAxis = barchartRekapAbsensi.axisLeft
        yAxis.textColor= Color.WHITE
        val yAxis2: YAxis = barchartRekapAbsensi.axisRight
        yAxis2.textColor= Color.WHITE
        val l: Legend = barchartRekapAbsensi.getLegend()
        l.textColor= Color.WHITE
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