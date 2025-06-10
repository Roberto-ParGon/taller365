package com.uv.taller365

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.uv.taller365.databinding.ActivityLoginBinding
import com.uv.taller365.workshopFiles.CreateWorkshop

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityLoginBinding
    private var isLoadingVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflar el layout usando ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar base de datos
        database = FirebaseDatabase.getInstance().getReference("workshops")

        // Acciones de botones
        binding.buttonLogin.setOnClickListener {
            val code = binding.editTextText.text.toString().trim().uppercase()
            if (code.isEmpty()) {
                Toast.makeText(this, "Ingresa un código", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginWithCode(code)
        }

        binding.buttonSign.setOnClickListener {
            val intent = Intent(this, CreateWorkshop::class.java)
            startActivity(intent)
        }
    }

    private fun loginWithCode(code: String) {
        showLoading(true)

        database.orderByChild("info/code").equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tallerSnapshot = snapshot.children.first()
                        val workshopId = tallerSnapshot.key ?: return
                        val activo = tallerSnapshot.child("activo").getValue(Boolean::class.java) ?: true

                        if (!activo) {
                            showLoading(false)
                            Toast.makeText(this@LoginActivity, "Este taller está desactivado.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        // Registrar hora de login
                        val logRef = database.child("$workshopId/logins").push()
                        logRef.setValue(System.currentTimeMillis())

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("WORKSHOP_ID", workshopId)
                        showLoading(false)
                        startActivity(intent)
                        finish()
                    } else {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, "Código incorrecto", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
            })
    }

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
