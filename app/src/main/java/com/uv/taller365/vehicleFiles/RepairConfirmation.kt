package com.uv.taller365.vehicleFiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.uv.taller365.MainActivity
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.helpers.CustomDialogHelper
import com.uv.taller365.helpers.uploadImageToSupabase
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class RepairConfirmation : AppCompatActivity() {

    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var firebaseConnection: FirebaseConnection

    private lateinit var loadingContainer: LinearLayout
    private var isLoadingVisible: Boolean = false

    private var damagedParts: ArrayList<DamagedPart> = arrayListOf()
    private var vehicleData: Vehicle? = null
    private var workshopId: String? = null
    private var imageUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_confirmation)

        firebaseConnection = FirebaseConnection()

        loadingContainer = findViewById(R.id.loadingContainer)

        workshopId = intent.getStringExtra("WORKSHOP_ID")
        vehicleData = intent.getParcelableExtra("VEHICLE_DATA")
        imageUriString = intent.getStringExtra("IMAGE_URI_STRING")
        damagedParts = intent.getParcelableArrayListExtra("DAMAGED_PARTS") ?: arrayListOf()

        setupToolbar()
        setupRecyclerView()
        calculateAndDisplayTotal()
        setupConfirmButton()
    }

    private fun setupToolbar() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val rvSummary = findViewById<RecyclerView>(R.id.rvSummary)
        rvSummary.layoutManager = LinearLayoutManager(this)
        summaryAdapter = SummaryAdapter(damagedParts)
        rvSummary.adapter = summaryAdapter
    }

    private fun calculateAndDisplayTotal() {
        val totalCost = damagedParts.sumOf { it.cost }
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        findViewById<TextView>(R.id.tvTotalCost).text = format.format(totalCost)
    }

    private fun setupConfirmButton() {
        findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener {
            if (vehicleData == null || workshopId == null) {
                Toast.makeText(this, "Faltan datos para registrar el vehículo.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            showLoading(true)

            lifecycleScope.launch {
                val imageUrl = imageUriString?.let {
                    try {
                        uploadImageToSupabase(Uri.parse(it), this@RepairConfirmation)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                vehicleData!!.imageUrl = imageUrl

                firebaseConnection.writeNewVehicleWithDamages(workshopId!!, vehicleData!!, damagedParts) { success ->
                    showLoading(false)
                    if (success) {
                        CustomDialogHelper.showInfoDialog(
                            activity = this@RepairConfirmation,
                            title = "Éxito",
                            message = "Vehículo registrado exitosamente",
                            iconResId = R.drawable.ic_success_24px,
                            buttonText = "Aceptar"
                        ) {
                            val intent = Intent(this@RepairConfirmation, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("WORKSHOP_ID", workshopId)
                            startActivity(intent)
                        }
                    } else {
                        CustomDialogHelper.showInfoDialog(
                            activity = this@RepairConfirmation,
                            title = "Error",
                            message = "Error al guardar en la base de datos. Inténtalo de nuevo.",
                            iconResId = R.drawable.ic_error_24px,
                            buttonText = "Entendido"
                        )
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        isLoadingVisible = isLoading

        if (isLoading) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
        findViewById<MaterialButton>(R.id.btnConfirm).isEnabled = !isLoading

        window.decorView.systemUiVisibility = if (isLoading) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onBackPressed() {
        if (!isLoadingVisible) {
            super.onBackPressed()
        }
    }

    private class SummaryAdapter(private val parts: List<DamagedPart>) :
        RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

        class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.ivPartIcon)
            val partName: TextView = itemView.findViewById(R.id.tvPartName)
            val subPartName: TextView = itemView.findViewById(R.id.tvSubPartName)
            val cost: TextView = itemView.findViewById(R.id.tvPartCost)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_repair_summary, parent, false)
            return SummaryViewHolder(view)
        }

        override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
            val part = parts[position]
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

            holder.icon.setImageResource(part.imageResId)
            holder.partName.text = part.partName
            holder.subPartName.text = part.subPartName
            holder.cost.text = format.format(part.cost)
        }

        override fun getItemCount() = parts.size
    }
}