package com.uv.taller365

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class RepairFragment : Fragment() {

    // Lista que contiene las refacciones obtenidas
    private val repairsList = mutableListOf<Repair>()

    // Vistas de la interfaz
    private lateinit var loadingContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView

    // Adaptador para el RecyclerView
    private lateinit var adapter: RepairAdapter

    // Conexión a Firebase
    private val firebaseConnection = FirebaseConnection()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repair, container, false)

        // Fondo del fragmento
        view.setBackgroundColor(resources.getColor(R.color.lightBlue, null))

        // Botón de cerrar sesión
        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }

        // Inicialización de vistas
        loadingContainer = view.findViewById(R.id.loadingContainer)
        recyclerView = view.findViewById(R.id.recyclerViewRepairs)

        // Configuración del RecyclerView
        setupRecyclerView()

        // Carga de refacciones desde Firebase
        loadRepairs()

        // Navegar al formulario de registro de refacción
        view.findViewById<ImageButton>(R.id.fab3)?.setOnClickListener {
            navigateToRepairRegistration()
        }

        return view
    }

    // Configura el RecyclerView
    private fun setupRecyclerView() {
        adapter = RepairAdapter(repairsList)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = this@RepairFragment.adapter

            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!
                    )
                }
            )
        }
    }

    // Carga las refacciones desde Firebase
    private fun loadRepairs() {
        loadingContainer.visibility = View.VISIBLE

        firebaseConnection.fetchRepairs(
            onResult = { repairs ->
                loadingContainer.visibility = View.GONE
                repairsList.clear()
                repairsList.addAll(repairs)
                adapter.notifyDataSetChanged()
            },
            onError = { exception ->
                loadingContainer.visibility = View.GONE
                Toast.makeText(
                    context,
                    "Error al cargar refacciones: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Navega al formulario para registrar una nueva refacción
    private fun navigateToRepairRegistration() {
        try {
            val intent = Intent(requireActivity(), RepairForm::class.java).apply {
                putExtra("is_edit_mode", false)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir el formulario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Navega al formulario para editar una refacción existente
    private fun openEditRepairActivity(item: Repair) {
        val intent = Intent(requireActivity(), RepairForm::class.java).apply {
            putExtra("is_edit_mode", true)
            putExtra("repair_id", item.repairId)
            putExtra("nombre", item.title)
            putExtra("marca", item.brand)
            putExtra("modelo", item.model)
            putExtra("cantidad", item.inventory)
            putExtra("tipo", item.repairType)
            putExtra("image_uri", item.imagePath)
        }
        startActivity(intent)
    }

    // Muestra un diálogo de confirmación para eliminar una refacción
    private fun showDeleteConfirmationDialog(item: Repair) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación de refacción")
            .setMessage("¿Estás seguro que deseas eliminar la refacción '${item.title}'?")
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Eliminar") { dialog, _ ->
                deleteRepair(item)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Elimina una refacción de Firebase
    private fun deleteRepair(item: Repair) {
        firebaseConnection.deleteRepair(item.repairId ?: "") { success ->
            if (success) {
                Toast.makeText(context, "Refacción '${item.title}' eliminada", Toast.LENGTH_SHORT).show()
                repairsList.remove(item)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(context, "Error al eliminar la refacción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Adaptador para mostrar las refacciones
    private inner class RepairAdapter(private val items: List<Repair>) :
        RecyclerView.Adapter<RepairAdapter.RepairViewHolder>() {

        // ViewHolder para cada tarjeta de refacción
        inner class RepairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
            val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
            val tvModel: TextView = itemView.findViewById(R.id.tvModel)
            val tvInventory: TextView = itemView.findViewById(R.id.tvInventory)
            val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
            val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.repair_card, parent, false)
            return RepairViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
            val item = items[position]

            // Carga de imagen desde Supabase
            if (!item.imagePath.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.imagePath)
                    .override(100, 100)
                    .placeholder(R.drawable.noimage)
                    .error(R.drawable.noimage)
                    .circleCrop()
                    .into(holder.imgProduct)
            } else {
                holder.imgProduct.setImageResource(R.drawable.noimage)
            }

            // Asignación de datos al ViewHolder
            holder.tvTitle.text = item.title.orEmpty()
            holder.tvBrand.text = "Marca: ${item.brand.orEmpty()}"
            holder.tvModel.text = "Modelo: ${item.model.orEmpty()}"
            holder.tvInventory.text = "En inventario: ${item.inventory.orEmpty()}"

            // Acciones de editar y eliminar
            holder.ivEdit.setOnClickListener { openEditRepairActivity(item) }
            holder.ivDelete.setOnClickListener { showDeleteConfirmationDialog(item) }
        }

        override fun getItemCount() = items.size
    }
}
