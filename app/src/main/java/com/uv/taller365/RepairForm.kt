package com.uv.taller365

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.uv.taller365.databinding.ActivityRepairFormBinding
import com.uv.taller365.helpers.ImageHelper
import com.uv.taller365.helpers.uploadImageToSupabase
import kotlinx.coroutines.launch

class RepairForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepairFormBinding
    private lateinit var database: FirebaseConnection
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var repairId: String? = null
    private var isLoadingVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRepairFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLoadingVisible = savedInstanceState?.getBoolean("isLoadingVisible") ?: false
        showLoading(isLoadingVisible)

        database = FirebaseConnection()

        setupWindowInsets()
        setupToolbar()
        setupSpinner()
        setupForm()
        setupButton()

        selectedImageUri = savedInstanceState?.getString("selectedImageUri")?.let { Uri.parse(it) }
        selectedImageUri?.let { uri -> handleImageSelected(uri) }

        binding.imageUploadContainer.setOnClickListener {
            showCustomOptionDialog()
        }
    }

    private fun enableEdgeToEdge() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.Offwhite)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        val isEditMode = intent.getBooleanExtra("is_edit_mode", false)
        binding.tvFormTittle.text = if (isEditMode) "Editar refacción" else "Ingresar refacción"
        binding.toolbar.title = binding.tvFormTittle.text
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupSpinner() {
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        ArrayAdapter(this, R.layout.spinner_item, tipos).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerTipo.adapter = adapter
        }
    }

    private fun setupForm() {
        val isEditMode = intent.getBooleanExtra("is_edit_mode", false)
        if (isEditMode) {
            repairId = intent.getStringExtra("repair_id")
            binding.imageText.setText("Editar imagen:")
            binding.spinnerTipo.setSelection(getTipoIndex(intent.getStringExtra("tipo") ?: ""))
            binding.editNombre.setText(intent.getStringExtra("nombre"))
            binding.editMarca.setText(intent.getStringExtra("marca"))
            binding.editModelo.setText(intent.getStringExtra("modelo"))
            binding.editCantidad.setText(intent.getStringExtra("cantidad"))

            val imageUrl = intent.getStringExtra("image_uri")
            if (!imageUrl.isNullOrBlank()) {
                val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
                binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
                binding.imageUploadContainer.requestLayout()

                ImageHelper.loadImageFromUri(
                    context = this,
                    uri = Uri.parse(imageUrl),
                    imageView = binding.imageContainerImage,
                    placeholder = binding.placeholderImage,
                    container = binding.imageUploadContainer,
                    radius = radius
                )
            } else {
                resetImagePlaceholder()
            }

            binding.btnGuardar.text = "Actualizar"
            binding.btnGuardar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.save_24px, 0)
        } else {
            resetImagePlaceholder()
        }
    }

    private fun getTipoIndex(tipo: String): Int {
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        return tipos.indexOfFirst { it.equals(tipo, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    }

    private fun validateForm(): Boolean {
        with(binding) {
            if (editNombre.text.isNullOrBlank()) {
                editNombre.error = "Ingrese el nombre"
                editNombre.requestFocus()
                return false
            }

            if (editMarca.text.isNullOrBlank()) {
                editMarca.error = "Ingrese la marca"
                editMarca.requestFocus()
                return false
            }

            if (editModelo.text.isNullOrBlank()) {
                editModelo.error = "Ingrese el modelo"
                editModelo.requestFocus()
                return false
            }

            val cantidad = editCantidad.text.toString().trim().toIntOrNull()
            if (cantidad == null || cantidad <= 0) {
                editCantidad.error = "Ingrese una cantidad válida mayor que cero"
                editCantidad.requestFocus()
                return false
            }
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        isLoadingVisible = isLoading

        if (isLoading) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnGuardar.isEnabled = !isLoading

        window.decorView.systemUiVisibility = if (isLoading) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun setupButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateForm()) {
                if (intent.getBooleanExtra("is_edit_mode", false)) updateRepair() else saveNewRepair()
            }
        }
    }

    private fun saveNewRepair() {
        showLoading(true)

        lifecycleScope.launch {
            val imageUrl = try {
                selectedImageUri?.let { uploadImageToSupabase(it, this@RepairForm) }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (imageUrl == null) {
                showLoading(false)
                Toast.makeText(this@RepairForm, "Error al subir imagen", Toast.LENGTH_LONG).show()
                return@launch
            }

            database.writeNewRepair(
                binding.spinnerTipo.selectedItem.toString(),
                binding.editNombre.text.toString(),
                binding.editMarca.text.toString(),
                binding.editModelo.text.toString(),
                binding.editCantidad.text.toString(),
                imageUrl
            ) { success ->
                showLoading(false)
                Toast.makeText(
                    this@RepairForm,
                    if (success) "Refacción guardada exitosamente" else "Error al guardar la refacción",
                    Toast.LENGTH_SHORT
                ).show()
                if (success) finish()
            }
        }
    }

    private fun updateRepair() {
        val id = repairId ?: run {
            Toast.makeText(this, "ID de refacción inválido", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            val imageUrl = selectedImageUri?.let {
                uploadImageToSupabase(it, this@RepairForm)
            } ?: intent.getStringExtra("image_uri")

            database.updateRepair(
                id,
                binding.spinnerTipo.selectedItem.toString(),
                binding.editNombre.text.toString(),
                binding.editMarca.text.toString(),
                binding.editModelo.text.toString(),
                binding.editCantidad.text.toString(),
                imageUrl
            ) { success ->
                showLoading(false)
                Toast.makeText(
                    this@RepairForm,
                    if (success) "Refacción actualizada exitosamente" else "Error al actualizar la refacción",
                    Toast.LENGTH_SHORT
                ).show()
                if (success) finish()
            }
        }
    }

    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri
        val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
        binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
        binding.imageUploadContainer.requestLayout()
        binding.imageText.setText("Editar imagen:")

        ImageHelper.loadImageFromUri(this, uri, binding.imageContainerImage, binding.placeholderImage, binding.imageUploadContainer, radius)
    }

    private fun resetImagePlaceholder() {
        val defaultHeight = resources.getDimensionPixelSize(R.dimen.image_upload_default_height)
        ImageHelper.resetImage(binding.imageUploadContainer, binding.imageContainerImage, binding.placeholderImage, defaultHeight)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            handleImageSelected(cameraImageUri!!)
        }
    }

    private fun showCustomOptionDialog() {
        ImageHelper.showImagePickerDialog(
            activity = this,
            createUri = { ImageHelper.createImageUri(this) },
            onCameraSelected = { uri ->
                cameraImageUri = uri
                takePictureLauncher.launch(uri)
            },
            onGallerySelected = {
                pickImageLauncher.launch("image/*")
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let {
            outState.putString("selectedImageUri", it.toString())
        }
        outState.putBoolean("isLoadingVisible", isLoadingVisible) // 👈 Añadir esto
    }

    override fun onBackPressed() {
        if (binding.loadingContainer.visibility != View.VISIBLE) {
            super.onBackPressed()
        }
    }
}
