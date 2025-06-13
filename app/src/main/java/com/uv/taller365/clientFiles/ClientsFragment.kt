package com.uv.taller365.clientFiles

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.uv.taller365.helpers.CustomDialogHelper
import com.uv.taller365.vehicleFiles.Vehicle
import java.util.Locale

class ClientsFragment : Fragment() {

    private lateinit var vehicleAdapter: ClienteAdapter
    private lateinit var searchInput: EditText

    private val filteredVehiclesList = mutableListOf<Vehicle>()
    private val allVehiclesList = mutableListOf<Vehicle>()

    private var workshopId: String? = null
    private val firebaseConnection = FirebaseConnection()

    private var currentStatusFilter: String = "Todos"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workshopId = arguments?.getString("WORKSHOP_ID")
        val view = inflater.inflate(R.layout.fragment_clients, container, false)
        view.setBackgroundColor(resources.getColor(R.color.white, null))

        setupViews(view)
        setupRecyclerView(view)
        loadClientData()

        return view
    }

    private fun setupViews(view: View) {
        view.findViewById<ImageButton>(R.id.btnLogout)?.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        searchInput = view.findViewById(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterClientList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        view.findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            showStatusFilterDialog()
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        vehicleAdapter = ClienteAdapter(filteredVehiclesList)
        recyclerView.adapter = vehicleAdapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
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
                filterClientList()
            },
            onError = { exception ->
                Toast.makeText(context, "Error al cargar clientes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun filterClientList() {
        val searchQuery = searchInput.text.toString().lowercase(Locale.getDefault())

        val filtered = allVehiclesList.filter { vehicle ->
            val statusMatch = when (currentStatusFilter) {
                "Todos" -> true
                else -> vehicle.status.equals(currentStatusFilter, ignoreCase = true)
            }

            val searchMatch = if (searchQuery.isEmpty()) {
                true
            } else {
                vehicle.brand?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                        vehicle.model?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                        vehicle.clientName?.lowercase(Locale.getDefault())?.contains(searchQuery) == true
            }

            statusMatch && searchMatch
        }

        filteredVehiclesList.clear()
        filteredVehiclesList.addAll(filtered)
        vehicleAdapter.notifyDataSetChanged()
    }

    private fun showStatusFilterDialog() {
        val statuses = listOf("Todos", "En registro", "En taller", "Listo para entregar", "Entregado")
        CustomDialogHelper.showFilterDialog(requireActivity(), statuses, currentStatusFilter) { selectedStatus ->
            currentStatusFilter = selectedStatus
            filterClientList()
        }
    }

    private inner class ClienteAdapter(private val vehicles: List<Vehicle>) :
        RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

        inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivVehicleImage: ImageView = itemView.findViewById(R.id.ivVehicleImage)
            val tvVehicleName: TextView = itemView.findViewById(R.id.tvVehicleName)
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
            holder.tvClientName.text = "${vehicle.clientName}"
            holder.tvStatus.text = "${vehicle.status}"

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