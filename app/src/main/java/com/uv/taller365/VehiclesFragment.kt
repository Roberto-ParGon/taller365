package com.uv.taller365

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class VehiclesFragment : Fragment() {

    data class Vehicle(
        val id: String = UUID.randomUUID().toString(),
        val imageRes: Int,
        val name: String,
        val brand: String,
        val model: String,
        val serialNumber: String,
        val arrivalDate: Date,
        val clientName: String,
        val clientPhone: String,
        val status: String,
        val statusColor: Int
    )

    private inner class VehicleAdapter(private val vehicles: List<Vehicle>) :
        RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

        inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivVehicleImage: ImageView = itemView.findViewById(R.id.ivVehicleImage)
            val tvVehicleName: TextView = itemView.findViewById(R.id.tvVehicleName)
            val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
            val tvSerialNumber: TextView = itemView.findViewById(R.id.tvSerialNumber)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.vehicle_card, parent, false)
            return VehicleViewHolder(view)
        }


        override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
            val vehicle = vehicles[position]

            holder.ivVehicleImage.setImageResource(vehicle.imageRes)
            holder.tvVehicleName.text = vehicle.name
            holder.tvBrand.text = "Marca: ${vehicle.brand}"
            holder.tvSerialNumber.text = "No. Serie: ${vehicle.serialNumber}"
            holder.tvStatus.text = vehicle.status
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, vehicle.statusColor))

            holder.ivEdit.setOnClickListener {
                openEditVehicleActivity(vehicle)
            }
        }

        override fun getItemCount() = vehicles.size
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_vehicles, container, false)
        view.setBackgroundColor(resources.getColor(R.color.white, null))

        view.findViewById<ImageButton>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }


        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val vehicles = listOf(
            Vehicle(
                imageRes = R.drawable.chevy_pop,
                name = "Chevy Pop",
                brand = "Chevrolet",
                model = "Pop",
                serialNumber = "FR08104",
                arrivalDate = dateFormat.parse("15/05/2023"),
                clientName = "Juanito",
                clientPhone = "2281029180",
                status = "En registro",
                statusColor = R.color.green
            ),
            Vehicle(
                imageRes = R.drawable.tsuru_tuneado,
                name = "Tsuru",
                brand = "Nissan",
                model = "Tsuru",
                serialNumber = "TS12345",
                arrivalDate = dateFormat.parse("20/05/2023"),
                clientName = "Pepito",
                clientPhone = "2288019209",
                status = "En taller",
                statusColor = R.color.ultraLightBlue
            ),
            Vehicle(
                imageRes = R.drawable.ford_ka,
                name = "Ford Ka",
                brand = "Ford",
                model = "Ka",
                serialNumber = "FK67890",
                arrivalDate = dateFormat.parse("10/05/2023"),
                clientName = "Ranita",
                clientPhone = "2201910191",
                status = "Listo para entregar",
                statusColor = R.color.blue
            ),
            Vehicle(
                imageRes = R.drawable.supra,
                name = "Toyota Supra",
                brand = "Toyoya",
                model = "Supra",
                serialNumber = "ALS100",
                arrivalDate = dateFormat.parse("10/05/2023"),
                clientName = "Juanito",
                clientPhone = "2281010291",
                status = "Listo para entregar",
                statusColor = R.color.blue
            ),

        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = VehicleAdapter(vehicles)
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!)
            }
        )

        view.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            navigateToVehicleRegistration()
        }

        return view
    }

    private fun openEditVehicleActivity(vehicle: Vehicle) {
        val intent = Intent(requireActivity(), VehicleForm::class.java).apply {
            putExtra("vehicle_id", vehicle.id)
            putExtra("brand", vehicle.brand)
            putExtra("model", vehicle.model)
            putExtra("serial_number", vehicle.serialNumber)
            putExtra("arrival_date", SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(vehicle.arrivalDate))
            putExtra("client_name", vehicle.clientName)
            putExtra("client_phone", vehicle.clientPhone)
            putExtra("is_edit_mode", true)
        }
        startActivity(intent)
    }

    private fun navigateToVehicleRegistration() {
        try {
            val intent = Intent(requireActivity(), VehicleForm::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir el formulario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



}