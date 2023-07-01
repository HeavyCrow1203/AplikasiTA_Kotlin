package com.example.aplikasita_kotlin.marker_chart

import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.example.aplikasita_kotlin.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Marker_Chart_Suhu (context: Context, layoutResource: Int, xLabel: ArrayList<String>) :
    MarkerView(context, layoutResource) {
    private var tanggal_value: TextView
    private var waktu_value: TextView
    private var suhu: TextView
    private var reference: DatabaseReference
    private var label_waktu: ArrayList<String>

    init {
        label_waktu = xLabel
        suhu = findViewById(R.id.txtInfo)
        tanggal_value = findViewById(R.id.txtTitle)
        waktu_value = findViewById(R.id.txtWaktu)
        reference = FirebaseDatabase.getInstance().getReference("Data")
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val waktu = label_waktu[e?.x?.toInt() ?: 0]
        val nilai_suhu = e?.y.toString()

        reference.orderByChild("Waktu").equalTo(waktu)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        var i = -1
                        for (snapshot1 in snapshot.children) {
                            i++
                            val date_chart = snapshot1.child("Tanggal").value.toString()
                            suhu.text = "Suhu Udara : $nilai_suhu °C"
                            waktu_value.text = waktu
                            tanggal_value.text = date_chart
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal Memuat Data", Toast.LENGTH_SHORT).show()
                }
            })

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}