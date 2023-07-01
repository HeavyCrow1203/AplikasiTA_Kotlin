package com.example.aplikasita_kotlin.etc

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasita_kotlin.R
import com.example.aplikasita_kotlin.database.getData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdapterListData (private var list: List<getData>, private val context: Context) :
    RecyclerView.Adapter<AdapterListData.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val temp: TextView = itemView.findViewById(R.id.textsuhu)
        val moist: TextView = itemView.findViewById(R.id.textsoil)
        val duration: TextView = itemView.findViewById(R.id.textduration)
        val status: TextView = itemView.findViewById(R.id.textstatus)
        val timestamp: TextView = itemView.findViewById(R.id.textdate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_card_data, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data1 = list[position]
        holder.temp.text = "Suhu : ${data1.Suhu}\u00B0C"
        holder.moist.text = "Kelembaban Tanah : ${data1.Kelembaban_Tanah} %"
        holder.duration.text = "Lama Siram : ${data1.Lama_Siram} detik"
        holder.status.text = "Keterangan : ${data1.keterangan}"
        holder.timestamp.text = "${data1.Tanggal}, ${list[position].Waktu}"
        holder.itemView.setOnClickListener {
            val dialog: AlertDialog = AlertDialog.Builder(it.context)
                .setTitle("Hapus Item ?")
                .setPositiveButton("Hapus") { dialogInterface: DialogInterface, i: Int ->
                    onDeleteData(list[position], position)
                }
                .setNegativeButton("Batal", null)
                .create()
            dialog.show()
        }
    }

    private fun onDeleteData(data2: getData, position: Int) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Data")
        if (databaseReference != null) {
            databaseReference.child(data2.key!!).removeValue()
                .addOnSuccessListener { unused: Void? ->
                    Toast.makeText(
                        context.applicationContext,
                        "Data Berhasil di hapus",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    fun filterlist(list: List<getData>) {
        this.list = list
        notifyDataSetChanged()
    }


}