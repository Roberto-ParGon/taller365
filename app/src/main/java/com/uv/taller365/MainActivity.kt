package com.uv.taller365

import android.view.View
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: FragmentStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val workshopId = intent.getStringExtra("WORKSHOP_ID") ?: ""

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        bottomNav = findViewById(R.id.bottomNav)
        viewPager = findViewById(R.id.viewpager)

        adapter = FragmentStateAdapter(this, workshopId)
        viewPager.adapter = adapter

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.vehiclesNav -> viewPager.currentItem = 0
                R.id.repaisNav -> viewPager.currentItem = 1
                R.id.clientsNav -> viewPager.currentItem = 2
                R.id.configNav -> viewPager.currentItem = 3
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu[position].isChecked = true
            }
        })
    }
}

