package com.uv.taller365.vehicleFiles

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.databinding.ActivityVehicleFormBinding
import com.uv.taller365.helpers.CustomDialogHelper
import com.uv.taller365.helpers.ImageHelper
import com.uv.taller365.helpers.uploadImageToSupabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VehicleForm : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleFormBinding
    private lateinit var database: FirebaseConnection
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var newSelectedImageUri: Uri? = null
    private var existingImageUrl: String? = null

    private var cameraImageUri: Uri? = null
    private var vehicleId: String? = null
    private var workshopId: String? = null
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseConnection()
        workshopId = intent.getStringExtra("WORKSHOP_ID")
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        setupWindow()
        setupToolbar()
        setupDatePicker()
        setupImagePicker()

        if (isEditMode) {
            setupEditMode()
        } else {
            setupCreateMode()
        }
    }

    private fun setupEditMode() {
        vehicleId = intent.getStringExtra("VEHICLE_ID")
        existingImageUrl = intent.getStringExtra("IMAGE_URL")
        binding.etBrand.setText(intent.getStringExtra("BRAND"))
        binding.etModel.setText(intent.getStringExtra("MODEL"))
        binding.etSerialNumber.setText(intent.getStringExtra("SERIAL_NUMBER"))
        binding.etArrivalDate.setText(intent.getStringExtra("ARRIVAL_DATE"))
        binding.etClientName.setText(intent.getStringExtra("CLIENT_NAME"))
        binding.etClientPhone.setText(intent.getStringExtra("CLIENT_PHONE"))

        if (!existingImageUrl.isNullOrBlank()) {
            displayImage(Uri.parse(existingImageUrl))
        }

        binding.btnNext.text = "Actualizar Vehículo"
        binding.btnNext.setIconResource(R.drawable.ic_save_24px)
        binding.btnNext.setOnClickListener {
            if (validateForm()) {
                updateVehicle()
            }
        }
    }

    private fun updateVehicle() {
        if (workshopId.isNullOrEmpty() || vehicleId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: IDs no disponibles para actualizar.", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, "Actualizando...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val finalImageUrl = if (newSelectedImageUri != null) {
                uploadImageToSupabase(newSelectedImageUri!!, this@VehicleForm)
            } else {
                existingImageUrl
            }

            val vehicle = Vehicle(
                id = vehicleId,
                brand = binding.etBrand.text.toString(),
                model = binding.etModel.text.toString(),
                name = "${binding.etBrand.text} ${binding.etModel.text}",
                serialNumber = binding.etSerialNumber.text.toString(),
                arrivalDate = binding.etArrivalDate.text.toString(),
                clientName = binding.etClientName.text.toString(),
                clientPhone = binding.etClientPhone.text.toString(),
                status = intent.getStringExtra("STATUS") ?: "En registro",
                imageUrl = finalImageUrl
            )

            database.updateVehicle(workshopId!!, vehicle) { success ->
                if (success) {
                    Toast.makeText(this@VehicleForm, "Vehículo actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@VehicleForm, "Error al actualizar el vehículo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun displayImage(uri: Uri) {
        binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
        binding.imageUploadContainer.requestLayout()
        Glide.with(this).load(uri).placeholder(R.drawable.ic_upload_24px).error(R.drawable.ic_warning_24px).centerCrop().into(binding.imageContainerImage)
        binding.placeholderImage.visibility = View.GONE
        binding.imageContainerImage.visibility = View.VISIBLE
    }


    private fun handleNewImageSelection(uri: Uri) {
        newSelectedImageUri = uri
        displayImage(uri)
    }

    private fun setupCreateMode() {
        binding.btnNext.text = "Siguiente"
        binding.btnNext.setIconResource(R.drawable.ic_arrow_forward_24px)
        binding.btnNext.setOnClickListener {
            if (validateForm()) {
                navigateToRepairParts()
            }
        }
    }

    private fun navigateToRepairParts() {
        val vehicle = Vehicle(
            brand = binding.etBrand.text.toString(),
            model = binding.etModel.text.toString(),
            name = "${binding.etBrand.text} ${binding.etModel.text}",
            serialNumber = binding.etSerialNumber.text.toString(),
            arrivalDate = binding.etArrivalDate.text.toString(),
            clientName = binding.etClientName.text.toString(),
            clientPhone = binding.etClientPhone.text.toString(),
            status = "En registro"
        )
        val intent = Intent(this, RepairParts::class.java).apply {
            putExtra("WORKSHOP_ID", workshopId)
            putExtra("VEHICLE_DATA", vehicle)
            putExtra("IMAGE_URI_STRING", newSelectedImageUri?.toString())
        }
        startActivity(intent)
    }

    private fun setupWindow() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun setupToolbar() {
        binding.tvFormTittle.text = if (isEditMode) "Editar Vehículo" else "Registro de Vehículo"
        binding.btnBack.setOnClickListener { finish() }
    }
    private fun validateForm(): Boolean {
        if (binding.etBrand.text.isNullOrEmpty()) {
            binding.etBrand.error = "Ingrese la marca del vehículo"
            return false
        }
        if (binding.etModel.text.isNullOrEmpty()) {
            binding.etModel.error = "Ingrese el modelo"
            return false
        }
        if (binding.etClientName.text.isNullOrEmpty()) {
            binding.etClientName.error = "Ingrese el nombre del cliente"
            return false
        }
        return true
    }
    private fun setupDatePicker() {
        binding.etArrivalDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, day) }
                binding.etArrivalDate.setText(dateFormat.format(selectedDate.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }
    private fun setupImagePicker() {
        binding.imageUploadContainer.setOnClickListener {
            CustomDialogHelper.showImagePickerDialog(this,
                createUri = { ImageHelper.createImageUri(this) },
                onCameraSelected = { uri -> cameraImageUri = uri; takePictureLauncher.launch(uri) },
                onGallerySelected = { pickImageLauncher.launch("image/*") }
            )
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { handleNewImageSelection(it) } }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) cameraImageUri?.let { handleNewImageSelection(it) } }
}