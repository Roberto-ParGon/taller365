package com.uv.taller365

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RepairFragment : Fragment() {

    private val repairsList = mutableListOf<Repair>()
    private lateinit var loadingContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepairAdapter
    private val firebaseConnection = FirebaseConnection()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repair, container, false)
        view.setBackgroundColor(resources.getColor(R.color.lightBlue, null))

        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }

        loadingContainer = view.findViewById(R.id.loadingContainer)
        recyclerView = view.findViewById(R.id.recyclerViewRepairs)

        setupRecyclerView()
        loadRepairs()

        view.findViewById<ImageButton>(R.id.fab3)?.setOnClickListener {
            navigateToRepairRegistration()
        }

        return view
    }

    private fun setupRecyclerView() {
        adapter = RepairAdapter(repairsList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = this@RepairFragment.adapter
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!)
                }
            )
        }
    }

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
                Toast.makeText(context, "Error al cargar refacciones: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

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

        override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
            val item = items[position]

            if (!item.imagePath.isNullOrEmpty()) {
                val decodedBytes = Base64.decode(item.imagePath, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                Glide.with(holder.itemView.context)
                    .load(bitmap)
                    .override(100, 100)
                    .placeholder(R.drawable.noimage)
                    .error(R.drawable.noimage)
                    .circleCrop()
                    .into(holder.imgProduct)
            } else {
                holder.imgProduct.setImageResource(R.drawable.noimage)
            }

            holder.tvTitle.text = item.title.orEmpty()
            holder.tvBrand.text = "Marca: ${item.brand.orEmpty()}"
            holder.tvModel.text = "Modelo: ${item.model.orEmpty()}"
            holder.tvInventory.text = "En inventario: ${item.inventory.orEmpty()}"

            holder.ivEdit.setOnClickListener { openEditRepairActivity(item) }
            holder.ivDelete.setOnClickListener { showDeleteConfirmationDialog(item) }
        }

        override fun getItemCount() = items.size
    }


}
