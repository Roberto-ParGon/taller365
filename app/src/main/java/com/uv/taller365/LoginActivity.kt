package com.uv.taller365

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.uv.taller365.databinding.ActivityLoginBinding
import com.uv.taller365.helpers.CustomDialogHelper
import com.uv.taller365.workshopFiles.CreateWorkshop

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: DatabaseReference
    private var isLoadingVisible: Boolean = false
    private lateinit var loadingContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingContainer = super.findViewById(R.id.loadingContainer)

        if (restaurarSesionSiExiste()) return

        database = FirebaseDatabase.getInstance().getReference("workshops")

        binding.buttonLogin.setOnClickListener { validarYLogin() }

        binding.buttonSign.setOnClickListener {
            startActivity(Intent(this, CreateWorkshop::class.java))
        }
    }

    // ---------------------- Restaurar sesión previa ----------------------

    private fun restaurarSesionSiExiste(): Boolean {
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        val storedWorkshopId = prefs.getString("WORKSHOP_ID", null)
        val storedName = prefs.getString("USER_NAME", null)

        if (storedWorkshopId != null && storedName != null) {
            FirebaseDatabase.getInstance().getReference("workshops")
                .child(storedWorkshopId)
                .child("info")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            startMainActivity(storedWorkshopId)
                        } else {
                            prefs.edit().clear().apply()
                            Toast.makeText(this@LoginActivity, "Este taller ya no existe", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Error al verificar taller", Toast.LENGTH_SHORT).show()
                    }
                })
            return true
        }
        return false
    }

    // ---------------------- Validaciones y login ----------------------

    private fun validarYLogin() {
        val code = binding.editTextText.text.toString().trim().uppercase()
        val emailOrName = binding.editTextEmailOrName.text.toString().trim()

        if (code.isEmpty()) {
            Toast.makeText(this, "Ingresa un código", Toast.LENGTH_SHORT).show()
            return
        }

        if (emailOrName.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo o tu nombre", Toast.LENGTH_SHORT).show()
            return
        }

        loginWithCode(code, emailOrName)
    }

    private fun loginWithCode(code: String, emailOrName: String) {
        showLoading(true)

        database.orderByChild("info/code").equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        showLoading(false)
                        CustomDialogHelper.showInfoDialog(
                            activity = this@LoginActivity,
                            title = "Error",
                            message = "El código ingresado no es válido. Verifica el código",
                            iconResId = R.drawable.ic_error_24px,
                            buttonText = "Entendido"
                        )
                        return
                    }

                    val tallerSnapshot = snapshot.children.first()
                    val workshopId = tallerSnapshot.key ?: return

                    val info = tallerSnapshot.child("info")
                    val adminEmail = info.child("admin").getValue(String::class.java)
                    val activo = info.child("activo").getValue(Boolean::class.java) ?: true

                    if (!activo) {
                        showLoading(false)
                        CustomDialogHelper.showInfoDialog(
                            activity = this@LoginActivity,
                            title = "Error",
                            message = "Este taller ya no existe",
                            iconResId = R.drawable.ic_error_24px,
                            buttonText = "Entendido"
                        )
                        return
                    }

                    guardarPreferenciasYContinuar(workshopId, emailOrName, adminEmail)
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    CustomDialogHelper.showInfoDialog(
                        activity = this@LoginActivity,
                        title = "Error",
                        message = "Error al iniciar sesión",
                        iconResId = R.drawable.ic_error_24px,
                        buttonText = "Entendido"
                    )
                }
            })
    }

    private fun guardarPreferenciasYContinuar(workshopId: String, emailOrName: String, adminEmail: String?) {
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE).edit()
        prefs.putString("WORKSHOP_ID", workshopId)

        val esAdmin = adminEmail != null && emailOrName.equals(adminEmail, ignoreCase = true)

        if (esAdmin) {
            prefs.putString("USER_NAME", "Administrador")
            prefs.putString("USER_ROLE", "admin")
        } else {
            prefs.putString("USER_NAME", emailOrName)
            prefs.putString("USER_ROLE", "worker")
            registrarUsuarioEnBase(workshopId, emailOrName)
        }

        prefs.apply()
        showLoading(false)
        startMainActivity(workshopId)
    }

    private fun registrarUsuarioEnBase(workshopId: String, nombre: String) {
        val usuariosRef = database.child(workshopId).child("usuarios")
        val nuevoUsuario = mapOf("nombre" to nombre, "timestamp" to System.currentTimeMillis())
        usuariosRef.push().setValue(nuevoUsuario)
    }

    private fun startMainActivity(workshopId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("WORKSHOP_ID", workshopId)
        }
        startActivity(intent)
        finish()
    }

    // ---------------------- Pantalla de cargando ----------------------

    private fun showLoading(isLoading: Boolean) {
        isLoadingVisible = isLoading

        if (isLoading) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE

        window.decorView.systemUiVisibility = if (isLoading) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
