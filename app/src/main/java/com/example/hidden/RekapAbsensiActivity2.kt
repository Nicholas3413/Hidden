package com.example.hidden

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_informasi_absensi.*
import kotlinx.android.synthetic.main.activity_rekap_absensi2.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RekapAbsensiActivity2 : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    val list: MutableList<String> = ArrayList()
    val listuid: MutableList<String> = ArrayList()
    val listauid: MutableList<String> = ArrayList()
    
    var selected=0

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
    private var tempWorkHoursWeek:Int=0
    private val dataentry: java.util.ArrayList<BarEntry> = java.util.ArrayList()
    private var countData:Float=1F
    private val DAYS = arrayOf("SEN","SEL","RAB","KAM","JUM","SAB","MIN")
    private val hari = arrayOf("Senin","Selasa","Rabu","Kamis","Jumat","Sabtu","Minggu")
    private val listTanggal = arrayOf("a","b","c","d","e","f","g")
    var m = Array(8) {Array<String>(7) {"0"} }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rekap_absensi2)

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        database = Firebase.database.reference
        var userID= Firebase.auth.currentUser?.uid.toString()
        spinnerAnggota.setOnItemSelectedListener(this)




        database.child("users").child(userID).get().addOnSuccessListener {
            perusahaanID = it.child("perusahaan_id").value.toString()
            anggotaPerusahaanID = it.child("anggota_perusahaan_id").value.toString()

            val usersRef = database.child("perusahaan").child(perusahaanID!!).child("anggota")
            val valueEventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    list.clear()
                    listuid.clear()
                    listauid.clear()
                    var counti=0
                    var size = 0
                    for (ds in dataSnapshot.children) {
                        size++

                    }
//                    m = Array(size) {Array<String>(14) {"0"} }
                    for (ds in dataSnapshot.children) {
                        val uid = ds.key
                        Log.d("listanggota", uid!!)
                        database.child("perusahaan").child(perusahaanID!!).child("anggota").child(uid).child("user_id").get().addOnSuccessListener {
                            listuid.add(it.value.toString())
                            database.child("users").child(it.value.toString()).get().addOnSuccessListener {
                                list.add(it.child("user_name").value.toString())
                                listauid.add(it.child("anggota_perusahaan_id").value.toString())

                                counti=counti+1
                                if(counti==size){
                                    database.child("perusahaan").child(perusahaanID).get().addOnSuccessListener {
                                        workHoursDay = it.child("work_hours_day").value.toString().toDouble()
                                        workHoursWeek = it.child("work_hours_week").value.toString().toDouble()
                                        database.child("timestamp").setValue(ServerValue.TIMESTAMP)
                                        database.child("timestamp").get().addOnSuccessListener {
                                            var timestamp = it.value
                                            tanggal = getDate(timestamp as Long)
                                            editTanggalRekapAbsensi2.setText(tanggal)
                                            val adapter = ArrayAdapter(this@RekapAbsensiActivity2, android.R.layout.simple_spinner_item, list)
                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                            spinnerAnggota.setAdapter(adapter)
                                            tempWorkHoursWeek=0
                                            val calendar: Calendar = Calendar.getInstance(Locale.UK)
                                            calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                                            val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]
                                            val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                                            c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                                            val t = c.time;
                                            val firstDay = c.firstDayOfWeek
                                            c.set(Calendar.DAY_OF_WEEK,firstDay)
                                            var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=startDate.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],0,false)
                                            txtTanggalSeninRekapAbsensi2.setText(startDate)
                                            listTanggal[0]=startDate
                                            Log.v("arr1",arr1.toString())
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                                            var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate1.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],1,false)
                                            txtTanggalSelasaRekapAbsensi2.setText(endDate1)
                                            listTanggal[1]=endDate1
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                                            var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate2.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],2,false)
                                            txtTanggalRabuRekapAbsensi2.setText(endDate2)
                                            listTanggal[2]=endDate2
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                                            var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate3.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],3,false)
                                            txtTanggalKamisRekapAbsensi2.setText(endDate3)
                                            listTanggal[3]=endDate3
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                                            var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate4.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],4,false)
                                            txtTanggalJumatRekapAbsensi2.setText(endDate4)
                                            listTanggal[4]=endDate4
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                                            var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate5.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],5,false)
                                            txtTanggalSabtuRekapAbsensi2.setText(endDate5)
                                            listTanggal[5]=endDate5
                                            c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                                            var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                                            arr1=endDate6.split("-")
                                            hitungLamaWaktu(arr1,listauid[selected],6,true)
                                            txtTanggalMingguRekapAbsensi2.setText(endDate6)
                                            listTanggal[6]=endDate6
                                            txtWaktuSemingguRekapAbsensi2.setText("Senin, "+startDate+" sampai Minggu, "+endDate6)
                                        }
                                    }
                                }
                            }
                        }
                    }


                }
                override fun onCancelled(databaseError: DatabaseError) {}

            }
            usersRef.addListenerForSingleValueEvent(valueEventListener)



        }
        editTanggalRekapAbsensi2.setOnClickListener {
            m = Array(8) {Array<String>(7) {"0"} }
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
                editTanggalRekapAbsensi2.setText(""+ tanggalHari +"/"+ tanggalBulan +"/"+ tanggalTahun)
                dataentry.clear()
                val calendar: Calendar = Calendar.getInstance(Locale.UK)
                calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]
                val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                val t = c.time;
                val firstDay = c.firstDayOfWeek
                c.set(Calendar.DAY_OF_WEEK,firstDay)
                var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=startDate.split("-")
                hitungLamaWaktu(arr1,listauid[selected],0,false)
                txtTanggalSeninRekapAbsensi2.setText(startDate)
                listTanggal[0]=startDate
                Log.v("arr1",arr1.toString())
                c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate1.split("-")
                hitungLamaWaktu(arr1,listauid[selected],1,false)
                txtTanggalSelasaRekapAbsensi2.setText(endDate1)
                listTanggal[1]=endDate1
                c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate2.split("-")
                hitungLamaWaktu(arr1,listauid[selected],2,false)
                txtTanggalRabuRekapAbsensi2.setText(endDate2)
                listTanggal[2]=endDate2
                c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate3.split("-")
                hitungLamaWaktu(arr1,listauid[selected],3,false)
                txtTanggalKamisRekapAbsensi2.setText(endDate3)
                listTanggal[3]=endDate3
                c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate4.split("-")
                hitungLamaWaktu(arr1,listauid[selected],4,false)
                txtTanggalJumatRekapAbsensi2.setText(endDate4)
                listTanggal[4]=endDate4
                c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate5.split("-")
                hitungLamaWaktu(arr1,listauid[selected],5,false)
                txtTanggalSabtuRekapAbsensi2.setText(endDate5)
                listTanggal[5]=endDate5
                c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate6.split("-")
                hitungLamaWaktu(arr1,listauid[selected],6,true)
                txtTanggalMingguRekapAbsensi2.setText(endDate6)
                listTanggal[6]=endDate6
                txtWaktuSemingguRekapAbsensi2.setText("Senin, "+startDate+" sampai Minggu, "+endDate6)


            }, year, month, day)
            datePickerDialog.show()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.v("selected",list[p2])
        dataentry.clear()
        m = Array(8) {Array<String>(7) {"0"} }
        database.child("users").child(listuid[p2]).get().addOnSuccessListener {
            txtNamaUserIsiRekapAbsensi2.setText(it.child("user_name").value.toString())
            txtEmailIsiRekapAbsensi2.setText(it.child("email_user").value.toString())
            database.child("perusahaan").child(perusahaanID!!).child("anggota").child(it.child("anggota_perusahaan_id").value.toString()).child("bagian").get().addOnSuccessListener {
                if(it.value.toString()==""||it.value.toString()=="null"){
                    txtBagianIsiRekapAbsensi2.setText("-")
                }else{
                    txtBagianIsiRekapAbsensi2.setText(it.value.toString())
                }

                selected=p2
//                tanggal=editTanggalRekapAbsensi2.text.toString()
//                tempWorkHoursWeek=0
                val calendar: Calendar = Calendar.getInstance(Locale.UK)
                calendar.set(tanggalTahun.toInt(), tanggalBulan.toInt()-1,tanggalHari.toInt())
                val weekOfYear = calendar[Calendar.WEEK_OF_YEAR]
                val c: Calendar = Calendar.getInstance(Locale.UK)//Locale.getDefault())
                c.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                val t = c.time;
                val firstDay = c.firstDayOfWeek
                c.set(Calendar.DAY_OF_WEEK,firstDay)
                var startDate = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=startDate.split("-")
                hitungLamaWaktu(arr1,listauid[selected],0,false)
                txtTanggalSeninRekapAbsensi2.setText(startDate)
                listTanggal[0]=startDate
                Log.v("arr1",arr1.toString())
                c.set(Calendar.DAY_OF_WEEK,firstDay+1)
                var endDate1 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate1.split("-")
                hitungLamaWaktu(arr1,listauid[selected],1,false)
                txtTanggalSelasaRekapAbsensi2.setText(endDate1)
                listTanggal[1]=endDate1
                c.set(Calendar.DAY_OF_WEEK,firstDay+2)
                var endDate2 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate2.split("-")
                hitungLamaWaktu(arr1,listauid[selected],2,false)
                txtTanggalRabuRekapAbsensi2.setText(endDate2)
                listTanggal[2]=endDate2
                c.set(Calendar.DAY_OF_WEEK,firstDay+3)
                var endDate3 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate3.split("-")
                hitungLamaWaktu(arr1,listauid[selected],3,false)
                txtTanggalKamisRekapAbsensi2.setText(endDate3)
                listTanggal[3]=endDate3
                c.set(Calendar.DAY_OF_WEEK,firstDay+4)
                var endDate4 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate4.split("-")
                hitungLamaWaktu(arr1,listauid[selected],4,false)
                txtTanggalJumatRekapAbsensi2.setText(endDate4)
                listTanggal[4]=endDate4
                c.set(Calendar.DAY_OF_WEEK,firstDay+5)
                var endDate5 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate5.split("-")
                hitungLamaWaktu(arr1,listauid[selected],5,false)
                txtTanggalSabtuRekapAbsensi2.setText(endDate5)
                listTanggal[5]=endDate5
                c.set(Calendar.DAY_OF_WEEK,firstDay+6)
                var endDate6 = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.time).toString()
                arr1=endDate6.split("-")
                hitungLamaWaktu(arr1,listauid[selected],6,true)
                txtTanggalMingguRekapAbsensi2.setText(endDate6)
                listTanggal[6]=endDate6
            }

        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
    private fun hitungLamaWaktu(arr:List<String>,angid:String,hariid:Int,render:Boolean?){
        var temphoursweek=0

        database.child("perusahaan").child(perusahaanID).child("absensi").child(arr[0]).child(arr[1]).child(arr[2]).get().addOnSuccessListener {
//            txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\nHari ${hari[countData.toInt()-1]}, ${listTanggal[countData.toInt()-1]}:")
            var lock=true
            var wjm:String?
            var wmm:String?
            var wdm:String?
            var wjk:String?
            var wmk:String?
            var wdk:String?
            var tot:Double=0.0
            var longWaktuMasuk=it.child(angid).child("jam_masuk").value
            var longWaktuKeluar=it.child(angid).child("jam_keluar").value
            if(longWaktuMasuk!=null){
                getTime(longWaktuMasuk as Long)
                wjm=waktuJam
                wmm=waktuMenit
                wdm=waktuDetik
                m[hariid][0]=wjm+":"+wmm+":"+wdm
                m[hariid][4]=it.child(angid).child("lokasi_latitude_masuk").value.toString()+","+it.child(angid).child("lokasi_longitude_masuk").value.toString()
                Log.v("ini bukan null","a")
            }
            else{
                wjm=""
                wmm=""
                wdm=""
                m[hariid][0]="-"
                m[hariid][4]="-"
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Belum Absensi Masuk")
            }
            if(longWaktuKeluar!=null){
                getTime(longWaktuKeluar as Long)
                wjk=waktuJam
                wmk=waktuMenit
                wdk=waktuDetik
                m[hariid][1]=wjk+":"+wmk+":"+wdk
                m[hariid][5]=it.child(angid).child("lokasi_latitude_keluar").value.toString()+","+it.child(angid).child("lokasi_longitude_keluar").value.toString()

            }
            else{
                wjk=""
                wmk=""
                wdk=""
                m[hariid][1]="-"
                m[hariid][5]="-"
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Belum Absensi Keluar")
            }
            tot=0.0
            Log.v("infoabsensilongmasuk",longWaktuMasuk.toString()+"x"+angid)
            Log.v("infoabsensilongkeluar",longWaktuKeluar.toString()+"x"+angid)
            if(longWaktuKeluar!=null&&longWaktuMasuk!=null) {
                var longWaktuSelisih = longWaktuKeluar as Long - longWaktuMasuk as Long
                getTime(longWaktuSelisih as Long)
                val sharedPreferences =getSharedPreferences("Settings", MODE_PRIVATE)
                var jamMasuk=sharedPreferences.getString("jam_masuk","")
                var menitMasuk=sharedPreferences.getString("menit_masuk","")
                var jamKeluar=sharedPreferences.getString("jam_pulang","")
                var menitKeluar=sharedPreferences.getString("menit_pulang","")
                if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()<=jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
                    if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()<=jamMasuk!!.toInt()*3600+menitMasuk!!.toInt()*60){
                        wjm=jamMasuk
                        wmm=menitMasuk
                        wdm="0"
                    }
                    if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()>=jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
                    wjk=jamKeluar
//                        wjk=wjk
                    wmk=menitKeluar
//                        wmk=wmk
                        wdk="0"
                    }

                    if(longWaktuMasuk<=longWaktuKeluar){
                        var xxx:Double=(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()-wjm.toInt()*3600-wmm.toInt()*60-wdm.toInt()).toDouble()
                        if(xxx>workHoursDay*3600){
                            var sxxx:Double=xxx-workHoursDay*3600
                            xxx=workHoursDay*3600
                            m[hariid][2]=(xxx.toInt()/3600).toString().padStart(2, '0')+":"+((xxx.toInt()/60)%60).toString().padStart(2, '0')+":"+((xxx.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
                            m[7][0]=(m[7][0].toDouble().toInt()+xxx).toString()
                            m[hariid][6]=xxx.toString()
                            m[hariid][3]=(sxxx.toInt()/3600).toString().padStart(2, '0')+":"+((sxxx.toInt()/60)%60).toString().padStart(2, '0')+":"+((sxxx.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
                        }else{
                            m[hariid][2]=(xxx.toInt()/3600).toString().padStart(2, '0')+":"+((xxx.toInt()/60)%60).toString().padStart(2, '0')+":"+((xxx.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
                            m[7][0]=(m[7][0].toDouble().toInt()+xxx).toString()
                            m[hariid][6]=xxx.toString()
                        }
                    }else{
                        m[hariid][2]="00:00:00"
                        m[hariid][3]="00:00:00"
                        m[hariid][6]="0"
                    }
                }else{
                    m[hariid][2]="00:00:00"
                    m[hariid][3]="00:00:00"
                    m[hariid][6]="0"
                }
//                if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()<=jamMasuk!!.toInt()*3600+menitMasuk!!.toInt()*60){
//
//                    wjm=jamMasuk
//                    wmm=menitMasuk
//                    wdm="0"
//                    Log.v("xy",wjm+":"+wmm+":"+wdm)
//                }
//                else{
////                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Telat Absensi Masuk")
////                    m[orderid][11]=(m[orderid][11].toInt()+1).toString()
//                }
//                if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()>=jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
////                    wjk=jamKeluar
//                    wjk=wjk
////                    wmk=menitKeluar
//                    wmk=wmk
//                    wdk="0"
//                }
//                else{
////                    m[orderid][12]=(m[orderid][12].toInt()+1).toString()
//                }
//                if(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()<jamMasuk!!.toInt()*3600+menitMasuk!!.toInt()*60){
//                    wjk=jamMasuk
//                    wmk=menitMasuk
//                    wdk="0"
////                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Keluar Sebelum Jam Masuk")
//                }
//                if(wjm.toInt()*3600+wmm.toInt()*60+wdm.toInt()>jamKeluar!!.toInt()*3600+menitKeluar!!.toInt()*60){
//                    wjm=jamKeluar
////                    wmm=menitKeluar
//                    wmm=menitKeluar
//                    wdm="0"
////                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Masuk Setelah Jam Keluar")
//
//                }
////                var sj=(wjk!!.toInt()-wjm!!.toInt())
////                var sm=(wmk!!.toInt()-wmm!!.toInt())
////                var sd=(wdk!!.toInt()-wdm!!.toInt())
////                if(sm<0){
////                    sm=60+sm
////                    sj=sj-1
////                }
////                if(sd<0){
////                    sd=60+sd
////                    sm=sm-1
////                }
//                var xxx:Double=(wjk.toInt()*3600+wmk.toInt()*60+wdk.toInt()-wjm.toInt()*3600-wmm.toInt()*60-wdm.toInt()).toDouble()
//                if(longWaktuMasuk<=longWaktuKeluar){
////                    tot=(sj*3600+sm*60+sd).toDouble()
//                    tot=xxx
//                    Log.v("seltot",tot.toString())
////                    Log.v("seltot",sj.toString()+":"+sm.toString()+":"+sd.toString())
//                    if(tot>workHoursDay*3600){
//                        var seltot=tot-workHoursDay*3600
//                        tot=workHoursDay*3600.0
//                        m[hariid][2]=(tot.toInt()/3600).toString().padStart(2, '0')+":"+((tot.toInt()/60)%60).toString().padStart(2, '0')+":"+((tot.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
//                        m[7][0]=(m[7][0].toDouble().toInt()+tot).toString()
//                        m[hariid][6]=tot.toString()
//                        Log.v("seltot",seltot.toString())
//                        Log.v("seltot",(seltot.toInt()/3600).toString().padStart(2, '0')+":"+((seltot.toInt()/60)%60).toString().padStart(2, '0')+":"+((seltot.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
//                        m[hariid][3]=(seltot.toInt()/3600).toString().padStart(2, '0')+":"+((seltot.toInt()/60)%60).toString().padStart(2, '0')+":"+((seltot.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
////                        Log.v("temphours",tempWorkHoursWeek.toString())
////                        m[orderid][2+countData.toInt()]=(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
//
////                        txtTotalWaktuIsiInfoAbsensi.setText((tempWorkHoursWeek/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek/60)%60).toString().padStart(2, '0')+":"+((tempWorkHoursWeek.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
////                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Lama Waktu Kerja: "+(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
////                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Surplus Waktu Kerja: "+(seltot/3600).toString().padStart(2, '0')+":"+((seltot/60)%60).toString().padStart(2, '0')+":"+((seltot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
////                        countData=countData+1
//
//
//                    }else{
//                        m[hariid][2]=(tot.toInt()/3600).toString().padStart(2, '0')+":"+((tot.toInt()/60)%60).toString().padStart(2, '0')+":"+((tot.toInt().toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
//                        m[7][0]=(m[7][0].toDouble().toInt()+tot).toString()
//                        m[hariid][3]="00:00:00"
//                        m[hariid][6]=tot.toString()
////                        Log.v("temphours",tempWorkHoursWeek.toString())
//
////                        m[orderid][2+countData.toInt()]=(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0')
////                        txtTotalWaktuIsiInfoAbsensi.setText((tempWorkHoursWeek/3600).toString().padStart(2, '0')+":"+((tempWorkHoursWeek/60)%60).toString().padStart(2,'0')+":"+((tempWorkHoursWeek.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
////                        txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Lama Waktu Kerja: "+(tot/3600).toString().padStart(2, '0')+":"+((tot/60)%60).toString().padStart(2, '0')+":"+((tot.toString().toDouble()%3600)%60).toInt().toString().padStart(2, '0'))
////                        countData=countData+1
////                        Log.v("morderida",m[orderid][10]+"x"+angid+"x"+countData+"x"+orderid)
//                    }
//
//                }
//                else{
////                    Log.v("tesChart",countData.toString()+0)
//                    m[hariid][2]="00:00:00"
//                    m[hariid][3]="00:00:00"
//                    m[hariid][6]="0"
////                    txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n- Absensi Keluar terlebih dahulu")
////                    countData=countData+1
//                }

            }
            else{
//                Log.v("tesChart",countData.toString()+0)
                m[hariid][2]="00:00:00"
                m[hariid][3]="00:00:00"
                m[hariid][6]="0"
//                m[orderid][13]=(m[orderid][13].toInt()+1).toString()
//                countData=countData+1
            }
            if(render==true){
                m[7][1]=(m[7][0].toDouble().toInt()/3600).toString().padStart(2, '0')+":"+((m[7][0].toDouble().toInt()/60)%60).toString().padStart(2, '0')+":"+((m[7][0].toDouble().toInt().toString().toDouble()%3600)%60).toDouble().toInt().toString().padStart(2, '0')
                if(m[7][0].toDouble()<workHoursWeek*3600){
                    txtWaktuKerjaSemingguRekapAbsensi2.setTextColor(Color.parseColor("red"))
                }
                else{
//                    txtWaktuKerjaSemingguRekapAbsensi2.setTextColor(Color.parseColor("#006400"))
                    txtWaktuKerjaSemingguRekapAbsensi2.setTextColor(Color.parseColor("green"))
                }
                txt1wmiRekapAbsensi2.setText(m[0][0])
                txt2wmiRekapAbsensi2.setText(m[1][0])
                txt3wmiRekapAbsensi2.setText(m[2][0])
                txt4wmiRekapAbsensi2.setText(m[3][0])
                txt5wmiRekapAbsensi2.setText(m[4][0])
                txt6wmiRekapAbsensi2.setText(m[5][0])
                txt7wmiRekapAbsensi2.setText(m[6][0])
                txt1wkiRekapAbsensi2.setText(m[0][1])
                txt2wkiRekapAbsensi2.setText(m[1][1])
                txt3wkiRekapAbsensi2.setText(m[2][1])
                txt4wkiRekapAbsensi2.setText(m[3][1])
                txt5wkiRekapAbsensi2.setText(m[4][1])
                txt6wkiRekapAbsensi2.setText(m[5][1])
                txt7wkiRekapAbsensi2.setText(m[6][1])
                txt1wliRekapAbsensi2.setText(m[0][2])
                txt2wliRekapAbsensi2.setText(m[1][2])
                txt3wliRekapAbsensi2.setText(m[2][2])
                txt4wliRekapAbsensi2.setText(m[3][2])
                txt5wliRekapAbsensi2.setText(m[4][2])
                txt6wliRekapAbsensi2.setText(m[5][2])
                txt7wliRekapAbsensi2.setText(m[6][2])
                txt1wsiRekapAbsensi2.setText(m[0][3])
                txt2wsiRekapAbsensi2.setText(m[1][3])
                txt3wsiRekapAbsensi2.setText(m[2][3])
                txt4wsiRekapAbsensi2.setText(m[3][3])
                txt5wsiRekapAbsensi2.setText(m[4][3])
                txt6wsiRekapAbsensi2.setText(m[5][3])
                txt7wsiRekapAbsensi2.setText(m[6][3])
                txt1lmiRekapAbsensi2.setText(m[0][4])
                txt2lmiRekapAbsensi2.setText(m[1][4])
                txt3lmiRekapAbsensi2.setText(m[2][4])
                txt4lmiRekapAbsensi2.setText(m[3][4])
                txt5lmiRekapAbsensi2.setText(m[4][4])
                txt6lmiRekapAbsensi2.setText(m[5][4])
                txt7lmiRekapAbsensi2.setText(m[6][4])
                txt1lkiRekapAbsensi2.setText(m[0][5])
                txt2lkiRekapAbsensi2.setText(m[1][5])
                txt3lkiRekapAbsensi2.setText(m[2][5])
                txt4lkiRekapAbsensi2.setText(m[3][5])
                txt5lkiRekapAbsensi2.setText(m[4][5])
                txt6lkiRekapAbsensi2.setText(m[5][5])
                txt7lkiRekapAbsensi2.setText(m[6][5])
                txtWaktuKerjaSemingguRekapAbsensi2.setText("Total Waktu Kerja Minggu Ini: "+m[7][1])
                for (i in 1..7) {
                    dataentry.add(BarEntry(i.toFloat(), m[i-1][6].toFloat()/3600))
                }
                val barDataSet = BarDataSet(dataentry, "Waktu Kerja (jam)")
                barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                barDataSet.setValueTextColor(Color.WHITE)
                barDataSet.valueTextSize = 16f

                val barData = BarData(barDataSet)

                barchartRekapAbsensi.setFitBars(true)
//                barchartRekapAbsensi.setData(null)
                barchartRekapAbsensi.setData(barData)
                barchartRekapAbsensi.getDescription().setText("Chart Waktu Kerja")
                barchartRekapAbsensi.animateY(2000)
                val xAxis: XAxis = barchartRekapAbsensi.getXAxis()
                xAxis.textColor=Color.WHITE
                xAxis.setLabelCount(7)
                xAxis.setGranularity(1f);
                val yAxis: YAxis = barchartRekapAbsensi.axisLeft
                yAxis.textColor=Color.WHITE
                val yAxis2: YAxis = barchartRekapAbsensi.axisRight
                yAxis2.textColor=Color.WHITE
                createBar()
            }else{
//                txtInformasiIsiInfoAbsensi.setText(txtInformasiIsiInfoAbsensi.text.toString()+"\n")

            }

        }

    }
    private fun createBar(){
        barchartRekapAbsensi.getDescription().setEnabled(false);
        barchartRekapAbsensi.setDrawValueAboveBar(false);
        val barDataSet = BarDataSet(dataentry, "Waktu Kerja (jam)")
        barDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        barDataSet.setValueTextColor(Color.WHITE)
        barDataSet.valueTextSize = 12f
        val barData = BarData(barDataSet)

        barchartRekapAbsensi.setFitBars(true)
        barchartRekapAbsensi.setData(barData)
        barchartRekapAbsensi.getDescription().setText("Chart Waktu Kerja")
        barchartRekapAbsensi.animateY(2000)
        val xAxis: XAxis = barchartRekapAbsensi.getXAxis()
        xAxis.textColor=Color.WHITE
        xAxis.setGranularity(1f);
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return DAYS[value.toInt()-1]
            }
        }
        val yAxis: YAxis = barchartRekapAbsensi.axisLeft
        yAxis.textColor=Color.WHITE
        val yAxis2: YAxis = barchartRekapAbsensi.axisRight
        yAxis2.textColor=Color.WHITE
        val l: Legend = barchartRekapAbsensi.getLegend()
        l.textColor=Color.WHITE
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

