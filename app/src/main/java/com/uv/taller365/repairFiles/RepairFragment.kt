package com.uv.taller365.repairFiles

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.helpers.*

class RepairFragment : Fragment() {

    // Lista de refacciones que se muestran en el RecyclerView
    private val repairsList = mutableListOf<Repair>()

    // Vistas principales
    private lateinit var loadingContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepairAdapter

    // Conexión con Firebase
    private val firebaseConnection = FirebaseConnection()

    // ID del taller (recibido como argumento)
    private var workshopId: String? = null

    // Lista de todas las refacciones para el filtrado
    private val allRepairsList = mutableListOf<Repair>()
    private var currentSelectedTipo: String = "Todos"

    // Buscador
    private lateinit var searchInput: EditText

    // ---------------------- Ciclo de vida ----------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repair, container, false)
        workshopId = arguments?.getString("WORKSHOP_ID")

        // Fondo de color del fragmento
        view.setBackgroundColor(resources.getColor(R.color.lightBlue, null))

        // Botón para cerrar sesión
        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }

        // Inicialización de vistas
        loadingContainer = view.findViewById(R.id.loadingContainer)
        recyclerView = view.findViewById(R.id.recyclerViewRepairs)

        // Buscador
        searchInput = view.findViewById(R.id.searchInput)
        setupSearchBar()

        // Configuración de lista y carga de datos
        setupRecyclerView()
        loadRepairs()

        // Botón para registrar nueva refacción
        view.findViewById<ImageButton>(R.id.fab3)?.setOnClickListener {
            navigateToRepairRegistration()
        }

        // Botón para filtrar refacciones
        view.findViewById<ImageButton>(R.id.btnFilter)?.setOnClickListener {
            CustomDialogHelper.showFilterDialog(
                activity = requireActivity(),
                tipos = listOf("Todos", "Repuesto", "Accesorio", "Herramienta", "Otro"),
                selectedTipo = currentSelectedTipo,
                onFilterSelected = { tipo ->
                    currentSelectedTipo = tipo // Guardar el tipo seleccionado

                    repairsList.clear()
                    if (tipo == "Todos") {
                        repairsList.addAll(allRepairsList)
                    } else {
                        repairsList.addAll(allRepairsList.filter {
                            it.repairType.equals(tipo, ignoreCase = true)
                        })
                    }
                    adapter.notifyDataSetChanged()
                }
            )
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadRepairs()
    }

    // ---------------------- Configuración del RecyclerView ----------------------

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

    // ---------------------- Buscador ----------------------

    private fun setupSearchBar() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRepairs(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterRepairs(query: String) {
        val filtered = allRepairsList.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
                    it.brand?.contains(query, ignoreCase = true) == true ||
                    it.model?.contains(query, ignoreCase = true) == true
        }.filter {
            currentSelectedTipo == "Todos" || it.repairType.equals(currentSelectedTipo, ignoreCase = true)
        }

        repairsList.clear()
        repairsList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    // ---------------------- Carga de datos ----------------------

    private fun loadRepairs() {
        if (repairsList.isEmpty()) {
            loadingContainer.visibility = View.VISIBLE
        }

        firebaseConnection.fetchRepairs(
            workshopId ?: return,
            onResult = { repairs ->
                loadingContainer.visibility = View.GONE
                allRepairsList.clear()
                allRepairsList.addAll(repairs)

                repairsList.clear()
                repairsList.addAll(allRepairsList)
                adapter.notifyDataSetChanged()
            },
            onError = { exception ->
                loadingContainer.visibility = View.GONE
                CustomDialogHelper.showInfoDialog(
                    activity = requireActivity(),
                    title = "Error",
                    message = "Error al cargar refacciones: ${exception.message}",
                    iconResId = R.drawable.ic_error_24px,
                    buttonText = "Entendido"
                )
            }
        )
    }

    // ---------------------- Navegación ----------------------

    private fun navigateToRepairRegistration() {
        if (workshopId.isNullOrEmpty()) {
            CustomDialogHelper.showInfoDialog(
                activity = requireActivity(),
                title = "Error",
                message = "ID del taller no disponible",
                iconResId = R.drawable.ic_error_24px,
                buttonText = "Entendido"
            )
            return
        }

        val intent = Intent(requireActivity(), RepairForm::class.java).apply {
            putExtra("is_edit_mode", false)
            putExtra("workshop_code", workshopId)
        }
        startActivity(intent)
    }

    private fun openEditRepairActivity(item: Repair) {
        if (workshopId.isNullOrEmpty()) {
            CustomDialogHelper.showInfoDialog(
                activity = requireActivity(),
                title = "Error",
                message = "ID del taller no disponible",
                iconResId = R.drawable.ic_error_24px,
                buttonText = "Entendido"
            )
            return
        }

        val intent = Intent(requireActivity(), RepairForm::class.java).apply {
            putExtra("is_edit_mode", true)
            putExtra("repair_id", item.repairId)
            putExtra("nombre", item.title)
            putExtra("marca", item.brand)
            putExtra("modelo", item.model)
            putExtra("cantidad", item.inventory)
            putExtra("tipo", item.repairType)
            putExtra("image_uri", item.imagePath)
            putExtra("workshop_code", workshopId)
        }
        startActivity(intent)
    }

    // ---------------------- Eliminación ----------------------

    private fun showDeleteConfirmationDialog(item: Repair) {
        CustomDialogHelper.showConfirmationDialog(
            activity = requireActivity(),
            title = "Confirmar eliminación",
            message = "¿Estás seguro que deseas eliminar la refacción '${item.title}'?",
            iconResId = R.drawable.ic_warning_24px,
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            onConfirm = { deleteRepair(item) }
        )
    }

    private fun deleteRepair(item: Repair) {
        firebaseConnection.deleteRepair(
            workshopId ?: return,
            item.repairId ?: ""
        ) { success ->
            if (success) {
                CustomDialogHelper.showInfoDialog(
                    activity = requireActivity(),
                    title = "Refacción eliminada",
                    message = "Refacción '${item.title}' eliminada correctamente.",
                    iconResId = R.drawable.ic_success_24px,
                    buttonText = "Aceptar"
                )
                repairsList.remove(item)
                allRepairsList.remove(item)
                adapter.notifyDataSetChanged()
            } else {
                CustomDialogHelper.showInfoDialog(
                    activity = requireActivity(),
                    title = "Error",
                    message = "Error al eliminar la refacción.",
                    iconResId = R.drawable.ic_error_24px,
                    buttonText = "Entendido"
                )
            }
        }
    }

    // ---------------------- Adaptador del RecyclerView ----------------------

    private inner class RepairAdapter(private val items: List<Repair>) :
        RecyclerView.Adapter<RepairAdapter.RepairViewHolder>() {

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

            // Cargar imagen desde Supabase (o usar placeholder)
            if (!item.imagePath.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.imagePath)
                    .override(100, 100)
                    .placeholder(R.drawable.ic_noimage_24px)
                    .error(R.drawable.ic_noimage_24px)
                    .circleCrop()
                    .into(holder.imgProduct)
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_noimage_24px)
            }

            // Mostrar datos
            holder.tvTitle.text = item.title.orEmpty()
            holder.tvBrand.text = "Marca: ${item.brand.orEmpty()}"
            holder.tvModel.text = "Modelo: ${item.model.orEmpty()}"
            holder.tvInventory.text = "En inventario: ${item.inventory.orEmpty()}"

            // Acciones
            holder.itemView.setOnClickListener { showRepairDetailDialog(item) }
            holder.ivEdit.setOnClickListener { openEditRepairActivity(item) }
            holder.ivDelete.setOnClickListener { showDeleteConfirmationDialog(item) }
        }

        override fun getItemCount() = items.size
    }

    private fun showRepairDetailDialog(repair: Repair) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_repair_details, null)
        val dialog = android.app.Dialog(requireContext()).apply {
            setContentView(view)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val image = view.findViewById<ImageView>(R.id.imageDetail)
        val title = view.findViewById<TextView>(R.id.titleDetail)
        val brand = view.findViewById<TextView>(R.id.brandDetail)
        val model = view.findViewById<TextView>(R.id.modelDetail)
        val type = view.findViewById<TextView>(R.id.typeDetail)
        val inventory = view.findViewById<TextView>(R.id.inventoryDetail)
        val btnClose = view.findViewById<Button>(R.id.btnCloseDetail)

        Glide.with(requireContext())
            .load(repair.imagePath)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(40))) // puedes ajustar el valor del radio
            .placeholder(R.drawable.ic_noimage_24px)
            .error(R.drawable.ic_noimage_24px)
            .into(image)

        title.text = repair.title
        brand.text = "Marca: ${repair.brand}"
        model.text = "Modelo: ${repair.model}"
        type.text = "Tipo: ${repair.repairType}"
        inventory.text = "Inventario: ${repair.inventory}"

        btnClose.setOnClickListener { dialog.dismiss() }

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    }

}