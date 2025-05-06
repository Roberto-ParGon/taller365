package com.uv.taller365

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ModifyWorkshop : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modify_workshop)

        // Ajustes para el layout principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los campos de texto
        val nombreTaller = findViewById<EditText>(R.id.etWorkshopName)
        val direccionTaller = findViewById<EditText>(R.id.etWorkshopAddress)
        val telefonoEncargado = findViewById<EditText>(R.id.etPhone)
        val correoEncargado = findViewById<EditText>(R.id.etEmail)

        // Botones
        val btnSaveWorkshop = findViewById<MaterialButton>(R.id.btnSaveWorkshop)
        val btnDeleteWorkshop = findViewById<MaterialButton>(R.id.btnDeleteWorkshop)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Acción del botón guardar
        btnSaveWorkshop.setOnClickListener {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            // Aquí podrías guardar en base de datos o enviar a servidor
        }

        // Acción del botón eliminar
        btnDeleteWorkshop.setOnClickListener {
            Toast.makeText(this, "Taller eliminado", Toast.LENGTH_SHORT).show()
            // Aquí podrías eliminar el taller de la base de datos
        }

        // Acción del botón volver
        btnBack.setOnClickListener {
            val intent = Intent(this, VehiclesFragment::class.java)
            startActivity(intent)
            finish() // Opcional: cierra esta pantalla
        }

        val btnDeleteUser = findViewById<ImageButton>(R.id.btnDeleteUser)

        btnDeleteWorkshop.setOnClickListener {
            showDeleteWorkshopDialog("Nombre del taller")
        }

        btnDeleteUser.setOnClickListener {
            showDeleteUserDialog("Juan Medina")
        }

    }
    fun showDeleteWorkshopDialog(workshopName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete_workshop, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<TextView>(R.id.tvMessage).text =
            "¿Estás seguro que deseas eliminar el taller \"$workshopName\"?"

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            // Lógica para eliminar taller
            Toast.makeText(this, "Taller eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showDeleteUserDialog(userName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete_user, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<TextView>(R.id.tvMessage).text =
            "¿Estás seguro que deseas eliminar al usuario \"$userName\"?"

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            // Lógica para eliminar usuario
            Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }


}
