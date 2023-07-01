package com.example.aplikasita_kotlin.fragment

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.aplikasita_kotlin.R
import com.example.aplikasita_kotlin.database.getData
import com.example.aplikasita_kotlin.marker_chart.Marker_Chart_Kelembaban
import com.example.aplikasita_kotlin.marker_chart.Marker_Chart_Lama_Siram
import com.example.aplikasita_kotlin.marker_chart.Marker_Chart_Suhu
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class fragmentChart : Fragment() {
    private lateinit var line_chart_suhu: LineChart
    private lateinit var line_chart_kelembaban: LineChart
    private lateinit var line_chart_siram: LineChart
    private lateinit var lineDataSet1: LineDataSet
    private lateinit var lineDataSet2: LineDataSet
    private lateinit var lineDataSet3: LineDataSet
    private lateinit var iLineDataSets1: ArrayList<ILineDataSet>
    private lateinit var iLineDataSets2: ArrayList<ILineDataSet>
    private lateinit var iLineDataSets3: ArrayList<ILineDataSet>
    private lateinit var lineData: LineData
    private lateinit var label: ArrayList<String>
    private lateinit var date: DatePickerDialog
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var filter_chart: Button
    private lateinit var tanggal: EditText
    private lateinit var dataku: getData

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        setHasOptionsMenu(true)
        line_chart_suhu = view.findViewById(R.id.grafik_suhu)
        line_chart_kelembaban = view.findViewById(R.id.grafik_kelembaban_tanah)
        line_chart_siram = view.findViewById(R.id.grafik_lama_siram)
        filter_chart = view.findViewById(R.id.aksi_filter)
        dataku = getData()
        filter_chart.setOnClickListener {
            if (tanggal.text.toString().isEmpty()) {
                tanggal.error = "isi dulu"
                tanggal.requestFocus()
            } else {
                filter_grafik()
            }
        }

        tampilkan_grafik()

        tanggal = view.findViewById(R.id.filter_grafik)
        tanggal.setOnClickListener {
            showDateDialog()
        }

        dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.US)
        return view
    }

    private fun showDateDialog() {
        val newCalendar = Calendar.getInstance()
        date = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate[year, monthOfYear] = dayOfMonth
                tanggal.setText(dateFormat.format(newDate.time))
            },
            newCalendar[Calendar.YEAR],
            newCalendar[Calendar.MONTH],
            newCalendar[Calendar.DAY_OF_MONTH]
        )
        date.show()
    }

    private fun tampilkan_grafik() {
        lineDataSet1 = LineDataSet(null, null)
        iLineDataSets1 = ArrayList()
        lineDataSet2 = LineDataSet(null, null)
        iLineDataSets2 = ArrayList()
        lineDataSet3 = LineDataSet(null, null)
        iLineDataSets3 = ArrayList()
        label = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Data")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries1 = ArrayList<Entry>()
                val entries2 = ArrayList<Entry>()
                val entries3 = ArrayList<Entry>()

                if (snapshot.hasChildren()) {
                    var i = -1
                    for (snapshot1 in snapshot.children) {
                        i++
                        val suhu = snapshot1.child("Suhu").value.toString()
                        val kelembaban_tanah = snapshot1.child("Kelembaban_Tanah").value.toString()
                        val durasi_siram = snapshot1.child("Lama_Siram").value.toString()
                        val timestamp = snapshot1.child("Waktu").value.toString()
                        entries1.add(Entry(i.toFloat(), suhu.toFloat()))
                        entries2.add(Entry(i.toFloat(), kelembaban_tanah.toFloat()))
                        entries3.add(Entry(i.toFloat(), durasi_siram.toFloat()))
                        label.add(timestamp)
                    }
                    lineDataSet1.values = entries1
                    display_Chart(line_chart_suhu, lineDataSet1, iLineDataSets1, "Suhu (°C)",
                        Marker_Chart_Suhu(requireContext(), R.layout.marker_chart_suhu, label), Color.BLUE)
                    lineDataSet2.values = entries2
                    display_Chart(line_chart_kelembaban, lineDataSet2, iLineDataSets2, "Kelembaban Tanah (%)",
                        Marker_Chart_Kelembaban(requireContext(), R.layout.marker_chart_kelembaban, label), Color.GREEN)
                    lineDataSet3.values = entries3
                    display_Chart(line_chart_siram, lineDataSet3, iLineDataSets3, "Lama Siram (detik)",
                        Marker_Chart_Lama_Siram(requireContext(), R.layout.custom_mark_chart, label), Color.RED)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                pesanToast("Gagal Memuat Data")
            }
        })
    }

    private fun filter_grafik() {
        lineDataSet1 = LineDataSet(null, null)
        iLineDataSets1 = ArrayList()
        lineDataSet2 = LineDataSet(null, null)
        iLineDataSets2 = ArrayList()
        lineDataSet3 = LineDataSet(null, null)
        iLineDataSets3 = ArrayList()
        label = ArrayList()
        val reference1 = FirebaseDatabase.getInstance().getReference("Data")
        reference1.orderByChild("Tanggal").equalTo(tanggal.text.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val entries1 = ArrayList<Entry>()
                    val entries2 = ArrayList<Entry>()
                    val entries3 = ArrayList<Entry>()

                    if (snapshot.hasChildren()) {
                        var i = -1
                        for (snapshot1 in snapshot.children) {
                            i++
                            val suhu = snapshot1.child("Suhu").value.toString()
                            val kelembaban_tanah = snapshot1.child("Kelembaban_Tanah").value.toString()
                            val durasi_siram = snapshot1.child("Lama_Siram").value.toString()
                            val timestamp = snapshot1.child("Waktu").value.toString()
                            entries1.add(Entry(i.toFloat(), suhu.toFloat()))
                            entries2.add(Entry(i.toFloat(), kelembaban_tanah.toFloat()))
                            entries3.add(Entry(i.toFloat(), durasi_siram.toFloat()))
                            label.add(timestamp)
                        }
                        lineDataSet1.values = entries1
                        display_Chart(line_chart_suhu, lineDataSet1, iLineDataSets1, "Suhu (°C)",
                            Marker_Chart_Suhu(requireContext(), R.layout.marker_chart_suhu, label), Color.BLUE)
                        lineDataSet2.values = entries2
                        display_Chart(line_chart_kelembaban, lineDataSet2, iLineDataSets2, "Kelembaban Tanah (%)",
                            Marker_Chart_Kelembaban(requireContext(), R.layout.marker_chart_kelembaban, label), Color.GREEN)
                        lineDataSet3.values = entries3
                        display_Chart(line_chart_siram, lineDataSet3, iLineDataSets3, "Lama Siram (detik)",
                            Marker_Chart_Lama_Siram(requireContext(), R.layout.custom_mark_chart, label), Color.RED)
                    } else {
                        val dialog: AlertDialog = AlertDialog.Builder(requireContext())
                            .setTitle("Data pada grafik tidak ditemukan")
                            .setNegativeButton("Kembali",
                                DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                }).create()
                        dialog.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    pesanToast("Gagal Memuat Data")
                }
            })
    }

    private fun display_Chart(lineChart: LineChart, lineDataSet: LineDataSet, iLineDataSets: ArrayList<ILineDataSet>,
                              keterangan: String, iMarker: IMarker, warna: Int) {
        lineDataSet.label = keterangan
        lineDataSet.color = warna
        lineDataSet.circleRadius = 2f
        lineDataSet.setCircleColor(warna)
        iLineDataSets.clear()
        iLineDataSets.add(lineDataSet)
        lineData = LineData(iLineDataSets)
        lineChart.clear()
        lineChart.data = lineData
        lineChart.invalidate()
        lineChart.isDoubleTapToZoomEnabled = true
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(label)
        lineChart.animateX(2000)
        lineDataSet.lineWidth = 2f
        lineChart.setScaleEnabled(true)
        lineChart.legend.setDrawInside(false)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.description.isEnabled = false
        lineChart.marker = iMarker
    }

    private fun pesanToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
