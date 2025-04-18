package com.uv.taller365

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadFragment(VehiclesFragment())
        bottomNav = findViewById(R.id.bottomNav)!!
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.vehiclesNav -> {
                    loadFragment(VehiclesFragment())
                    true
                }
                R.id.repaisNav -> {
                    loadFragment(RepairFragment())
                    true
                }
                R.id.clientsNav -> {
                    loadFragment(ClientsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

}
