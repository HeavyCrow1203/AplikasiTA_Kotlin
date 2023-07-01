package com.example.aplikasita_kotlin.fragment

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.aplikasita_kotlin.Login
import com.example.aplikasita_kotlin.MainActivity
import com.example.aplikasita_kotlin.R
import com.example.aplikasita_kotlin.database.userApps
import com.github.lzyzsd.circleprogress.ArcProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class fragmentHome : Fragment() {

    private lateinit var view: View
    private lateinit var tanggal: TextView
    private lateinit var email: TextView
    private lateinit var vTemp: TextView
    private lateinit var vSoil: TextView
    private lateinit var output: TextView
    private lateinit var vJumlah: TextView
    private lateinit var gTemp: ArcProgress
    private lateinit var gSoil: ArcProgress
    private lateinit var button1: Button
    private lateinit var reference: DatabaseReference
    private var checkBackground: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_home, container, false)
        tanggal = view.findViewById(R.id.tanggaldanwaktu)
        email = view.findViewById(R.id.txtEmail)
        vTemp = view.findViewById(R.id.value_temp)
        vSoil = view.findViewById(R.id.value_soil)
        gTemp = view.findViewById(R.id.gauge_suhu)
        gSoil = view.findViewById(R.id.gauge_kelembaban)
        vJumlah = view.findViewById(R.id.jumlah_data)
        output = view.findViewById(R.id.hasil_fuzzy)
        button1 = view.findViewById(R.id.button)

        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userID: String = firebaseUser?.uid ?: ""
        reference = FirebaseDatabase.getInstance().getReference()
        reference.child("Users").child(userID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u: userApps? = snapshot.getValue(userApps::class.java)
                u?.let {
                    email.text = u.email
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        button1.setOnClickListener {
            val dialog: AlertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setPositiveButton("Ya") { dialogInterface: DialogInterface?, i: Int ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(requireActivity().applicationContext, Login::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Tidak", null)
                .create()
            dialog.show()
        }

        displayGauge()
        displayTimeStamp()
        displayTotalData()
        return view
    }

    private fun displayTotalData() {
        reference.child("Data").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hitung: Int = snapshot.childrenCount.toInt()
                vJumlah.text = "Jumlah Data : $hitung"
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun displayTimeStamp() {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val formatJam: DateFormat = SimpleDateFormat("HH:mm:ss")
                val formatTanggal: DateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy")
                tanggal.text =
                    formatTanggal.format(Date()) + ", " + formatJam.format(Date())
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun displayGauge() {
        reference.child("Data").limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot1 in snapshot.children) {
                    val bacasuhu: String = snapshot1.child("Suhu").getValue().toString()
                    val nilaisuhu: Float = bacasuhu.toFloat() * 100
                    val suhu: Int = Math.round(nilaisuhu)
                    gTemp.progress = suhu
                    gTemp.max = 10000
                    vTemp.text = bacasuhu + " °C"

                    val kelembabanTanah: String = snapshot1.child("Kelembaban_Tanah").getValue().toString()
                    val nilaiTanah: Float = kelembabanTanah.toFloat() * 100
                    val progress: Int = Math.round(nilaiTanah)
                    gSoil.progress = progress
                    gSoil.max = 10000
                    vSoil.text = kelembabanTanah + " %"

                    val durasi: String = snapshot1.child("Lama_Siram").getValue().toString()
                    val keterangan: String = snapshot1.child("keterangan").getValue().toString()

                    output.text = "$keterangan ($durasi detik)"

                    val prosesInfo = ActivityManager.RunningAppProcessInfo()
                    ActivityManager.getMyMemoryState(prosesInfo)
                    checkBackground = prosesInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    if (checkBackground) {
                        if (durasi.toFloat() >= 1.00) {
                            val intent = Intent(requireActivity().applicationContext, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                            val pendingIntent = PendingIntent.getActivity(
                                requireActivity().applicationContext,
                                0, intent, PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                            val notificationBuilder = NotificationCompat.Builder(
                                requireActivity().applicationContext, "CH1"
                            )
                                .setSmallIcon(R.drawable.logo1)
                                .setContentText(
                                    "Sistem Melakukan Penyiraman. (Suhu : $bacasuhu°C , " +
                                            "Kelembaban Tanah : $kelembabanTanah%, \nLama Penyiraman : " +
                                            "$durasi detik. " +
                                            "\nTap Untuk Membuka Aplikasi"
                                )
                                .setContentTitle("Pemberitahuan Sistem Melakukan Penyiraman")
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                            val notificationManager = requireActivity().applicationContext.getSystemService(
                                Context.NOTIFICATION_SERVICE
                            ) as NotificationManager?

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(
                                    "CH1",
                                    "Notifikasi",
                                    NotificationManager.IMPORTANCE_DEFAULT
                                )
                                notificationManager?.createNotificationChannel(channel)
                            }
                            notificationManager?.notify(1, notificationBuilder.build())
                        }
                    } else {
                        Log.d("EcoGarden", "Aplikasi sedang berjalan di latar depan")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireActivity().applicationContext, "Gagal Memuat Data", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}