package com.uv.taller365

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uv.taller365.databinding.ActivityVehicleFormBinding
import java.text.SimpleDateFormat
import java.util.*

class VehicleForm : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleFormBinding
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVehicleFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupForm()
        setupDatePicker()
        setupButton()
    }

    private fun setupToolbar() {
        if (intent.getBooleanExtra("is_edit_mode", false)) {
            binding.tvFormTittle.text = "Editar vehículo"

        }else{
            binding.tvFormTittle.text = "Registro de vehículo"

        }

        binding.toolbar.title = if (intent.getBooleanExtra("is_edit_mode", false)) {
            "Editar Vehículo"
        } else {
            "Registro de Vehículos"
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupForm() {
        if (intent.getBooleanExtra("is_edit_mode", false)) {
            binding.etBrand.setText(intent.getStringExtra("brand"))
            binding.etModel.setText(intent.getStringExtra("model"))
            binding.etSerialNumber.setText(intent.getStringExtra("serial_number"))
            binding.etArrivalDate.setText(intent.getStringExtra("arrival_date"))
            binding.etClientName.setText(intent.getStringExtra("client_name"))
            binding.etClientPhone.setText(intent.getStringExtra("client_phone"))

            binding.btnNext.text = "Actualizar"
            binding.btnNext.setIconResource(R.drawable.save_24px)

        }
    }

    private fun setupDatePicker() {
        binding.etArrivalDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    binding.etArrivalDate.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButton() {
        binding.btnNext.setOnClickListener {
            if (validateForm()) {
                if (intent.getBooleanExtra("is_edit_mode", false)) {
                    updateVehicle()
                } else {
                    saveNewVehicle()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etBrand.text.isNullOrEmpty()) {
            binding.etBrand.error = "Ingrese la marca del vehículo"
            isValid = false
        }

        if (binding.etModel.text.isNullOrEmpty()) {
            binding.etModel.error = "Ingrese el modelo"
            isValid = false
        }

        if (binding.etSerialNumber.text.isNullOrEmpty()) {
            binding.etSerialNumber.error = "Ingrese el número de serie"
            isValid = false
        }

        if (binding.etArrivalDate.text.isNullOrEmpty()) {
            binding.etArrivalDate.error = "Seleccione la fecha de llegada"
            isValid = false
        }

        if (binding.etClientName.text.isNullOrEmpty()) {
            binding.etClientName.error = "Ingrese el nombre del cliente"
            isValid = false
        }

        if (binding.etClientPhone.text.isNullOrEmpty()) {
            binding.etClientPhone.error = "Ingrese el número de celular"
            isValid = false
        }

        return isValid
    }

    private fun saveNewVehicle() {
        //Toast.makeText(this, "Vehículo registrado exitosamente", Toast.LENGTH_SHORT).show()
        //finish()
        val intent = Intent(this, RepairParts::class.java)
        startActivity(intent)
    }

    private fun updateVehicle() {
        Toast.makeText(this, "Vehículo actualizado exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }
}