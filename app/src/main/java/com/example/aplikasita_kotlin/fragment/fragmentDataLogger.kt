package com.example.aplikasita_kotlin.fragment

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasita_kotlin.R
import com.example.aplikasita_kotlin.database.getData
import com.example.aplikasita_kotlin.etc.AdapterListData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class fragmentDataLogger : Fragment() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var rv_data: RecyclerView
    private lateinit var dataFirebaseList: List<getData>
    private lateinit var adapter: AdapterListData
    private val STORAGE_PERMISSION_CODE = 101
    private lateinit var file: File
    private lateinit var fileExcel: Button
    private lateinit var eventListener: ValueEventListener

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        val view = inflater.inflate(R.layout.fragment_data_logger, container, false)
        setHasOptionsMenu(true)
        checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
        databaseReference = FirebaseDatabase.getInstance().getReference("Data")
        rv_data = view.findViewById(R.id.list_data_2)
        list_view()
        eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataFirebaseList = ArrayList()
                for (snapshot1 in snapshot.children) {
                    val load: getData? = snapshot1.getValue(getData::class.java)
                    load?.key = snapshot1.key!!
                    load?.let { (dataFirebaseList as ArrayList<getData>).add(it) }
                }
                adapter = AdapterListData(dataFirebaseList as ArrayList<getData>, requireContext())
                rv_data.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity().applicationContext, "Gagal Memuat Data", Toast.LENGTH_SHORT).show()
            }
        }
        databaseReference.addValueEventListener(eventListener)

        fileExcel = view.findViewById(R.id.generateExcel)
        fileExcel.setOnClickListener {
            generateExcel()
        }
        return view
    }

    private fun list_view() {
        val linearLayoutManager = LinearLayoutManager(requireActivity().applicationContext)
        rv_data.setHasFixedSize(true)
        rv_data.layoutManager = linearLayoutManager
    }

    private fun list_reverse_view() {
        val linearLayoutManager = LinearLayoutManager(requireActivity().applicationContext)
        rv_data.setHasFixedSize(true)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rv_data.layoutManager = linearLayoutManager
    }

    private fun generateExcel() {
        val wb: Workbook = HSSFWorkbook()
        var cell: Cell?
        val cellStyle: CellStyle = wb.createCellStyle()
        cellStyle.setFillForegroundColor(HSSFColor.AQUA.index)
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND)
        cellStyle.setAlignment(HorizontalAlignment.CENTER)

        val cellStyle1: CellStyle = wb.createCellStyle()
        cellStyle1.setAlignment(HorizontalAlignment.CENTER)

        val sheet: Sheet = wb.createSheet("Sheet 1")

        val row: Row = sheet.createRow(0)
        val row1: Row = sheet.createRow(2)

        cell = row.createCell(0)
        cell.setCellValue("Data Monitoring Tanaman Daun Mint")
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))
        cell.cellStyle = cellStyle1

        cell = row1.createCell(0)
        cell.setCellValue("No.")
        cell.cellStyle = cellStyle

        cell = row1.createCell(1)
        cell.setCellValue("Tanggal")
        cell.cellStyle = cellStyle

        cell = row1.createCell(2)
        cell.setCellValue("Waktu")
        cell.cellStyle = cellStyle

        cell = row1.createCell(3)
        cell.setCellValue("Suhu (*C)")
        cell.cellStyle = cellStyle

        cell = row1.createCell(4)
        cell.setCellValue("Kelembaban Tanah \n\n (%)")
        cell.cellStyle = cellStyle

        cell = row1.createCell(5)
        cell.setCellValue("Lama Siram")
        cell.cellStyle = cellStyle

        cell = row1.createCell(6)
        cell.setCellValue("keterangan")
        cell.cellStyle = cellStyle

        sheet.setColumnWidth(0, (10 * 100))
        sheet.setColumnWidth(1, (10 * 800))
        sheet.setColumnWidth(2, (10 * 800))
        sheet.setColumnWidth(3, (10 * 500))
        sheet.setColumnWidth(4, (10 * 500))
        sheet.setColumnWidth(5, (10 * 500))
        sheet.setColumnWidth(6, (10 * 500))

        val dateFile = dateFile()

        dataFirebaseList = ArrayList()
        for (i in dataFirebaseList.indices) {
            val row2: Row = sheet.createRow(i + 3)

            if (dataFirebaseList[i] == null) {
                sheet.shiftRows(i + 1, dataFirebaseList.size, -1)
                continue
            }

            cell = row2.createCell(0)
            cell.setCellValue((i + 1).toString())
            cell.cellStyle = cellStyle1

            cell = row2.createCell(1)
            cell.setCellValue(dataFirebaseList[i].Tanggal)
            cell.cellStyle = cellStyle1

            cell = row2.createCell(2)
            cell.setCellValue(dataFirebaseList[i].Waktu)
            cell.cellStyle = cellStyle1

            cell = row2.createCell(3)
            cell.setCellValue(dataFirebaseList[i].Suhu.toString())
            cell.cellStyle = cellStyle1

            cell = row2.createCell(4)
            cell.setCellValue(dataFirebaseList[i].Kelembaban_Tanah.toString())
            cell.cellStyle = cellStyle1

            cell = row2.createCell(5)
            cell.setCellValue(dataFirebaseList[i].Lama_Siram.toString())
            cell.cellStyle = cellStyle1

            cell = row2.createCell(6)
            cell.setCellValue(dataFirebaseList[i].keterangan)
            cell.cellStyle = cellStyle1
        }

        val fileName = "File $dateFile.xls"
        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(file)
            wb.write(outputStream)

            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val pendingIntent = PendingIntent.getActivity(requireActivity().applicationContext, STORAGE_PERMISSION_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(requireActivity().applicationContext, "CH1")
                .setSmallIcon(R.drawable.logo1)
                .setContentText("$fileName berhasil disimpan ke folder Downloads")
                .setContentTitle("Pemberitahuan Sistem")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val notificationManager = requireActivity().applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("CH1", "Notifikasi", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(1, notificationBuilder.build())

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireActivity().applicationContext, "NO OK", Toast.LENGTH_LONG).show()
            try {
                outputStream?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, permission) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(permission), requestCode)
        } else {
            Toast.makeText(requireActivity().applicationContext, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Toast.makeText(requireActivity().applicationContext, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity().applicationContext, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_data, menu)
        val item: MenuItem = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText.toLowerCase())
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun dateFile(): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd MMMM YYYY HH:MM:ss")
        val date = Date()
        return dateFormat.format(date)
    }

    private fun filter(data: String) {
        val list: MutableList<getData> = ArrayList()
        for (s in dataFirebaseList) {
            if (s.Tanggal?.toLowerCase()?.contains(data.toLowerCase()) == true) {
                list.add(s)
            }
        }
        adapter.filterlist(list)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val pilih = item.itemId
        if (pilih == R.id.action_sort) {
            if (pilih == R.id.action_sort_ascending) {
                list_view()
            } else if (pilih == R.id.action_sort_descending) {
                list_reverse_view()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}