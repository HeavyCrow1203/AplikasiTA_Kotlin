package com.example.aplikasita_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.aplikasita_kotlin.fragment.fragmentChart
import com.example.aplikasita_kotlin.fragment.fragmentDataLogger
import com.example.aplikasita_kotlin.fragment.fragmentHome
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val navigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {item ->
        val fragment: Fragment
        when (item.itemId) {
            R.id.home -> {
                fragment = fragmentHome()
                title = "Dashboard"
            }
            R.id.datas -> {
                fragment = fragmentDataLogger()
                title = "Data Logger"
            }
            R.id.grafik -> {
                fragment = fragmentChart()
                title = "Chart"
            }
            else -> return@OnNavigationItemSelectedListener false
        }
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("Dashboard")

        bottomNavigationView = findViewById(R.id.menunya)
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentHome()).commit()
    }
}