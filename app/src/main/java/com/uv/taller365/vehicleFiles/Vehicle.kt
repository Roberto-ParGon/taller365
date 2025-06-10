package com.uv.taller365.vehicleFiles

import java.util.*

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val imageRes: Int,
    val name: String,
    val brand: String,
    val model: String,
    val serialNumber: String,
    val arrivalDate: Date,
    val clientName: String,
    val clientPhone: String,
    val status: String,
    val statusColor: Int
)
