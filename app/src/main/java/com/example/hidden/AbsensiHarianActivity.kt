package com.example.hidden

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_absensi_harian.*
import java.text.SimpleDateFormat
import java.util.*


class AbsensiHarianActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    var list=ArrayList<AnggotasAbsensiHarian>()
    private lateinit var perusahaanID:String
    private lateinit var tanggal:String
    private lateinit var tanggalTahun:String
    private lateinit var tanggalBulan:String
    private lateinit var tanggalHari:String
    private lateinit var waktuJam:String
    private lateinit var waktuMenit:String
    private lateinit var waktuDetik:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absensi_harian)
        database = Firebase.database.reference
        var userID= Firebase.auth.currentUser?.uid.toString()
        database.child("timestamp").setValue(ServerValue.TIMESTAMP)
        database.child("timestamp").get().addOnSuccessListener {
            var timestamp=it.value
            tanggal=getDate(timestamp as Long)
            editTanggalAbsensiHarian.setText(tanggal)

        }.addOnFailureListener{
            Log.e("timestampfromdatabase", "Error getting data", it)
        }
        mRecyclerViewAbsensi.setHasFixedSize(true)
        mRecyclerViewAbsensi.layoutManager= LinearLayoutManager(this@AbsensiHarianActivity)
        val adapter=AdapterRecyclerViewAbsensiHarian(list)
        mRecyclerViewAbsensi.adapter=adapter
        adapter.setOnItemClickListener(object:AdapterRecyclerViewAbsensiHarian.onItemClickListener{
            override fun onItemClick(position: Int) {
                Log.v("hello",list[position].toString())

            }
        })
        database.child("users").child(userID).child("perusahaan_id").get().addOnSuccessListener {
            perusahaanID=it.value.toString()
            val usersRef = database.child("perusahaan").child(perusahaanID).child("anggota")
            val valueEventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val uid = ds.key
                        Log.d("listanggota", uid!!)
                        database.child("perusahaan").child(perusahaanID).child("anggota").child(uid.toString()).get().addOnSuccessListener {
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

                                    var longWaktuMasuk=it.child(uid.toString()).child("jam_masuk").value
                                    var longWaktuKeluar=it.child(uid.toString()).child("jam_keluar").value
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

                                    var strlokasiLatitudeMasuk=it.child(uid.toString()).child("lokasi_latitude_masuk").value.toString()
                                    var strlokasiLongitudeMasuk=it.child(uid.toString()).child("lokasi_longitude_masuk").value.toString()
                                    var strlokasiLatitudeKeluar=it.child(uid.toString()).child("lokasi_latitude_keluar").value.toString()
                                    var strlokasiLongitudeKeluar=it.child(uid.toString()).child("lokasi_longitude_keluar").value.toString()
                                    if(it.child(uid.toString()).child("lokasi_latitude_masuk").value!=null){
                                        lokasiLatitudeMasuk=strlokasiLatitudeMasuk
                                        lokasiLongitudeMasuk=strlokasiLongitudeMasuk
                                    }
                                    else{
                                        lokasiLatitudeMasuk=""
                                        lokasiLongitudeMasuk=""
                                    }
                                    if(it.child(uid.toString()).child("lokasi_latitude_keluar").value!=null){
                                        lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                        lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                    }
                                    else{
                                        lokasiLatitudeKeluar=""
                                        lokasiLongitudeKeluar=""
                                    }
                                    list.add(AnggotasAbsensiHarian(nama_user,gambar_user,wjm,wjk,wmm,wmk,wdm,wdk,lokasiLatitudeMasuk,lokasiLatitudeKeluar,lokasiLongitudeMasuk,lokasiLongitudeKeluar))
                                    adapter.notifyDataSetChanged()
                                }

                            }
                            Log.v("user_id",user_id)

                        }.addOnFailureListener {
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            Log.v("list",list.toString())
            usersRef.addListenerForSingleValueEvent(valueEventListener)
        }
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        editTanggalAbsensiHarian.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, myear, mmonth, mdayOfMonth ->

                tanggalTahun=myear.toString()
                var intTanggalBulan=mmonth+1
                if(intTanggalBulan-10>=0){
                    tanggalBulan=intTanggalBulan.toString()
                }else{
                    tanggalBulan="0"+intTanggalBulan.toString()
                }

                tanggalHari=mdayOfMonth.toString()
                editTanggalAbsensiHarian.setText(""+ tanggalHari +"/"+ tanggalBulan +"/"+ tanggalTahun)
                list.clear()
                val usersRef = database.child("perusahaan").child(perusahaanID).child("anggota")
                val valueEventListener: ValueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (ds in dataSnapshot.children) {
                            val uid = ds.key
                            Log.d("listanggota", uid!!)
                            database.child("perusahaan").child(perusahaanID).child("anggota").child(uid.toString()).get().addOnSuccessListener {
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

                                        var longWaktuMasuk=it.child(uid.toString()).child("jam_masuk").value
                                        var longWaktuKeluar=it.child(uid.toString()).child("jam_keluar").value
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

                                        var strlokasiLatitudeMasuk=it.child(uid.toString()).child("lokasi_latitude_masuk").value.toString()
                                        var strlokasiLongitudeMasuk=it.child(uid.toString()).child("lokasi_longitude_masuk").value.toString()
                                        var strlokasiLatitudeKeluar=it.child(uid.toString()).child("lokasi_latitude_keluar").value.toString()
                                        var strlokasiLongitudeKeluar=it.child(uid.toString()).child("lokasi_longitude_keluar").value.toString()
                                        if(it.child(uid.toString()).child("lokasi_latitude_masuk").value!=null){
                                            lokasiLatitudeMasuk=strlokasiLatitudeMasuk
                                            lokasiLongitudeMasuk=strlokasiLongitudeMasuk
                                        }
                                        else{
                                            lokasiLatitudeMasuk=""
                                            lokasiLongitudeMasuk=""
                                        }
                                        if(it.child(uid.toString()).child("lokasi_latitude_keluar").value!=null){
                                            lokasiLatitudeKeluar=strlokasiLatitudeKeluar
                                            lokasiLongitudeKeluar=strlokasiLongitudeKeluar
                                        }
                                        else{
                                            lokasiLatitudeKeluar=""
                                            lokasiLongitudeKeluar=""
                                        }
                                        list.add(AnggotasAbsensiHarian(nama_user,gambar_user,wjm,wjk,wmm,wmk,wdm,wdk,lokasiLatitudeMasuk,lokasiLatitudeKeluar,lokasiLongitudeMasuk,lokasiLongitudeKeluar))
                                        adapter.notifyDataSetChanged()
                                    }

                                }
                                Log.v("user_id",user_id)

                            }.addOnFailureListener {
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
                Log.v("list",list.toString())
                usersRef.addListenerForSingleValueEvent(valueEventListener)
            }, year, month, day)
            datePickerDialog.show()

        }

