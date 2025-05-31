package com.uv.taller365

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.uv.taller365.databinding.ActivityRepairFormBinding
import java.io.ByteArrayOutputStream

class RepairForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepairFormBinding
    private lateinit var database: FirebaseConnection
    private var selectedImageUri: Uri? = null
    private var repairId: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleImageSelected(it) }
    }

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

        binding.imageUploadContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun enableEdgeToEdge() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
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
        binding.btnGuardar.isEnabled = !isLoading
        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE

        window.setFlags(
            if (isLoading) WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE else 0,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
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
        binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)
        binding.imageContainerImage.visibility = View.VISIBLE

        val radiusInPixels = resources.getDimensionPixelSize(R.dimen.image_corner_radius)

        Glide.with(this)
            .load(uri)
            .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(radiusInPixels))))
            .placeholder(R.drawable.ic_upload_24px)
            .error(R.drawable.ic_upload_24px)
            .into(binding.imageContainerImage)

        binding.imageUploadContainer.apply {
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
            requestLayout()
            alpha = 0.7f
        }
    }

    private fun loadImageFromBase64(base64String: String) {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        val radiusInPixels = resources.getDimensionPixelSize(R.dimen.image_corner_radius)

        binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)
        binding.imageContainerImage.visibility = View.VISIBLE

        Glide.with(this)
            .load(bitmap)
            .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(radiusInPixels))))
            .placeholder(R.drawable.ic_upload_24px)
            .error(R.drawable.ic_upload_24px)
            .into(binding.imageContainerImage)

        binding.imageUploadContainer.apply {
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
            requestLayout()
            alpha = 0.7f
        }
    }

    private fun loadImageFromResource(resId: Int) {
        binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)
        binding.imageContainerImage.visibility = View.VISIBLE
        binding.imageContainerImage.setImageResource(resId)
        binding.imageUploadContainer.apply {
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
            requestLayout()
            alpha = 0.7f
        }
    }

    private fun resetImagePlaceholder() {
        binding.placeholderImage.setImageResource(R.drawable.ic_upload_24px)
        binding.imageContainerImage.visibility = View.GONE
        binding.imageUploadContainer.apply {
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_default_height)
            requestLayout()
            alpha = 1f
        }
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
}
