package com.uv.taller365

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClientForm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_form)


        val brand = intent.getStringExtra("brand") ?: "Sin marca"
        val model = intent.getStringExtra("model") ?: "Sin modelo"
        val arrivalDate = intent.getStringExtra("arrival_date") ?: "Sin fecha"
        val clientName = intent.getStringExtra("client_name") ?: "Sin nombre"
        val clientPhone = intent.getStringExtra("client_phone") ?: "Sin teléfono"
        val status = intent.getStringExtra("status") ?: "Sin estado"

        // Mostrar los datos en los TextView
        findViewById<TextView>(R.id.etBrand).text = brand
        findViewById<TextView>(R.id.etModel).text = model
        findViewById<TextView>(R.id.etArrivalDate).text = arrivalDate
        findViewById<TextView>(R.id.etClientName).text = clientName
        findViewById<TextView>(R.id.etCellPhone).text = clientPhone
        findViewById<TextView>(R.id.textView3).text = status

        // Botón de regreso
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
