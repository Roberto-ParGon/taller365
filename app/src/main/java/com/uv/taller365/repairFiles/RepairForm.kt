package com.uv.taller365.repairFiles

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.databinding.ActivityRepairFormBinding
import com.uv.taller365.helpers.*
import kotlinx.coroutines.launch

class RepairForm : AppCompatActivity() {

    // Binding y conexión a Firebase
    private lateinit var binding: ActivityRepairFormBinding
    private lateinit var database: FirebaseConnection

    // Variables de estado
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var repairId: String? = null
    private var isLoadingVisible: Boolean = false

    // ---------------------- Ciclo de vida ----------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRepairFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restaurar estado si aplica
        isLoadingVisible = savedInstanceState?.getBoolean("isLoadingVisible") ?: false
        showLoading(isLoadingVisible)

        database = FirebaseConnection()

        // Configuraciones de UI
        setupWindowInsets()
        setupToolbar()
        setupSpinner()
        setupForm()
        setupButton()

        // Restaurar imagen seleccionada si aplica
        selectedImageUri = savedInstanceState?.getString("selectedImageUri")?.let { Uri.parse(it) }
        selectedImageUri?.let { handleImageSelected(it) }

        // Selección de imagen
        binding.imageUploadContainer.setOnClickListener {
            CustomDialogHelper.showImagePickerDialog(
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
    }

    // ---------------------- Configuración de la interfaz ----------------------

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

    private fun setupButton() {
        binding.btnGuardar.setOnClickListener {
            if (validateForm()) {
                if (intent.getBooleanExtra("is_edit_mode", false)) updateRepair()
                else saveNewRepair()
            }
        }
    }

    // ---------------------- Manejo del formulario ----------------------

    private fun setupForm() {
        val isEditMode = intent.getBooleanExtra("is_edit_mode", false)
        if (isEditMode) {
            repairId = intent.getStringExtra("repair_id")

            // Rellenar datos
            binding.imageText.setText("Editar imagen:")
            binding.spinnerTipo.setSelection(getTipoIndex(intent.getStringExtra("tipo") ?: ""))
            binding.editNombre.setText(intent.getStringExtra("nombre"))
            binding.editMarca.setText(intent.getStringExtra("marca"))
            binding.editModelo.setText(intent.getStringExtra("modelo"))
            binding.editCantidad.setText(intent.getStringExtra("cantidad"))

            // Mostrar imagen si hay
            val imageUrl = intent.getStringExtra("image_uri")
            if (!imageUrl.isNullOrBlank()) {
                val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
                binding.imageUploadContainer.layoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
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
            binding.btnGuardar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_save_24px, 0)
        } else {
            resetImagePlaceholder()
        }
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

    // ---------------------- Guardar refacción nueva o existente ----------------------

    private fun saveNewRepair() {
        showLoading(true)

        val workshopCode = intent.getStringExtra("workshop_code") ?: run {
            showLoading(false)
            CustomDialogHelper.showInfoDialog(
                activity = this@RepairForm,
                title = "Error",
                message = "Código del taller no proporcionado",
                iconResId = R.drawable.ic_error_24px,
                buttonText = "Entendido"
            ) {
                finish()
            }
            return
        }

        lifecycleScope.launch {
            val imageUrl = try {
                selectedImageUri?.let { uploadImageToSupabase(it, this@RepairForm) }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (imageUrl == null) {
                showLoading(false)
                CustomDialogHelper.showInfoDialog(
                    activity = this@RepairForm,
                    title = "Error",
                    message = "Ocurrió un error al subir la imagen. Intenta nuevamente.",
                    iconResId = R.drawable.ic_error_24px,
                    buttonText = "Entendido"
                ) {
                }
                return@launch
            }

            database.writeNewRepair(
                workshopCode,
                binding.spinnerTipo.selectedItem.toString(),
                binding.editNombre.text.toString(),
                binding.editMarca.text.toString(),
                binding.editModelo.text.toString(),
                binding.editCantidad.text.toString(),
                imageUrl
            ) { success ->
                showLoading(false)
                CustomDialogHelper.showInfoDialog(
                    activity = this@RepairForm,
                    title = if (success) "Éxito" else "Error",
                    message = if (success) "Refacción guardada exitosamente" else "Error al guardar la refacción",
                    iconResId = if (success) R.drawable.ic_success_24px else R.drawable.ic_error_24px,
                    buttonText = "Aceptar"
                ){
                    if (success) finish()
                }
            }
        }
    }

    private fun updateRepair() {
        val workshopCode = intent.getStringExtra("workshop_code") ?: run {
            showLoading(false)
            CustomDialogHelper.showInfoDialog(
                activity = this@RepairForm,
                title = "Error",
                message = "Código del taller no proporcionado",
                iconResId = R.drawable.ic_error_24px,
                buttonText = "Entendido"
            ) {
                finish()
            }
            return
        }

        val id = repairId ?: run {
            CustomDialogHelper.showInfoDialog(
                activity = this@RepairForm,
                title = "Error",
                message = "ID de refacción inválido",
                iconResId = R.drawable.ic_error_24px,
                buttonText = "Entendido"
            ) {
                finish()
            }
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            val imageUrl = selectedImageUri?.let {
                uploadImageToSupabase(it, this@RepairForm)
            } ?: intent.getStringExtra("image_uri")

            database.updateRepair(
                workshopCode,
                id,
                binding.spinnerTipo.selectedItem.toString(),
                binding.editNombre.text.toString(),
                binding.editMarca.text.toString(),
                binding.editModelo.text.toString(),
                binding.editCantidad.text.toString(),
                imageUrl
            ) { success ->
                showLoading(false)
                CustomDialogHelper.showInfoDialog(
                    activity = this@RepairForm,
                    title = if (success) "Éxito" else "Error",
                    message = if (success) "Refacción actualizada exitosamente" else "Error al actualizar la refacción",
                    iconResId = if (success) R.drawable.ic_success_24px else R.drawable.ic_error_24px,
                    buttonText = "Aceptar"
                ){
                    if (success) finish()
                }
            }
        }
    }

    // ---------------------- Imagen ----------------------

    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri
        val radius = resources.getDimensionPixelSize(R.dimen.image_corner_radius)

        binding.imageUploadContainer.layoutParams.height =
            resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
        binding.imageUploadContainer.requestLayout()
        binding.imageText.setText("Editar imagen:")

        ImageHelper.loadImageFromUri(
            this, uri,
            binding.imageContainerImage,
            binding.placeholderImage,
            binding.imageUploadContainer,
            radius
        )
    }

    private fun resetImagePlaceholder() {
        val defaultHeight = resources.getDimensionPixelSize(R.dimen.image_upload_default_height)
        ImageHelper.resetImage(
            binding.imageUploadContainer,
            binding.imageContainerImage,
            binding.placeholderImage,
            defaultHeight
        )
    }

    // ---------------------- Utilidades ----------------------

    private fun showLoading(isLoading: Boolean) {
        isLoadingVisible = isLoading

        if (isLoading) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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

    private fun getTipoIndex(tipo: String): Int {
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        return tipos.indexOfFirst { it.equals(tipo, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    }

    // ---------------------- Launchers de imagen ----------------------

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri != null) {
            handleImageSelected(cameraImageUri!!)
        }
    }

    // ---------------------- Guardado de estado ----------------------

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let { outState.putString("selectedImageUri", it.toString()) }
        outState.putBoolean("isLoadingVisible", isLoadingVisible)
    }

    override fun onBackPressed() {
        if (binding.loadingContainer.visibility != View.VISIBLE) {
            super.onBackPressed()
        }
    }
}