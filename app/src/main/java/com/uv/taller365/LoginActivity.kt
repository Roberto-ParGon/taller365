package com.uv.taller365

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.buttonLogin)
        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnCreate = findViewById<Button>(R.id.buttonSign)
        btnCreate.setOnClickListener {
            val intent = Intent(this, CreateWorkshop::class.java)
            startActivity(intent)
        }
    }
}

/*
Activity:
val dialog = SuccessWorkshopDialog.newInstance("12D1X420")
dialog.show(supportFragmentManager, "SuccessWorkshopDialog")
Fragment:
dialog.show(childFragmentManager, "SuccessWorkshopDialog")
 */