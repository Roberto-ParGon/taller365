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
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import android.view.View
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.MultiTransformation
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
    return try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val filename = "repair_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()
        file.absolutePath // ruta absoluta del archivo guardado
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

class RepairForm : AppCompatActivity() {

    private lateinit var database: FirebaseConnection
    private var selectedImageUri: Uri? = null
    private lateinit var binding: ActivityRepairFormBinding

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)
            binding.imageContainerImage.visibility = View.VISIBLE

            val radiusInPixels = resources.getDimensionPixelSize(R.dimen.image_corner_radius)

            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.bitmapTransform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(radiusInPixels)
                    )
                ))
                .placeholder(R.drawable.ic_upload_24px)
                .error(R.drawable.ic_upload_24px)
                .into(binding.imageContainerImage)

            binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
            binding.imageUploadContainer.requestLayout()

            binding.imageUploadContainer.alpha = 0.7f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRepairFormBinding.inflate(layoutInflater)
        binding.imageUploadContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        setContentView(binding.root)
        // Inicializar Firebase //
        database = FirebaseConnection()
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

    /* Metodo para configurar la toolbar */
    private fun setupToolbar() {
        val isEditMode = intent.getBooleanExtra("is_edit_mode", false)

        binding.tvFormTittle.text = if (isEditMode) "Editar refacción" else "Ingresar refacción"
        binding.toolbar.title = binding.tvFormTittle.text

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    /* Metodo para configurar el spinner */
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

            // Obtener URI o recurso de imagen a cargar en edición
            val imageUriString = intent.getStringExtra("image_uri") // si tienes URI en String
            val imageRes = intent.getIntExtra("image_res", 0) // recurso alternativo

            if (!imageUriString.isNullOrBlank()) {
                // Mostrar imagen desde URI con Glide, igual que en la carga de imagen
                val radiusInPixels = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
                binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)
                binding.imageContainerImage.visibility = View.VISIBLE

                Glide.with(this)
                    .load(File(imageUriString))
                    .apply(RequestOptions.bitmapTransform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(radiusInPixels)
                        )
                    ))
                    .placeholder(R.drawable.ic_upload_24px)
                    .error(R.drawable.ic_upload_24px)
                    .into(binding.imageContainerImage)

                binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
                binding.imageUploadContainer.requestLayout()
                binding.imageUploadContainer.alpha = 0.7f

            } else if (imageRes != 0) {
                // Cargar imagen desde recurso, sin Glide, solo para respaldo
                binding.placeholderImage.setImageResource(R.drawable.ic_edit_24px)
                binding.imageContainerImage.visibility = View.VISIBLE
                binding.imageContainerImage.setImageResource(imageRes)
                binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_expanded_height)
                binding.imageUploadContainer.requestLayout()
                binding.imageUploadContainer.alpha = 0.7f

            } else {
                // No imagen, mostrar placeholder original
                binding.placeholderImage.setImageResource(R.drawable.ic_upload_24px)
                binding.imageContainerImage.visibility = View.GONE
                binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_default_height)
                binding.imageUploadContainer.requestLayout()
                binding.imageUploadContainer.alpha = 1f
            }

            binding.btnGuardar.text = "Actualizar"
            binding.btnGuardar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.save_24px, 0)
        } else {
            // Formulario nuevo sin imagen
            binding.placeholderImage.setImageResource(R.drawable.ic_upload_24px)
            binding.imageContainerImage.visibility = View.GONE
            binding.imageUploadContainer.layoutParams.height = resources.getDimensionPixelSize(R.dimen.image_upload_default_height)
            binding.imageUploadContainer.requestLayout()
            binding.imageUploadContainer.alpha = 1f
        }
    }

    /* Metodo para obtener el indice del tipo en el spinner */
    private fun getTipoIndex(tipo: String): Int {
        val tipos = listOf("Repuesto", "Accesorio", "Herramienta", "Otro")
        return tipos.indexOfFirst { it.equals(tipo, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    }

    /* Metodo para validar el formulario */
    private fun validateForm(): Boolean {
        // Validar Nombre
        val nombre = binding.editNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            binding.editNombre.error = "Ingrese el nombre"
            binding.editNombre.requestFocus()
            return false
        } else {
            binding.editNombre.error = null
        }

        // Validar Marca
        val marca = binding.editMarca.text.toString().trim()
        if (marca.isEmpty()) {
            binding.editMarca.error = "Ingrese la marca"
            binding.editMarca.requestFocus()
            return false
        } else {
            binding.editMarca.error = null
        }

        // Validar Modelo
        val modelo = binding.editModelo.text.toString().trim()
        if (modelo.isEmpty()) {
            binding.editModelo.error = "Ingrese el modelo"
            binding.editModelo.requestFocus()
            return false
        } else {
            binding.editModelo.error = null
        }

        // Validar Cantidad: no vacía, y número positivo
        val cantidadStr = binding.editCantidad.text.toString().trim()
        if (cantidadStr.isEmpty()) {
            binding.editCantidad.error = "Ingrese la cantidad"
            binding.editCantidad.requestFocus()
            return false
        }
        val cantidad = cantidadStr.toIntOrNull()
        if (cantidad == null || cantidad <= 0) {
            binding.editCantidad.error = "Ingrese una cantidad válida mayor que cero"
            binding.editCantidad.requestFocus()
            return false
        } else {
            binding.editCantidad.error = null
        }

        return true
    }

    /* Metodo para ingresar una herramienta */
    private fun saveNewRepair() {
        val imagePath = selectedImageUri?.let { uri ->
            saveImageToInternalStorage(this, uri)
        }

        database.writeNewRepair(
            binding.spinnerTipo.selectedItem.toString(),
            binding.editNombre.text.toString(),
            binding.editMarca.text.toString(),
            binding.editModelo.text.toString(),
            binding.editCantidad.text.toString(),
            imagePath
        ) { success ->
            if (success) {
                Toast.makeText(this, "Refacción guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar la refacción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* Metodo para actualizar informacion de una herramienta */
    private fun updateRepair() {
        Toast.makeText(this, "Refacción actualizada exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    /* Metodo para configurar el boton de guardar */
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