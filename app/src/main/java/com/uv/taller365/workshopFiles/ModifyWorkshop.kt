package com.uv.taller365.workshopFiles

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.uv.taller365.LoginActivity
import com.uv.taller365.R
import com.uv.taller365.database.FirebaseConnection
import com.uv.taller365.helpers.CustomDialogHelper

class ModifyWorkshop : Fragment() {

    private val firebaseConnection = FirebaseConnection()
    private var workshopId: String? = null

    private lateinit var containerUsuarios: LinearLayout
    private lateinit var usuariosScroll: ScrollView

    private lateinit var etNombre: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText

    private lateinit var loadingContainer: LinearLayout
    private var isLoading = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_workshop_details, container, false)
        setupEdgeToEdge(view)
        setupViews(view)
        showLoading(true)

        workshopId = activity?.intent?.getStringExtra("WORKSHOP_ID")
        workshopId?.let { id ->
            fetchWorkshopData(id)
            cargarUsuarios(id)
        }

        return view
    }

    private fun setupEdgeToEdge(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + 24)
            insets
        }
    }

    private fun setupViews(view: View) {
        etNombre = view.findViewById(R.id.etWorkshopName)
        etDireccion = view.findViewById(R.id.etWorkshopAddress)
        etTelefono = view.findViewById(R.id.etPhone)
        etCorreo = view.findViewById(R.id.etEmail)

        containerUsuarios = view.findViewById(R.id.containerUsuarios)
        usuariosScroll = view.findViewById(R.id.scrollUsuarios)
        loadingContainer = view.findViewById(R.id.loadingContainer)

        view.findViewById<View>(R.id.btnLogout)?.setOnClickListener {
            activity?.finish()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveWorkshop).setOnClickListener {
            guardarCambios()
        }

        view.findViewById<MaterialButton>(R.id.btnDeleteWorkshop).setOnClickListener {
            showDeleteWorkshopDialog(etNombre.text.toString())
        }
    }

    private fun fetchWorkshopData(id: String) {
        firebaseConnection.fetchWorkshop(
            code = id,
            onResult = { taller ->
                taller?.let {
                    etNombre.setText(it.name)
                    etDireccion.setText(it.address)
                    etTelefono.setText(it.phone)
                    etCorreo.setText(it.email)
                }
                showLoading(false)
            },
            onError = {
                Toast.makeText(requireContext(), "Error al cargar taller", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        )
    }

    private fun guardarCambios() {
        val nombre = etNombre.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val correo = etCorreo.text.toString().trim()

        if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        workshopId?.let { id ->
            firebaseConnection.updateWorkshopInfo(
                code = id,
                name = nombre,
                address = direccion,
                phone = telefono,
                email = correo
            ) { success ->
                showLoading(false)
                val msg = if (success) "Taller actualizado correctamente" else "Error al actualizar taller"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                if (success) {
                    val prefs = requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                }
            }
        }
    }

    private fun showDeleteWorkshopDialog(workshopName: String) {
        CustomDialogHelper.showConfirmationDialog(
            activity = requireActivity(),
            title = "Confirmar eliminación",
            message = "¿Estás seguro que deseas eliminar el taller '$workshopName'?",
            iconResId = R.drawable.ic_warning_24px,
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            onConfirm = { eliminarTaller() }
        )
    }

    private fun eliminarTaller() {
        workshopId?.let { id ->
            firebaseConnection.deleteWorkshop(id) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Taller eliminado", Toast.LENGTH_SHORT).show()
                    val prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar taller", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarUsuarios(workshopId: String) {
        firebaseConnection.fetchUsuarios(
            workshopId = workshopId,
            onResult = { usuarios ->
                containerUsuarios.removeAllViews()
                usuariosScroll.visibility = if (usuarios.isEmpty()) View.GONE else View.VISIBLE

                for (usuario in usuarios) {
                    val row = createUsuarioRow(usuario)
                    containerUsuarios.addView(row)
                }
            },
            onError = {
                Toast.makeText(requireContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun createUsuarioRow(usuario: String): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            setPadding(8, 8, 8, 8)
        }

        val icon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_user_24px)
            layoutParams = LinearLayout.LayoutParams(50, 50)
        }

        val textView = TextView(requireContext()).apply {
            text = usuario
            textSize = 16f
            setTextColor(resources.getColor(R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(16, 0, 0, 0)
        }

        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.ic_delete_24px)
            setBackgroundResource(android.R.color.transparent)
            setColorFilter(resources.getColor(R.color.red, null))
            setOnClickListener { showDeleteUserDialog(usuario) }
        }

        row.addView(icon)
        row.addView(textView)
        row.addView(deleteBtn)

        return row
    }

    private fun showDeleteUserDialog(userName: String) {
        CustomDialogHelper.showConfirmationDialog(
            activity = requireActivity(),
            title = "Confirmar eliminación",
            message = "¿Estás seguro que deseas eliminar al usuario '$userName'?",
            iconResId = R.drawable.ic_warning_24px,
            confirmText = "Eliminar",
            cancelText = "Cancelar",
            onConfirm = { deleteUsuario(userName) }
        )
    }

    private fun deleteUsuario(nombre: String) {
        workshopId?.let { id ->
            firebaseConnection.deleteUsuario(id, nombre) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    cargarUsuarios(id)
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(active: Boolean) {
        isLoading = active
        loadingContainer.visibility = if (active) View.VISIBLE else View.GONE

        activity?.window?.apply {
            if (active) {
                setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }
}
