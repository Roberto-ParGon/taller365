package com.uv.taller365

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var workshopId: String
    private var userRole: String = "worker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        workshopId = intent.getStringExtra("WORKSHOP_ID") ?: ""
        userRole = obtenerRolUsuario()

        configurarColoresSistema()
        inicializarViews()
        configurarViewPager()
        configurarNavegacion()

        aplicarRestriccionesPorRol()
    }

    // ---------------------- Configuración inicial ----------------------

    private fun configurarColoresSistema() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    private fun inicializarViews() {
        bottomNav = findViewById(R.id.bottomNav)
        viewPager = findViewById(R.id.viewpager)
    }

    private fun configurarViewPager() {
        adapter = FragmentStateAdapter(this, workshopId)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < bottomNav.menu.size()) {
                    bottomNav.menu.getItem(position).isChecked = true
                }
            }
        })
    }

    private fun configurarNavegacion() {
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.vehiclesNav -> viewPager.currentItem = 0
                R.id.repaisNav   -> viewPager.currentItem = 1
                R.id.clientsNav  -> viewPager.currentItem = 2
                R.id.configNav   -> viewPager.currentItem = 3
            }
            true
        }
    }

    private fun obtenerRolUsuario(): String {
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "worker") ?: "worker"
    }

    // ---------------------- Personalización por rol ----------------------

    private fun aplicarRestriccionesPorRol() {
        if (userRole != "admin") {
            // Oculta la opción de configuración
            bottomNav.menu.removeItem(R.id.configNav)

            // Ajusta el adaptador para 3 fragmentos
            adapter.setRole(userRole)
            adapter.setItemCount(3)
            viewPager.adapter = adapter
        }
    }
}
