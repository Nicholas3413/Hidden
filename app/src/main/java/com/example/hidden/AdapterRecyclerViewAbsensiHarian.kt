package com.example.hidden

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_edit_perusahaan.*
import kotlinx.android.synthetic.main.list_absensi_harian.view.*
import kotlinx.android.synthetic.main.list_absensi_harian.view.namaList
import kotlinx.android.synthetic.main.list_karyawan.view.*

class AdapterRecyclerViewAbsensiHarian(private val list:ArrayList<AnggotasAbsensiHarian>): RecyclerView.Adapter<AdapterRecyclerViewAbsensiHarian.Holder>(){
    private lateinit var mListener:onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener){
        mListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRecyclerViewAbsensiHarian.Holder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.list_absensi_harian,parent,false)
        return AdapterRecyclerViewAbsensiHarian.Holder(itemView, mListener)
    }

    override fun getItemCount(): Int=list.size
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: AdapterRecyclerViewAbsensiHarian.Holder, position: Int) {
        holder.itemView.namaList.text=list.get(position).nama_anggota
        holder.itemView.masukList.setTextColor(Color.parseColor("black"))
        holder.itemView.masukList.textSize=16.0f
        holder.itemView.keluarList.setTextColor(Color.parseColor("black"))
        holder.itemView.keluarList.textSize=16.0f
        Glide.with(holder.itemView.context)
            .load(list.get(position).gambar_anggota)
            .circleCrop()
            .placeholder(R.drawable.avatar)
            .into(holder.itemView.imgListAbsensiHarian)
        val sharedPreferences = holder.itemView.context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

        if(list.get(position).lokasi_latitude_masuk!=""){
            holder.itemView.masukLokasiList.text="Lokasi masuk"
            holder.itemView.masukLokasiList.setOnClickListener(View.OnClickListener {
                val gmmIntentUri = Uri.parse("geo:"+list.get(position).lokasi_latitude_masuk+","+list.get(position).lokasi_longitude_masuk)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                mapIntent.resolveActivity(holder.itemView.context.packageManager)?.let {
                    holder.itemView.context.startActivity(mapIntent)
                }
            })
        }
        else{
            holder.itemView.masukLokasiList.text=""
        }
        if(list.get(position).lokasi_latitude_keluar!=""){
            holder.itemView.keluarLokasiList.text="Lokasi keluar"
            holder.itemView.keluarLokasiList.setOnClickListener(View.OnClickListener {
                val gmmIntentUri = Uri.parse("geo:"+list.get(position).lokasi_latitude_keluar+","+list.get(position).lokasi_longitude_keluar)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                mapIntent.resolveActivity(holder.itemView.context.packageManager)?.let {
                    holder.itemView.context.startActivity(mapIntent)
                }
            })
        }
        else{
            holder.itemView.keluarLokasiList.text=""
        }
        var jamMasuk=sharedPreferences.getString("jam_masuk","")
        var menitMasuk=sharedPreferences.getString("menit_masuk","")
        var jamKeluar=sharedPreferences.getString("jam_pulang","")
        var menitKeluar=sharedPreferences.getString("menit_pulang","")
        var loclamin=sharedPreferences.getString("loclamin","")
        var loclapos=sharedPreferences.getString("loclapos","")
        var loclongmin=sharedPreferences.getString("loclongmin","")
        var loclongpos=sharedPreferences.getString("loclongpos","")
        if(jamMasuk!=""){
            var totpmasuk:Int=(jamMasuk!!.toInt()*60+menitMasuk!!.toInt())*60
            if(list.get(position).jam_masuk!=""){
                holder.itemView.masukList.text=list.get(position).jam_masuk+":"+list.get(position).menit_masuk+":"+list.get(position).detik_masuk
                var totwmasuk:Int= (list.get(position).jam_masuk!!.toInt()*60+list.get(position).menit_masuk!!.toInt())*60+list.get(position).detik_masuk!!.toInt()
                if(totwmasuk<=totpmasuk){
                    Log.v("totwmasuk",totwmasuk.toString())
                    Log.v("totpmasuk",totpmasuk.toString())
                    holder.itemView.masukList.setTextColor(Color.parseColor("green"))
                }
                else{
                    holder.itemView.masukList.setTextColor(Color.parseColor("red"))
                }
            }
            else{
                holder.itemView.masukList.text="Belum Absensi Masuk"
                holder.itemView.masukList.textSize=12.0f
            }
        }
        Log.v("totjamkeluar",jamKeluar.toString())
        if(jamKeluar!=""){
            var totpkeluar:Int=(jamKeluar!!.toInt()*60+menitKeluar!!.toInt())*60
            if(list.get(position).jam_keluar!=""){
                holder.itemView.keluarList.text=list.get(position).jam_keluar+":"+list.get(position).menit_keluar+":"+list.get(position).detik_keluar
                var totwkeluar:Int= (list.get(position).jam_keluar!!.toInt()*60+list.get(position).menit_keluar!!.toInt())*60+list.get(position).detik_keluar!!.toInt()
                Log.v("totwkeluar",totwkeluar.toString())
                Log.v("totpkeluar",totpkeluar.toString())
                if(totwkeluar<=totpkeluar){
                    holder.itemView.keluarList.setTextColor(Color.parseColor("red"))
                }
                else{
                    holder.itemView.keluarList.setTextColor(Color.parseColor("green"))
                }
            }
            else{
                holder.itemView.keluarList.text="Belum Absensi Keluar"
                holder.itemView.keluarList.textSize=12.0f
            }
        }
        if(loclamin!=""){
            if(list.get(position).lokasi_latitude_masuk!=""){
                if(list.get(position).lokasi_latitude_masuk!!.toFloat()>=loclamin!!.toFloat() &&
                    list.get(position).lokasi_latitude_masuk!!.toFloat()<=loclapos!!.toFloat() &&
                    list.get(position).lokasi_longitude_masuk!!.toFloat()>=loclongmin!!.toFloat() &&
                    list.get(position).lokasi_longitude_masuk!!.toFloat()<=loclongpos!!.toFloat()){
                    holder.itemView.masukLokasiList.setTextColor(Color.parseColor("green"))
                }
                else{
                    holder.itemView.masukLokasiList.setTextColor(Color.parseColor("red"))
                }
            }
            if(list.get(position).lokasi_latitude_keluar!=""){
                if(list.get(position).lokasi_latitude_keluar!!.toFloat()>=loclamin!!.toFloat()&&
                    list.get(position).lokasi_latitude_keluar!!.toFloat()<=loclapos!!.toFloat()&&
                    list.get(position).lokasi_longitude_keluar!!.toFloat()>=loclongmin!!.toFloat() &&
                    list.get(position).lokasi_longitude_keluar!!.toFloat()<=loclongpos!!.toFloat()  ){
                    holder.itemView.keluarLokasiList.setTextColor(Color.parseColor("green"))
                }
                else{
                    holder.itemView.keluarLokasiList.setTextColor(Color.parseColor("red"))
                }
            }
        }


    }

    class Holder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }



}