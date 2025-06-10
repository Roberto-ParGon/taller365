package com.uv.taller365

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uv.taller365.database.FirebaseConnection
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class CreateWorkshop : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_workshop)

        // Generar el código una sola vez
        val codigo = generarCodigoTaller()

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
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Referencias al diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_taller_creado, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etCodigo = dialogView.findViewById<EditText>(R.id.etCodigoTaller)
        val btnCopiar = dialogView.findViewById<ImageView>(R.id.btnCopiarCodigo)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptar)

        etCodigo.setText(codigo)

        btnCopiar.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código Taller", codigo)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        btnAceptar.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Acción del botón Crear Taller
        btnCrearTaller.setOnClickListener {
            val nombre = nombreTaller.text.toString().trim()
            val direccion = direccionTaller.text.toString().trim()
            val telefono = telefonoEncargado.text.toString().trim()
            val correo = correoEncargado.text.toString().trim()

            if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            } else {
                val firebase = FirebaseConnection()
                firebase.writeNewWorkshop(
                    name = nombre,
                    address = direccion,
                    phone = telefono,
                    email = correo,
                    code = codigo
                ) { success ->
                    if (success) {
                        enviarCorreoCodigo(correo, nombre, codigo)
                        dialog.show()
                    } else {
                        Toast.makeText(this, "Error al registrar el taller", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun generarCodigoTaller(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    fun enviarCorreoCodigo(email: String, nombre: String, codigo: String) {
        val json = JSONObject().apply {
            put("service_id", "taller_365")
            put("template_id", "Taller_365")
            put("user_id", "SQTIFzEVAcPmKaOeT")
            put("template_params", JSONObject().apply {
                put("to_email", email)
                put("nombre", nombre)
                put("codigo_taller", codigo)
            })
        }

        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.emailjs.com/api/v1.0/email/send")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("EmailJS", "Error al enviar correo", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("EmailJS", "Correo enviado con éxito: $responseBody")
                } else {
                    Log.e("EmailJS", "Fallo al enviar correo: ${response.code} - $responseBody")
                }
            }
        })
    }

}
