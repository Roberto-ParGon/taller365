package com.uv.taller365.database

import com.google.firebase.database.*
import com.uv.taller365.repairFiles.Repair
import com.uv.taller365.vehicleFiles.DamagedPart
import com.uv.taller365.vehicleFiles.Vehicle
import com.uv.taller365.workshopFiles.Workshop

class FirebaseConnection {

    private val database = FirebaseDatabase.getInstance().reference

    // ---------------------- REPAIR: CRUD de refacciones ----------------------

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
        val db = database.child("workshops").child(workshopCode).child("repairs")
        val id = db.push().key ?: run {
            onComplete(false)
            return
        }

        val repair = Repair(id, repairType, title, brand, model, inventory, imagePath)
        db.child(id).setValue(repair)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

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
                    child.getValue(Repair::class.java)?.let { repairs.add(it) }
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
        val updatedRepair = Repair(repairId, repairType, title, brand, model, inventory, imagePath)
        database.child("workshops").child(workshopId).child("repairs").child(repairId)
            .setValue(updatedRepair)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteRepair(
        workshopId: String,
        repairId: String,
        onComplete: (Boolean) -> Unit
    ) {
        database.child("workshops").child(workshopId).child("repairs").child(repairId)
            .removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ---------------------- WORKSHOP: Crear, actualizar y eliminar taller ----------------------

    fun writeNewWorkshop(
        name: String,
        address: String,
        phone: String,
        email: String,
        code: String,
        onComplete: (Boolean) -> Unit
    ) {
        val db = database.child("workshops").child(code)
        val data = mapOf(
            "name" to name,
            "address" to address,
            "phone" to phone,
            "email" to email,
            "code" to code,
            "activo" to true,
            "admin" to email
        )
        db.child("info").setValue(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateWorkshop(
        workshopId: String,
        data: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        database.child("workshops").child(workshopId).child("info")
            .updateChildren(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateWorkshopInfo(
        code: String,
        name: String,
        address: String,
        phone: String,
        email: String,
        onComplete: (Boolean) -> Unit
    ) {
        val data = mapOf(
            "name" to name,
            "address" to address,
            "phone" to phone,
            "email" to email,
            "admin" to email
        )

        database.child("workshops").child(code).child("info")
            .updateChildren(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteWorkshop(
        workshopId: String,
        onComplete: (Boolean) -> Unit
    ) {
        database.child("workshops").child(workshopId)
            .removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun fetchWorkshop(
        code: String,
        onResult: (Workshop?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        database.child("workshops").child(code).child("info")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workshop = snapshot.getValue(Workshop::class.java)
                    onResult(workshop)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.toException())
                }
            })
    }

    // ---------------------- USUARIOS: listar y eliminar ----------------------

    fun fetchUsuarios(
        workshopId: String,
        onResult: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        database.child("workshops").child(workshopId).child("usuarios")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usuarios = mutableListOf<String>()
                    for (child in snapshot.children) {
                        child.child("nombre").getValue(String::class.java)?.let { usuarios.add(it) }
                    }
                    onResult(usuarios)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.toException())
                }
            })
    }

    fun deleteUsuario(
        workshopId: String,
        nombre: String,
        onComplete: (Boolean) -> Unit
    ) {
        val usuariosRef = database.child("workshops").child(workshopId).child("usuarios")
        usuariosRef.get()
            .addOnSuccessListener { snapshot ->
                var eliminado = false
                for (child in snapshot.children) {
                    val nombreDb = child.child("nombre").getValue(String::class.java)
                    if (nombreDb == nombre) {
                        child.ref.removeValue().addOnCompleteListener {
                            onComplete(it.isSuccessful)
                        }
                        eliminado = true
                        break
                    }
                }
                if (!eliminado) onComplete(false)
            }
            .addOnFailureListener { onComplete(false) }
    }

    // ---------------------- VEHÍCULOS ----------------------

    fun writeNewVehicle(
        workshopId: String,
        vehicle: Vehicle,
        onComplete: (Boolean) -> Unit
    ) {
        val db = database.child("workshops").child(workshopId).child("vehicles")
        val vehicleId = vehicle.id ?: db.push().key ?: run {
            onComplete(false)
            return
        }
        vehicle.id = vehicleId
        db.child(vehicleId).setValue(vehicle)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun fetchVehicles(
        workshopId: String,
        onResult: (List<Vehicle>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = database.child("workshops").child(workshopId).child("vehicles")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vehicles = mutableListOf<Vehicle>()
                for (child in snapshot.children) {
                    val vehicle = child.getValue(Vehicle::class.java)
                    vehicle?.let { vehicles.add(it) }
                }
                onResult(vehicles)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }
        })
    }

    fun updateVehicle(
        workshopId: String,
        vehicle: Vehicle,
        onComplete: (Boolean) -> Unit
    ) {
        val vehicleId = vehicle.id ?: run {
            onComplete(false)
            return
        }
        database.child("workshops").child(workshopId).child("vehicles").child(vehicleId)
            .setValue(vehicle)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateVehicleStatus(
        workshopId: String,
        vehicleId: String,
        newStatus: String,
        onComplete: (Boolean) -> Unit
    ) {
        database.child("workshops").child(workshopId).child("vehicles").child(vehicleId).child("status")
            .setValue(newStatus)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteVehicle(
        workshopId: String,
        vehicleId: String,
        onComplete: (Boolean) -> Unit
    ) {
        database.child("workshops").child(workshopId).child("vehicles").child(vehicleId)
            .removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun writeNewVehicleWithDamages(
        workshopId: String,
        vehicle: Vehicle,
        damagedParts: List<DamagedPart>,
        onComplete: (Boolean) -> Unit
    ) {
        val db = database.child("workshops").child(workshopId).child("vehicles")
        val vehicleId = db.push().key ?: run {
            onComplete(false)
            return
        }

        vehicle.id = vehicleId
        db.child(vehicleId).setValue(vehicle)
            .addOnSuccessListener {
                db.child(vehicleId).child("damages").setValue(damagedParts)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }
}
