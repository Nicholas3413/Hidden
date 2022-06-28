package com.example.hidden

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_splash.view.*
import kotlinx.android.synthetic.main.list_karyawan.view.*

class AdapterRecyclerView(private val list:ArrayList<Anggotas>): RecyclerView.Adapter<AdapterRecyclerView.Holder>(){
    private lateinit var mListener:onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener){
        mListener=listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.list_karyawan,parent,false)
        return Holder(itemView,mListener)
    }

    override fun getItemCount(): Int=list.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.namaList.text=list.get(position).nama_anggota
        holder.itemView.emailList.text=list.get(position).email_anggota
        Glide.with(holder.itemView.context)
            .load(list.get(position).gambar_anggota)
            .circleCrop()
            .placeholder(R.drawable.perusahaan)
            .into(holder.itemView.imgList)

    }

    class Holder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}