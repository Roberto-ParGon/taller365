package com.uv.taller365.workshopFiles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.uv.taller365.R
import com.uv.taller365.helpers.CustomDialogHelper

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
        CustomDialogHelper.showConfirmationDialog(
            activity = requireActivity(),
            title = "Confirmar eliminación",
            message = "¿Estás seguro que deseas eliminar el taller '${workshopName}'?",
            iconResId = R.drawable.warning,
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            onConfirm = { Toast.makeText(requireContext(), "Taller eliminado", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun showDeleteUserDialog(userName: String) {
        CustomDialogHelper.showConfirmationDialog(
            activity = requireActivity(),
            title = "Confirmar eliminación",
            message = "¿Estás seguro que deseas eliminar al usuario '${userName}'?",
            iconResId = R.drawable.warning,
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            onConfirm = { Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show() }
        )
    }
}
