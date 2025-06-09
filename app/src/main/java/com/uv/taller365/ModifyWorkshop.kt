package com.uv.taller365

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ModifyWorkshop : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workshop_details, container, false)

        view.findViewById<ImageButton>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }

        // Ajustes para el layout principal
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val nombreTaller = view.findViewById<EditText>(R.id.etWorkshopName)
        val direccionTaller = view.findViewById<EditText>(R.id.etWorkshopAddress)
        val telefonoEncargado = view.findViewById<EditText>(R.id.etPhone)
        val correoEncargado = view.findViewById<EditText>(R.id.etEmail)

        val btnSaveWorkshop = view.findViewById<MaterialButton>(R.id.btnSaveWorkshop)
        val btnDeleteWorkshop = view.findViewById<MaterialButton>(R.id.btnDeleteWorkshop)
        val btnDeleteUser = view.findViewById<ImageButton>(R.id.btnDeleteUser)

        btnSaveWorkshop.setOnClickListener {
            Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
        }

        btnDeleteWorkshop.setOnClickListener {
            showDeleteWorkshopDialog("Nombre del taller")
        }

        btnDeleteUser.setOnClickListener {
            showDeleteUserDialog("Juan Medina")
        }

        return view
    }

    private fun showDeleteWorkshopDialog(workshopName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete_workshop, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        dialogView.findViewById<TextView>(R.id.tvMessage).text =
            "¿Estás seguro que deseas eliminar el taller \"$workshopName\"?"

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            Toast.makeText(requireContext(), "Taller eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteUserDialog(userName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete_user, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        dialogView.findViewById<TextView>(R.id.tvMessage).text =
            "¿Estás seguro que deseas eliminar al usuario \"$userName\"?"

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
