package com.uv.taller365

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseConnection {

    private lateinit var database: DatabaseReference

    /* Operaciones de refacciones */

    /* Metodo para guardar una refaccion */
    fun writeNewRepair(
        repairType: String,
        title: String,
        brand: String,
        model: String,
        inventory: String,
        imagePath: String?, // ruta de la imagen local
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance().getReference("repairs")
        val id = db.push().key ?: run {
            onComplete(false)
            return
        }
        val repair = Repair(id, repairType, title, brand, model, inventory, imagePath)
        db.child(id).setValue(repair)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /* Metodo para obtener todas las refacciones */
    fun fetchRepairs(onResult: (List<Repair>) -> Unit, onError: (Exception) -> Unit) {
        val db = FirebaseDatabase.getInstance().getReference("repairs")
        db.get().addOnSuccessListener { snapshot ->
            val repairs = mutableListOf<Repair>()
            for (child in snapshot.children) {
                val repair = child.getValue(Repair::class.java)
                repair?.let { repairs.add(it) }
            }
            onResult(repairs)
        }.addOnFailureListener { exception ->
            onError(exception)
        }
    }

    fun writeNewWorkshop(
        name: String,
        address: String,
        phone: String,
        email: String,
        code: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance().getReference("workshops")
        val id = db.push().key ?: run {
            onComplete(false)
            return
        }
        val workshop = Workshop(id, name, address, phone, email, code)
        db.child(id).setValue(workshop)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


}