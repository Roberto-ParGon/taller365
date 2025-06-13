package com.uv.taller365.client

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.uv.taller365.R
import java.net.URLEncoder

class ClientForm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_form)

        val brand = intent.getStringExtra("brand") ?: "N/A"
        val model = intent.getStringExtra("model") ?: "N/A"
        val arrivalDate = intent.getStringExtra("arrival_date") ?: "Sin fecha"
        val clientName = intent.getStringExtra("client_name") ?: "Sin nombre"
        val clientPhone = intent.getStringExtra("client_phone") ?: ""
        val status = intent.getStringExtra("status") ?: "Sin estado"
        val imageUrl = intent.getStringExtra("image_url")

        findViewById<TextView>(R.id.etBrand).text = brand
        findViewById<TextView>(R.id.etModel).text = model
        findViewById<TextView>(R.id.etArrivalDate).text = arrivalDate
        findViewById<TextView>(R.id.etClientName).text = clientName
        findViewById<TextView>(R.id.etCellPhone).text = clientPhone
        findViewById<TextView>(R.id.textView3).text = status

        val ivVehicleImage = findViewById<ImageView>(R.id.ivVehicleImage)
        val placeholderImage = findViewById<ImageView>(R.id.placeholderImage)
        if (!imageUrl.isNullOrEmpty()) {
            placeholderImage.visibility = View.GONE
            ivVehicleImage.visibility = View.VISIBLE
            Glide.with(this).load(imageUrl).centerCrop().into(ivVehicleImage)
        } else {
            placeholderImage.visibility = View.VISIBLE
            ivVehicleImage.visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnNotifyWhatsApp).setOnClickListener {
            if (clientPhone.isNotEmpty()) {
                enviarMensajeEstadoWhatsApp(brand, model, clientPhone, status)
            } else {
                Toast.makeText(this, "Este cliente no tiene un número de teléfono registrado.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enviarMensajeEstadoWhatsApp(marca: String, modelo: String, telefono: String, estado: String) {
        val mensaje = "Hola, te informamos que el estado actual de tu vehículo '$marca $modelo' es: *$estado*."

        val numeroLimpio = telefono.replace(Regex("[\\s-]"), "")
        val numeroInternacional = if (numeroLimpio.startsWith("+")) numeroLimpio else "+52$numeroLimpio"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$numeroInternacional&text=${URLEncoder.encode(mensaje, "UTF-8")}")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp no está instalado en este dispositivo.", Toast.LENGTH_SHORT).show()
        }
    }
}