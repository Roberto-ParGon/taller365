package com.uv.taller365

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.uv.taller365.databinding.ActivityRepairFormBinding
import com.uv.taller365.helpers.ImageHelper
import java.io.ByteArrayOutputStream

class RepairForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepairFormBinding
    private lateinit var database: FirebaseConnection
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var repairId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRepairFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseConnection()
        setupWindowInsets()
        setupToolbar()
        setupSpinner()
        setupForm()
        setupButton()

        selectedImageUri = savedInstanceState?.getString("selectedImageUri")?.let { Uri.parse(it) }
        selectedImageUri?.let { uri ->
            val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
            binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
            binding.imageUploadContainer.requestLayout()
            ImageHelper.loadImageFromUri(this, uri, binding.imageContainerImage, binding.placeholderImage, binding.imageUploadContainer, radius)
        }

        val imageBase64 = selectedImageUri?.let { ImageHelper.uriToBase64(contentResolver, it) }

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
            binding.spinnerTipo.setSelection(getTipoIndex(intent.getStringExtra("tipo") ?: ""))
            binding.editNombre.setText(intent.getStringExtra("nombre"))
            binding.editMarca.setText(intent.getStringExtra("marca"))
            binding.editModelo.setText(intent.getStringExtra("modelo"))
            binding.editCantidad.setText(intent.getStringExtra("cantidad"))

            val imageUriString = intent.getStringExtra("image_uri")
            val imageRes = intent.getIntExtra("image_res", 0)

            if (!imageUriString.isNullOrBlank()) {
                loadImageFromBase64(imageUriString)
            } else if (imageRes != 0) {
                loadImageFromResource(imageRes)
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
            } else editNombre.error = null

            if (editMarca.text.isNullOrBlank()) {
                editMarca.error = "Ingrese la marca"
                editMarca.requestFocus()
                return false
            } else editMarca.error = null

            if (editModelo.text.isNullOrBlank()) {
                editModelo.error = "Ingrese el modelo"
                editModelo.requestFocus()
                return false
            } else editModelo.error = null

            val cantidadStr = editCantidad.text.toString().trim()
            val cantidad = cantidadStr.toIntOrNull()
            if (cantidadStr.isEmpty() || cantidad == null || cantidad <= 0) {
                editCantidad.error = "Ingrese una cantidad válida mayor que cero"
                editCantidad.requestFocus()
                return false
            } else editCantidad.error = null
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {

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

    override fun onBackPressed() {
        if (binding.loadingContainer.visibility == View.VISIBLE) {
            return
        }
        super.onBackPressed()
    }

    private fun saveNewRepair() {
        val imageBase64 = selectedImageUri?.let { uriToBase64(it) }

        showLoading(true)

        database.writeNewRepair(
            binding.spinnerTipo.selectedItem.toString(),
            binding.editNombre.text.toString(),
            binding.editMarca.text.toString(),
            binding.editModelo.text.toString(),
            binding.editCantidad.text.toString(),
            imageBase64
        ) { success ->
            showLoading(false)

            Toast.makeText(this,
                if (success) "Refacción guardada exitosamente" else "Error al guardar la refacción",
                Toast.LENGTH_SHORT
            ).show()

            if (success) finish()
        }
    }

    private fun updateRepair() {
        val id = repairId
        if (id == null) {
            Toast.makeText(this, "ID de refacción inválido", Toast.LENGTH_SHORT).show()
            return
        }
        val imageBase64 = selectedImageUri?.let { uriToBase64(it) } ?: intent.getStringExtra("image_uri")

        showLoading(true)

        database.updateRepair(
            id,
            binding.spinnerTipo.selectedItem.toString(),
            binding.editNombre.text.toString(),
            binding.editMarca.text.toString(),
            binding.editModelo.text.toString(),
            binding.editCantidad.text.toString(),
            imageBase64
        ) { success ->
            showLoading(false)

            Toast.makeText(this,
                if (success) "Refacción actualizada exitosamente" else "Error al actualizar la refacción",
                Toast.LENGTH_SHORT
            ).show()

            if (success) finish()
        }
    }

    private fun setupButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateForm()) {
                if (intent.getBooleanExtra("is_edit_mode", false)) updateRepair() else saveNewRepair()
            }
        }
    }

    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri
        val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
        binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
        binding.imageUploadContainer.requestLayout()

        ImageHelper.loadImageFromUri(this, uri, binding.imageContainerImage, binding.placeholderImage, binding.imageUploadContainer, radius)
    }

    private fun loadImageFromBase64(base64: String) {
        val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
        binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
        binding.imageUploadContainer.requestLayout()

        ImageHelper.loadImageFromBase64(this, base64, binding.imageContainerImage, binding.placeholderImage, binding.imageUploadContainer, radius)
    }

    private fun loadImageFromResource(resId: Int) {
        ImageHelper.loadImageFromResource(this, resId, binding.imageContainerImage, binding.placeholderImage, binding.imageUploadContainer)
        binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
        binding.imageUploadContainer.requestLayout()
    }

    private fun resetImagePlaceholder() {
        val defaultHeight = resources.getDimensionPixelSize(R.dimen.image_upload_default_height)
        ImageHelper.resetImage(binding.imageUploadContainer, binding.imageContainerImage, binding.placeholderImage, defaultHeight)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun uriToBase64(uri: Uri): String? = try {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        bitmapToBase64(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
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
    }



}
