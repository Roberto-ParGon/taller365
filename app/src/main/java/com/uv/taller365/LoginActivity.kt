package com.uv.taller365

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var codeInput: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        codeInput = findViewById(R.id.editTextText)
        btnLogin = findViewById(R.id.buttonLogin)
        btnCreate = findViewById(R.id.buttonSign)

        database = FirebaseDatabase.getInstance().getReference("workshops")

        btnLogin.setOnClickListener {
            val code = codeInput.text.toString().trim().uppercase()
            if (code.isEmpty()) {
                Toast.makeText(this, "Ingresa un código", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginWithCode(code)
        }

        btnCreate.setOnClickListener {
            val intent = Intent(this, CreateWorkshop::class.java)
            startActivity(intent)
        }
    }

    private fun loginWithCode(code: String) {
        database.orderByChild("info/code").equalTo(code)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tallerSnapshot = snapshot.children.first()
                        val workshopId = tallerSnapshot.key ?: return
                        val activo = tallerSnapshot.child("activo").getValue(Boolean::class.java) ?: true

                        if (!activo) {
                            Toast.makeText(this@LoginActivity, "Este taller está desactivado.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        // Registrar hora de login
                        val logRef = database.child("$workshopId/logins").push()
                        logRef.setValue(System.currentTimeMillis())

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("WORKSHOP_ID", workshopId)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Código incorrecto", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
