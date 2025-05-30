package com.uv.taller365

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.UUID

class RepairFragment : Fragment() {

    private val repairsList = mutableListOf<Repair>()

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
                Glide.with(holder.itemView.context)
                    .load(File(item.imagePath))
                    .placeholder(R.drawable.ic_upload_24px)
                    .error(R.drawable.ic_upload_24px)
                    .circleCrop()
                    .into(holder.imgProduct)
            } else {
                holder.imgProduct.setImageResource(R.drawable.ic_upload_24px)
            }

            holder.tvTitle.text = item.title ?: ""
            holder.tvBrand.text = "Marca: ${item.brand ?: ""}"
            holder.tvModel.text = "Modelo: ${item.model ?: ""}"
            holder.tvInventory.text = "En inventario: ${item.inventory ?: ""}"

            holder.ivEdit.setOnClickListener {
                openEditRepairActivity(item)
            }

            holder.ivDelete.setOnClickListener {
                showDeleteConfirmationDialog(item)
            }
        }

        override fun getItemCount() = items.size
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repair, container, false)
        view.setBackgroundColor(resources.getColor(R.color.lightBlue, null))

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewRepairs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!)
            }
        )

        val adapter = RepairAdapter(repairsList)
        recyclerView.adapter = adapter

        val firebaseConnection = FirebaseConnection()
        firebaseConnection.fetchRepairs(
            onResult = { repairs ->
                repairsList.clear()
                repairsList.addAll(repairs)
                adapter.notifyDataSetChanged()
            },
            onError = { exception ->
                Toast.makeText(context, "Error al cargar refacciones: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )

        view.findViewById<FloatingActionButton>(R.id.fab3)?.setOnClickListener {
            navigateToRepairRegistration()
        }

        return view
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

    private fun openEditRepairActivity(item: Repair ) {
        val intent = Intent(requireActivity(), RepairForm::class.java).apply {
            putExtra("is_edit_mode", true)
            putExtra("nombre", item.title)
            putExtra("marca", item.brand)
            putExtra("modelo", item.model)
            putExtra("cantidad", item.inventory)
            putExtra("tipo", item.repairType)
            putExtra("image_uri", item.imagePath)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(item: Repair ) {
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Confirmar eliminación de refacción")
        dialogBuilder.setMessage("¿Estás seguro que deseas eliminar la refacción '${item.title}'?")

        // Botón "Cancelar"
        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Botón "Eliminar"
        dialogBuilder.setPositiveButton("Eliminar") { dialog, _ ->
            deleteRepair(item)  // Llamamos a la función de eliminar
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteRepair(item: Repair ) {
        // Aquí puedes implementar la lógica para eliminar el ítem, ya sea de una lista, base de datos, etc.
        // Por ahora solo mostramos un mensaje de confirmación.
        Toast.makeText(context, "Refacción '${item.title}' eliminada", Toast.LENGTH_SHORT).show()
    }

}
