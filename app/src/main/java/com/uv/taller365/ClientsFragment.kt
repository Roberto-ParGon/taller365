package com.uv.taller365

import android.annotation.SuppressLint
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

class ClientsFragment : Fragment() {

    private inner class ClienteAdapter(private val vehicles: List<Vehicle>) :
        RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

        inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivVehicleImage: ImageView = itemView.findViewById(R.id.ivVehicleImage)
            val tvVehicleName: TextView = itemView.findViewById(R.id.tvVehicleName)
            val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
            val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.client_card, parent, false)
            return ClienteViewHolder(view)
        }

        override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
            val vehicle = vehicles[position]

            holder.ivVehicleImage.setImageResource(vehicle.imageRes)
            holder.tvVehicleName.text = vehicle.name
            holder.tvBrand.text = "Marca: ${vehicle.brand}"
            holder.tvClientName.text = "Nombre del cliente: ${vehicle.clientName}"
            holder.tvStatus.text = "Seguimiento: ${vehicle.status}"
            holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, vehicle.statusColor)
            )

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ClientForm::class.java).apply {
                    putExtra("vehicle_name", vehicle.name)
                    putExtra("", vehicle.name)
                    putExtra("client_name", vehicle.clientName)
                    putExtra("client_phone", vehicle.clientPhone)

                }
                context.startActivity(intent)
        }

        override fun getItemCount() = vehicles.size
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_clients, container, false)
        view.setBackgroundColor(resources.getColor(R.color.white, null))

        view.findViewById<ImageButton>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }

        // TODO: En el futuro puedes reemplazar esta línea por una consulta a Firebase
        val vehicles = VehicleRepository.getVehicles()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ClienteAdapter(vehicles)
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let {
                    setDrawable(it)
                }
            }
        )

        view.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            navigateToClientForm()
        }

        return view
    }

    private fun navigateToClientForm() {
        try {
            val intent = Intent(requireActivity(), ClientForm::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir formulario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
