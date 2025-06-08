package com.uv.taller365

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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
        imagePath: String?,
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
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val repairs = mutableListOf<Repair>()
                for (child in snapshot.children) {
                    val repair = child.getValue(Repair::class.java)
                    repair?.let { repairs.add(it) }
                }
                onResult(repairs)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        })
    }

    /* Metodo para actualizar una refaccion */
    fun updateRepair(
        repairId: String,
        repairType: String,
        title: String,
        brand: String,
        model: String,
        inventory: String,
        imagePath: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance().getReference("repairs")
        val updatedRepair = Repair(repairId, repairType, title, brand, model, inventory, imagePath)
        db.child(repairId).setValue(updatedRepair)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /* Metodo para eliminar una refaccion */
    fun deleteRepair(
        repairId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance().getReference("repairs")
        db.child(repairId).removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /* Operaciones de talleres */
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