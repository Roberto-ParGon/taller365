package com.uv.taller365.workshopFiles

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import com.uv.taller365.LoginActivity
import com.uv.taller365.MainActivity
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CreateWorkshop : AppCompatActivity() {

    // Views
    private lateinit var nombreTaller: EditText
    private lateinit var direccionTaller: EditText
    private lateinit var telefonoEncargado: EditText
    private lateinit var correoEncargado: EditText
    private lateinit var loadingContainer: LinearLayout

    private lateinit var dialog: AlertDialog
    private lateinit var codigoGenerado: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_workshop)

        codigoGenerado = generarCodigoTaller()
        setupWindowInsets()
        initViews()
        setupDialog()
        setupActions()
    }

    // ---------------------- Configuración de interfaz ----------------------

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        nombreTaller = findViewById(R.id.etWorkshopName)
        direccionTaller = findViewById(R.id.etWorkshopAddress)
        telefonoEncargado = findViewById(R.id.etPhone)
        correoEncargado = findViewById(R.id.etEmail)
        loadingContainer = findViewById(R.id.loadingContainer)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_taller_creado, null)
        val etCodigo = dialogView.findViewById<EditText>(R.id.etCodigoTaller)
        val btnCopiar = dialogView.findViewById<ImageView>(R.id.btnCopiarCodigo)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptar)

        etCodigo.setText(codigoGenerado)

        btnCopiar.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código Taller", codigoGenerado)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        btnAceptar.setOnClickListener {
            dialog.dismiss()
            buscarYGuardarTaller(codigoGenerado)
        }

        dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btnCreateWorkshop).setOnClickListener {
            val nombre = nombreTaller.text.toString().trim()
            val direccion = direccionTaller.text.toString().trim()
            val telefono = telefonoEncargado.text.toString().trim()
            val correo = correoEncargado.text.toString().trim()

            if (nombre.isBlank() || direccion.isBlank() || telefono.isBlank() || correo.isBlank()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)
            val firebase = FirebaseConnection()
            firebase.writeNewWorkshop(
                name = nombre,
                address = direccion,
                phone = telefono,
                email = correo,
                code = codigoGenerado
            ) { success ->
                showLoading(false)
                if (success) {
                    enviarCorreoCodigo(correo, nombre, codigoGenerado)
                    dialog.show()
                } else {
                    Toast.makeText(this, "Error al registrar el taller", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ---------------------- Funcionalidades ----------------------

    private fun generarCodigoTaller(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    private fun buscarYGuardarTaller(codigo: String) {
        showLoading(true)
        val database = FirebaseDatabase.getInstance().getReference("workshops")

        database.orderByChild("info/code").equalTo(codigo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    showLoading(false)
                    if (snapshot.exists()) {
                        val workshopSnapshot = snapshot.children.first()
                        val workshopId = workshopSnapshot.key ?: return

                        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE).edit()
                        prefs.putString("WORKSHOP_ID", workshopId)
                        prefs.putString("USER_ROLE", "admin")
                        prefs.putString("USER_NAME", correoEncargado.text.toString())
                        prefs.apply()

                        val intent = Intent(this@CreateWorkshop, MainActivity::class.java)
                        intent.putExtra("WORKSHOP_ID", workshopId)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@CreateWorkshop, "No se encontró el taller recién creado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(this@CreateWorkshop, "Error al buscar el taller", Toast.LENGTH_SHORT).show()
                }
            })
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

        val mediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.emailjs.com/api/v1.0/email/send")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
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

    // ---------------------- Pantalla de cargando ----------------------

    private fun showLoading(isLoading: Boolean) {
        if (!::loadingContainer.isInitialized) return
        loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE

        window.apply {
            if (isLoading) {
                setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        window.decorView.systemUiVisibility = if (isLoading) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
