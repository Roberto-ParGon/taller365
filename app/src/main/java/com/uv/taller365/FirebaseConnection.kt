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
}