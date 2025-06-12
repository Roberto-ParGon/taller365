package com.uv.taller365.vehicleFiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.uv.taller365.R

class RepairParts : AppCompatActivity() {

    private val selectedParts = mutableListOf<DamagedPart>()
    private var vehicleData: Vehicle? = null
    private var workshopId: String? = null
    private var imageUriString: String? = null

    private val partOptions = mapOf(
        "Puertas" to listOf(
            "Puerta Delantera Izquierda" to 1500.0, "Puerta Delantera Derecha" to 1500.0,
            "Puerta Trasera Izquierda" to 1200.0, "Puerta Trasera Derecha" to 1200.0
        ),
        "Cofre" to listOf(
            "Reparación de abolladura" to 800.0, "Reemplazo completo" to 4500.0,
            "Ajuste de bisagras" to 300.0
        ),
        "Cajuela" to listOf(
            "Reparación de abolladura" to 750.0, "Reemplazo de chapa" to 900.0,
            "Reemplazo completo" to 4000.0
        ),
        "Toldo" to listOf(
            "Reparación de abolladura" to 1100.0, "Pintura general" to 2500.0,
            "Reemplazo" to 7000.0
        ),
        "Facias" to listOf(
            "Facia Delantera" to 1300.0, "Facia Trasera" to 1300.0
        ),
        "Salpicaderas" to listOf(
            "Salpicadera Delantera Izquierda" to 600.0, "Salpicadera Delantera Derecha" to 600.0,
            "Salpicadera Trasera Izquierda" to 550.0, "Salpicadera Trasera Derecha" to 550.0
        ),
        "Retrovisores" to listOf(
            "Retrovisor Izquierdo" to 850.0, "Retrovisor Derecho" to 850.0
        ),
        "Marco de Cristales" to listOf(
            "Parabrisas" to 2200.0, "Medallón (cristal trasero)" to 1800.0,
            "Cristal puerta Del. Izq." to 700.0, "Cristal puerta Del. Der." to 700.0,
            "Cristal puerta Tra. Izq." to 650.0, "Cristal puerta Tra. Der." to 650.0
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_parts)

        workshopId = intent.getStringExtra("WORKSHOP_ID")
        vehicleData = intent.getParcelableExtra("VEHICLE_DATA")
        imageUriString = intent.getStringExtra("IMAGE_URI_STRING")

        if (vehicleData == null || workshopId == null) {
            Toast.makeText(this, "Error: No se recibieron los datos del vehículo.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupListeners()
        setupNavigation()
    }

    private fun setupListeners() {
        findViewById<CardView>(R.id.cardPuertas).setOnClickListener {
            showMultiChoiceDialog("Puertas", R.drawable.car_door, findViewById(R.id.tvPuertasSelection))
        }
        findViewById<CardView>(R.id.cardCofre).setOnClickListener {
            showMultiChoiceDialog("Cofre", R.drawable.car_bonnet, findViewById(R.id.tvCofreSelection))
        }
        findViewById<CardView>(R.id.cardCajuela).setOnClickListener {
            showMultiChoiceDialog("Cajuela", R.drawable.car_trunk, findViewById(R.id.tvCajuelaSelection))
        }
        findViewById<CardView>(R.id.cardToldo).setOnClickListener {
            showMultiChoiceDialog("Toldo", R.drawable.car_toldo, findViewById(R.id.tvToldoSelection))
        }
        findViewById<CardView>(R.id.cardFacias).setOnClickListener {
            showMultiChoiceDialog("Facias", R.drawable.car_bumper, findViewById(R.id.tvFaciasSelection))
        }
        findViewById<CardView>(R.id.cardSalpicaderas).setOnClickListener {
            showMultiChoiceDialog("Salpicaderas", R.drawable.car_fender, findViewById(R.id.tvSalpicaderasSelection))
        }
        findViewById<CardView>(R.id.cardRetrovisores).setOnClickListener {
            showMultiChoiceDialog("Retrovisores", R.drawable.car_rearview_mirror, findViewById(R.id.tvRetrovisoresSelection))
        }
        findViewById<CardView>(R.id.cardCristales).setOnClickListener {
            showMultiChoiceDialog("Marco de Cristales", R.drawable.car_wiper, findViewById(R.id.tvCristalesSelection))
        }
    }

    private fun showMultiChoiceDialog(partName: String, iconResId: Int, selectionTextView: TextView) {
        val subPartsData = partOptions[partName] ?: return
        val subPartNames = subPartsData.map { "${it.first} ($${it.second})" }.toTypedArray()

        val checkedItems = BooleanArray(subPartNames.size) { false }

        val existingSelectionsForPart = selectedParts.filter { it.partName == partName }
        existingSelectionsForPart.forEach { existingPart ->
            val index = subPartsData.indexOfFirst { it.first == existingPart.subPartName }
            if (index != -1) {
                checkedItems[index] = true
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Seleccionar detalle de: $partName")
            .setMultiChoiceItems(subPartNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Aceptar") { _, _ ->
                selectedParts.removeAll { it.partName == partName }
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        val selectedSubPartData = subPartsData[i]
                        selectedParts.add(
                            DamagedPart(
                                partName = partName,
                                subPartName = selectedSubPartData.first,
                                cost = selectedSubPartData.second,
                                imageResId = iconResId
                            )
                        )
                    }
                }
                updateSelectionText(selectionTextView, partName)
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Limpiar") { _, _ ->
                selectedParts.removeAll { it.partName == partName }
                updateSelectionText(selectionTextView, partName)
            }
            .show()
    }

    private fun updateSelectionText(textView: TextView, partName: String) {
        val count = selectedParts.count { it.partName == partName }
        if (count > 0) {
            textView.text = "$count seleccionado(s)"
            textView.setTextColor(ContextCompat.getColor(this, R.color.blue))
        } else {
            textView.text = "No seleccionado"
            textView.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
        }
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            if (selectedParts.isEmpty()) {
                Toast.makeText(this, "Debes seleccionar al menos una parte dañada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, RepairConfirmation::class.java).apply {
                putExtra("WORKSHOP_ID", workshopId)
                putExtra("VEHICLE_DATA", vehicleData)
                putExtra("IMAGE_URI_STRING", imageUriString)
                putParcelableArrayListExtra("DAMAGED_PARTS", ArrayList(selectedParts))
            }
            startActivity(intent)
        }
    }
}