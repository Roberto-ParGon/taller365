package com.uv.taller365.vehicleFiles

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.helpers.CustomDialogHelper
import java.net.URLEncoder
import java.util.*

class VehiclesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var searchInput: EditText

    private val vehiclesList = mutableListOf<Vehicle>()
    private val allVehiclesList = mutableListOf<Vehicle>()
    private var currentStatusFilter: String = "Todos"
    private var workshopId: String? = null
    private val firebaseConnection = FirebaseConnection()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicles, container, false)
        workshopId = arguments?.getString("WORKSHOP_ID")

        setupUI(view)
        setupRecyclerView(view)
        loadVehicles()

        return view
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        vehicleAdapter = VehicleAdapter(
            vehicles = vehiclesList,
            onCardClick = { vehicle -> showStatusUpdateDialog(vehicle) },
            onDeleteClick = { vehicle -> showDeleteConfirmationDialog(vehicle) },
            onEditClick = { vehicle -> navigateToVehicleEdit(vehicle) }
        )
        recyclerView.adapter = vehicleAdapter

        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)?.let { setDrawable(it) }
            }
        )
    }

    private fun enviarMensajeWhatsApp(marca: String, modelo: String, telefono: String) {
        val context = requireContext()
        val mensaje = "¡Hola! Tu vehículo '$marca $modelo' se encuentra listo para que pases a recogerlo. ¡Te esperamos en Taller365!"


        val numeroLimpio = telefono.replace(Regex("[\\s-]"), "")
        val numeroInternacional = if (numeroLimpio.startsWith("+")) numeroLimpio else "+52$numeroLimpio"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$numeroInternacional&text=${URLEncoder.encode(mensaje, "UTF-8")}")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp no está instalado en este dispositivo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStatusUpdateDialog(vehicle: Vehicle) {
        val possibleActions: Array<String> = when (vehicle.status) {
            "En registro" -> arrayOf("Mover a Taller")
            "En taller" -> arrayOf("Marcar como Listo para Entregar")
            "Listo para entregar" -> arrayOf("Marcar como Entregado")
            else -> {
                Toast.makeText(context, "Este vehículo ya completó su ciclo.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Acciones para: ${vehicle.name}")
            .setItems(possibleActions) { _, which ->
                val selectedAction = possibleActions[which]
                val newStatus = when (selectedAction) {
                    "Mover a Taller" -> "En taller"
                    "Marcar como Listo para Entregar" -> "Listo para entregar"
                    "Marcar como Entregado" -> "Entregado"
                    else -> ""
                }

                if (newStatus.isNotEmpty() && vehicle.id != null) {
                    firebaseConnection.updateVehicleStatus(workshopId!!, vehicle.id!!, newStatus) { success ->
                        if (success) {
                            Toast.makeText(context, "Estado actualizado a: $newStatus", Toast.LENGTH_SHORT).show()

                            if (newStatus == "Listo para entregar") {
                                val telefonoCliente = vehicle.clientPhone
                                val marcaVehiculo = vehicle.brand
                                val modeloVehiculo = vehicle.model

                                if (!telefonoCliente.isNullOrEmpty() && !marcaVehiculo.isNullOrEmpty() && !modeloVehiculo.isNullOrEmpty()) {
                                    enviarMensajeWhatsApp(marcaVehiculo, modeloVehiculo, telefonoCliente)
                                } else {
                                    Toast.makeText(context, "No se puede enviar el mensaje. Faltan datos del cliente o vehículo.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Error al actualizar el estado.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun navigateToVehicleEdit(vehicle: Vehicle) {
        val intent = Intent(requireActivity(), VehicleForm::class.java).apply {
            putExtra("IS_EDIT_MODE", true)
            putExtra("WORKSHOP_ID", workshopId)
            putExtra("VEHICLE_ID", vehicle.id)
            putExtra("BRAND", vehicle.brand)
            putExtra("MODEL", vehicle.model)
            putExtra("SERIAL_NUMBER", vehicle.serialNumber)
            putExtra("ARRIVAL_DATE", vehicle.arrivalDate)
            putExtra("CLIENT_NAME", vehicle.clientName)
            putExtra("CLIENT_PHONE", vehicle.clientPhone)
            putExtra("STATUS", vehicle.status)
            putExtra("IMAGE_URL", vehicle.imageUrl)
        }
        startActivity(intent)
    }

    private fun setupUI(view: View) {
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        view.findViewById<TextView>(R.id.tallerCode)?.text = workshopId
        view.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            navigateToVehicleRegistration()
        }
        view.findViewById<ImageButton>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }
        searchInput = view.findViewById(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterVehicleList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        view.findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            showStatusFilterDialog()
        }
    }

    private fun loadVehicles() {
        if (workshopId.isNullOrEmpty()) return
        firebaseConnection.fetchVehicles(workshopId!!,
            onResult = { vehicles ->
                allVehiclesList.clear()
                allVehiclesList.addAll(vehicles)
                filterVehicleList()
            },
            onError = { exception ->
                Toast.makeText(context, "Error al cargar vehículos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showStatusFilterDialog() {
        val statuses = listOf("Todos", "En registro", "En taller", "Listo para entregar", "Entregado")
        CustomDialogHelper.showFilterDialog(requireActivity(), statuses, currentStatusFilter) { selectedStatus ->
            currentStatusFilter = selectedStatus
            searchInput.text.clear()
            filterVehicleList()
        }
    }

    private fun filterVehicleList() {
        val searchQuery = searchInput.text.toString().lowercase(Locale.getDefault())
        val filteredList = allVehiclesList.filter { vehicle ->
            val statusMatch = when (currentStatusFilter) {
                "Todos" -> vehicle.status != "Entregado"
                else -> vehicle.status.equals(currentStatusFilter, ignoreCase = true)
            }
            val searchMatch = if (searchQuery.isEmpty()) true else {
                vehicle.brand?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                        vehicle.model?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                        vehicle.serialNumber?.lowercase(Locale.getDefault())?.contains(searchQuery) == true
            }
            statusMatch && searchMatch
        }
        vehiclesList.clear()
        vehiclesList.addAll(filteredList)
        vehicleAdapter.notifyDataSetChanged()
    }

    private fun navigateToVehicleRegistration() {
        val intent = Intent(requireActivity(), VehicleForm::class.java).apply {
            putExtra("WORKSHOP_ID", workshopId)
            putExtra("IS_EDIT_MODE", false)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(vehicle: Vehicle) {
        val activity = activity ?: return

        CustomDialogHelper.showConfirmationDialog(
            activity = activity,
            title = "Confirmar eliminación",
            message = "¿Estás seguro de que deseas eliminar el vehículo '${vehicle.name}'?",
            onConfirm = {
                deleteVehicle(vehicle)
            }
        )
    }

    private fun deleteVehicle(vehicle: Vehicle) {
        if (workshopId.isNullOrEmpty() || vehicle.id.isNullOrEmpty()) return
        firebaseConnection.deleteVehicle(workshopId!!, vehicle.id!!) { success ->
            if (success) Toast.makeText(context, "Vehículo eliminado.", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Error al eliminar.", Toast.LENGTH_SHORT).show()
        }
    }
}

private class VehicleAdapter(
    private val vehicles: List<Vehicle>,
    private val onCardClick: (Vehicle) -> Unit,
    private val onDeleteClick: (Vehicle) -> Unit,
    private val onEditClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVehicleImage: ImageView = itemView.findViewById(R.id.ivVehicleImage)
        val tvVehicleName: TextView = itemView.findViewById(R.id.tvVehicleName)
        val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
        val tvSerialNumber: TextView = itemView.findViewById(R.id.tvSerialNumber)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vehicle_card, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicles[position]
        holder.tvVehicleName.text = vehicle.name
        holder.tvBrand.text = "Marca: ${vehicle.brand}"
        holder.tvSerialNumber.text = "No. Serie: ${vehicle.serialNumber}"
        holder.tvStatus.text = vehicle.status
        holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, vehicle.getStatusColor(holder.itemView.context)))
        Glide.with(holder.itemView.context).load(vehicle.imageUrl).placeholder(R.drawable.ic_car_24px).error(R.drawable.ic_noimage_24px).circleCrop().into(holder.ivVehicleImage)

        holder.itemView.setOnClickListener { onCardClick(vehicle) }
        holder.ivEdit.setOnClickListener { onEditClick(vehicle) }
        holder.ivDelete.setOnClickListener { onDeleteClick(vehicle) }
    }



    override fun getItemCount() = vehicles.size
}