//        val items = arrayOf("1", "2", "3","4","5","6","7","8","9","10","11","12","13"
//        ,"14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31")
//        // access the spinner
//        if (spinner1 != null) {
//            val adapter = ArrayAdapter(this,
//                android.R.layout.simple_spinner_item, items)
//            spinner1.adapter = adapter
//
//            spinner1.onItemSelectedListener = object :
//                AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>,
//                                            view: View, position: Int, id: Long) {
//                    Toast.makeText(this@AbsensiHarianActivity,
//                        items[position], Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {
//                    // write code to perform some action
//                }
//            }
//        }
//        editText.setOnClickListener {
//            PopupMenu(this, editText).apply {
//                menuInflater.inflate(R.menu.menu_tanggal, menu)
//                setOnMenuItemClickListener { item ->
//                    editText.setText(item.title)
//                    true
//                }
//                show()
//            }
//        }
    }
//    fun init() {
//        val gradientDrawableDefault = GradientDrawable()
//        gradientDrawableDefault.setStroke(
//            1,
//            getResources().getColor(R.color.black)
//        )
//
//        val tbrow0 = TableRow(this)
//        val tv0 = TextView(this)
//        tv0.text = " No. "
//        tv0.background=gradientDrawableDefault
//        tv0.setTextColor(getResources().getColor(R.color.white))
//        tbrow0.addView(tv0)
//        val tv1 = TextView(this)
//        tv1.text = " Product "
//        tv1.setTextColor(getResources().getColor(R.color.white))
//        tv1.background=gradientDrawableDefault
//        tbrow0.addView(tv1)
//        val tv2 = TextView(this)
//        tv2.text = " Unit Price "
//        tv2.setTextColor(getResources().getColor(R.color.white))
//        tv2.background=gradientDrawableDefault
//        tbrow0.addView(tv2)
//        val tv3 = TextView(this)
//        tv3.text = " Stock Remaining "
//        tv3.setTextColor(getResources().getColor(R.color.white))
//        tv3.background=gradientDrawableDefault
//        tbrow0.addView(tv3)
//        table_main.addView(tbrow0)
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
//    }
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
