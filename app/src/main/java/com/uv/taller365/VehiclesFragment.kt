package com.uv.taller365

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VehiclesFragment : Fragment() {

    data class Vehicle(
        val imageRes: Int,
        val name: String,
        val brand: String,
        val serialNumber: String,
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
                Toast.makeText(context, "Editar ${vehicle.name}", Toast.LENGTH_SHORT).show()
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
        val vehicles = listOf(
            Vehicle(
                R.drawable.chevy_pop,
                "Chevy Pop 2002",
                "Chevrolet",
                "FR08104",
                "En registro",
                R.color.green
            ),
            Vehicle(
                R.drawable.tsuru_tuneado,
                "Tsuru tunning",
                "Nissan",
                "TS12345",
                "En taller",
                R.color.ultraLightBlue
            ),
            Vehicle(
                R.drawable.ford_ka,
                "Ford Ka",
                "Ford",
                "FK67890",
                "Listo para entregar",
                R.color.blue
            ),
            Vehicle(
                R.drawable.baseline_directions_car_24,
                "TEST",
                "TEST",
                "FK67890",
                "Listo para entregar",
                R.color.blue
            ),
            Vehicle(
                R.drawable.baseline_directions_car_24,
                "TEST",
                "TEST",
                "FK67890",
                "Listo para entregar",
                R.color.blue
            ),
            Vehicle(
                R.drawable.baseline_directions_car_24,
                "TEST",
                "TEST",
                "FK67890",
                "Listo para entregar",
                R.color.blue
            ),


        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = VehicleAdapter(vehicles)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!)
            }
        )

        return view
    }
}