package com.uv.taller365.database

import com.google.firebase.database.*
import com.uv.taller365.repairFiles.Repair
import com.uv.taller365.workshopFiles.Workshop

class FirebaseConnection {

    private val database = FirebaseDatabase.getInstance().reference

    fun writeNewRepair(
        workshopCode: String,
        repairType: String,
        title: String,
        brand: String,
        model: String,
        inventory: String,
        imagePath: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance().getReference("workshops")
            .child(workshopCode)
            .child("repairs")

        val id = db.push().key ?: run {
            onComplete(false)
            return
        }

        val repair = Repair(id, repairType, title, brand, model, inventory, imagePath)
        db.child(id).setValue(repair)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    /* Obtener refacciones del taller */
    fun fetchRepairs(
        workshopId: String,
        onResult: (List<Repair>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = database.child("workshops").child(workshopId).child("repairs")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
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

    fun updateRepair(
        workshopId: String,
        repairId: String,
        repairType: String,
        title: String,
        brand: String,
        model: String,
        inventory: String,
        imagePath: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val db = database.child("workshops").child(workshopId).child("repairs").child(repairId)
        val updatedRepair = Repair(repairId, repairType, title, brand, model, inventory, imagePath)

        db.setValue(updatedRepair)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteRepair(
        workshopId: String,
        repairId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = database.child("workshops").child(workshopId).child("repairs").child(repairId)
        db.removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /* Crear un nuevo taller */
    fun writeNewWorkshop(
        name: String,
        address: String,
        phone: String,
        email: String,
        code: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance().getReference("workshops").child(code)
        val workshop = Workshop(code, name, address, phone, email, code)
        db.child("info").setValue(workshop)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
