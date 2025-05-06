package com.uv.taller365

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uv.taller365.databinding.ActivityRepairFormBinding

class RepairForm : AppCompatActivity() {
    private lateinit var binding: ActivityRepairFormBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRepairFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val spinnerTipo = findViewById<Spinner>(R.id.spinnerTipo)
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            tipos
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTipo.adapter = adapter

        setupToolbar()
        setupSpinner()
        setupForm()
        setupButton()
    }
    private fun setupToolbar() {
        val isEditMode = intent.getBooleanExtra("is_edit_mode", false)

        binding.tvFormTittle.text = if (isEditMode) "Editar refacción" else "Ingresar refacción"
        binding.toolbar.title = binding.tvFormTittle.text

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, // ítem personalizado
            tipos
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerTipo.adapter = adapter
    }

    private fun setupForm() {
        if (intent.getBooleanExtra("is_edit_mode", false)) {
            binding.spinnerTipo.setSelection(getTipoIndex(intent.getStringExtra("tipo") ?: ""))
            binding.editNombre.setText(intent.getStringExtra("nombre"))
            binding.editMarca.setText(intent.getStringExtra("marca"))
            binding.editModelo.setText(intent.getStringExtra("modelo"))
            binding.editCantidad.setText(intent.getStringExtra("cantidad"))

            // Reemplazar el fondo del contenedor por la imagen del item
            val imageRes = intent.getIntExtra("image_res", R.drawable.ic_upload_24px)  // Agrega una imagen predeterminada si no existe
            binding.imageUploadContainer.setBackgroundResource(imageRes)

            // Asegúrate de aplicar el filtro de sombra (darkened effect)
            binding.imageUploadContainer.alpha = 0.5f  // Ajusta el nivel de sombra

            // Cambiar el icono para edición si es modo edición
            binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)

            binding.btnGuardar.text = "Actualizar"
            binding.btnGuardar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.save_24px, 0)
        } else {
            // Mantener el icono original cuando no es modo edición
            binding.placeholderImage.setImageResource(R.drawable.ic_upload_24px)
        }
    }


    private fun getTipoIndex(tipo: String): Int {
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        return tipos.indexOfFirst { it.equals(tipo, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.editNombre.text.isNullOrBlank()) {
            binding.editNombre.error = "Ingrese el nombre"
            isValid = false
        }

        if (binding.editMarca.text.isNullOrBlank()) {
            binding.editMarca.error = "Ingrese la marca"
            isValid = false
        }

        if (binding.editModelo.text.isNullOrBlank()) {
            binding.editModelo.error = "Ingrese el modelo"
            isValid = false
        }

        if (binding.editCantidad.text.isNullOrBlank()) {
            binding.editCantidad.error = "Ingrese la cantidad"
            isValid = false
        }

        return isValid
    }

    private fun saveNewRepair() {
        Toast.makeText(this, "Refacción registrada exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateRepair() {
        Toast.makeText(this, "Refacción actualizada exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateForm()) {
                if (intent.getBooleanExtra("is_edit_mode", false)) {
                    updateRepair()
                } else {
                    saveNewRepair()
                }
            }
        }
    }

}