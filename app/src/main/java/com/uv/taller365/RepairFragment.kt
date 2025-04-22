package com.uv.taller365

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

class RepairFragment : Fragment() {

    data class RepairItem(
        val imageRes: Int,
        val title: String,
        val brand: String,
        val model: String,
        val inventory: String
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

            holder.ivEdit.setOnClickListener {
                Toast.makeText(context, "Editar ${item.title}", Toast.LENGTH_SHORT).show()
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
            RepairItem(R.drawable.chevy_pop, "Disco Rotor Delantero Freno", "Fritec", "FR08104", "03"),
            RepairItem(R.drawable.tsuru_tuneado, "Balatas Traseras", "Brembo", "BT1290", "05"),
            RepairItem(R.drawable.ford_ka, "Aceite Motor Sintético", "Mobil", "MX750", "12")
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

        return view
    }
}
