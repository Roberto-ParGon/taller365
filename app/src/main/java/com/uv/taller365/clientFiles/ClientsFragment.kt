package com.uv.taller365.clientFiles

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
import com.bumptech.glide.Glide
import com.uv.taller365.R
import com.uv.taller365.client.ClientForm
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.vehicleFiles.Vehicle

class ClientsFragment : Fragment() {

    private lateinit var vehicleAdapter: ClienteAdapter
    private val clientVehiclesList = mutableListOf<Vehicle>()
    private val allVehiclesList = mutableListOf<Vehicle>()

    private var workshopId: String? = null
    private val firebaseConnection = FirebaseConnection()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workshopId = arguments?.getString("WORKSHOP_ID")
        val view = inflater.inflate(R.layout.fragment_clients, container, false)
        view.setBackgroundColor(resources.getColor(R.color.white, null))

        setupUI(view)
        setupRecyclerView(view)
        loadClientData()

        return view
    }

    private fun setupUI(view: View) {
        view.findViewById<ImageButton>(R.id.btnLogout)?.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        vehicleAdapter = ClienteAdapter(clientVehiclesList)
        recyclerView.adapter = vehicleAdapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let {
                    setDrawable(it)
                }
            }
        )
    }

    private fun loadClientData() {
        if (workshopId.isNullOrEmpty()) {
            Toast.makeText(context, "ID del taller no disponible.", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseConnection.fetchVehicles(
            workshopId!!,
            onResult = { vehicles ->
                allVehiclesList.clear()
                allVehiclesList.addAll(vehicles)

                val uniqueClients = vehicles.distinctBy { it.clientName to it.clientPhone }

                clientVehiclesList.clear()
                clientVehiclesList.addAll(uniqueClients)
                vehicleAdapter.notifyDataSetChanged()
            },
            onError = { exception ->
                Toast.makeText(context, "Error al cargar clientes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

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

            Glide.with(holder.itemView.context)
                .load(vehicle.imageUrl)
                .override(100, 100)
                .placeholder(R.drawable.ic_noimage_24px)
                .error(R.drawable.ic_noimage_24px)
                .circleCrop()
                .into(holder.ivVehicleImage)

            holder.tvVehicleName.text = vehicle.name
            holder.tvBrand.text = "Marca: ${vehicle.brand}"
            holder.tvClientName.text = "Nombre del cliente: ${vehicle.clientName}"
            holder.tvStatus.text = "Seguimiento: ${vehicle.status}"

            holder.tvStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, vehicle.getStatusColor(holder.itemView.context))
            )

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ClientForm::class.java).apply {
                    putExtra("vehicle_name", vehicle.name)
                    putExtra("brand", vehicle.brand)
                    putExtra("model", vehicle.model)
                    putExtra("client_name", vehicle.clientName)
                    putExtra("client_phone", vehicle.clientPhone)
                    putExtra("arrival_date", vehicle.arrivalDate)
                    putExtra("status", vehicle.status)
                    putExtra("image_url", vehicle.imageUrl)
                }
                context.startActivity(intent)
            }
        }

        override fun getItemCount() = vehicles.size
    }
}