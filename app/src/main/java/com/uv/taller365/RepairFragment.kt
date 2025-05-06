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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.UUID

class RepairFragment : Fragment() {

    data class RepairItem(
        val id: String = java.util.UUID.randomUUID().toString(),
        val imageRes: Int,
        val title: String,
        val brand: String,
        val model: String,
        val inventory: String,
        val tipo: String = "Repuesto" // puedes ajustar si quieres cargar el tipo real
    )

    private inner class RepairAdapter(private val items: List<RepairItem>) :
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

            holder.imgProduct.setImageResource(item.imageRes)
            holder.tvTitle.text = item.title
            holder.tvBrand.text = "Marca: ${item.brand}"
            holder.tvModel.text = "Modelo: ${item.model}"
            holder.tvInventory.text = "En inventario: ${item.inventory}"

            // Configurar los listeners de los botones de editar refaccion
            holder.ivEdit.setOnClickListener {
                openEditRepairActivity(item)
            }

            holder.ivDelete.setOnClickListener {
                Toast.makeText(context, "Eliminar ${item.title}", Toast.LENGTH_SHORT).show()
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

        val repairItems = listOf(
            RepairItem(
                id = UUID.randomUUID().toString(), // Agregar un id único
                imageRes = R.drawable.freno,
                title = "Disco Rotor Delantero Freno",
                brand = "Fritec",
                model = "FR08104",
                inventory = "03"
            ),
            RepairItem(
                id = UUID.randomUUID().toString(), // Agregar un id único
                imageRes = R.drawable.balatas,
                title = "Balatas Traseras",
                brand = "Brembo",
                model = "BT1290",
                inventory = "05"
            ),
            RepairItem(
                id = UUID.randomUUID().toString(), // Agregar un id único
                imageRes = R.drawable.aceite,
                title = "Aceite Motor Sintético",
                brand = "Mobil",
                model = "MX750",
                inventory = "12"
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewRepairs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RepairAdapter(repairItems)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider)!!)
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

    private fun openEditRepairActivity(item: RepairItem) {
        val intent = Intent(requireActivity(), RepairForm::class.java).apply {
            putExtra("is_edit_mode", true)
            putExtra("nombre", item.title)
            putExtra("marca", item.brand)
            putExtra("modelo", item.model)
            putExtra("cantidad", item.inventory)
            putExtra("tipo", item.tipo)
            putExtra("image_res", item.imageRes)  // Pasamos la imagen aquí
        }
        startActivity(intent)
    }


}
