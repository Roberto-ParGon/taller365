package com.uv.taller365

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog



class CreateWorkshop : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_workshop)

        // Ajuste de insets del sistema (barra de estado, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los campos del formulario
        val nombreTaller = findViewById<EditText>(R.id.etWorkshopName)
        val direccionTaller = findViewById<EditText>(R.id.etWorkshopAddress)
        val telefonoEncargado = findViewById<EditText>(R.id.etPhone)
        val correoEncargado = findViewById<EditText>(R.id.etEmail)
        val btnCrearTaller = findViewById<Button>(R.id.btnCreateWorkshop)

        // Acción del botón
        btnCrearTaller.setOnClickListener {
            val nombre = nombreTaller.text.toString().trim()
            val direccion = direccionTaller.text.toString().trim()
            val telefono = telefonoEncargado.text.toString().trim()
            val correo = correoEncargado.text.toString().trim()

            if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            } else {
                // Aquí podrías enviar los datos a un servidor o guardarlos localmente
                Toast.makeText(this, "Taller creado exitosamente", Toast.LENGTH_SHORT).show()
            }
        }

        fun generarCodigoTaller(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..8)
                .map { chars.random() }
                .joinToString("")
        }

        val codigo = generarCodigoTaller()
        val dialogView = layoutInflater.inflate(R.layout.dialog_taller_creado, null)

        val etCodigo = dialogView.findViewById<EditText>(R.id.etCodigoTaller)
        val btnCopiar = dialogView.findViewById<ImageView>(R.id.btnCopiarCodigo)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptar)

        etCodigo.setText(codigo)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCopiar.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código Taller", codigo)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        btnAceptar.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, VehiclesFragment::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()


    }
}
