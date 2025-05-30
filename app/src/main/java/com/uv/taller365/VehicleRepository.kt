package com.uv.taller365

import java.text.SimpleDateFormat
import java.util.*

object VehicleRepository {
    fun getVehicles(): List<Vehicle> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return listOf(
            Vehicle(
                imageRes = R.drawable.chevy_pop,
                name = "Chevy Pop",
                brand = "Chevrolet",
                model = "Pop",
                serialNumber = "FR08104",
                arrivalDate = dateFormat.parse("15/05/2023")!!,
                clientName = "Juanito",
                clientPhone = "2281029180",
                status = "En registro",
                statusColor = R.color.green
            ),
            Vehicle(
                imageRes = R.drawable.tsuru_tuneado,
                name = "Tsuru",
                brand = "Nissan",
                model = "Tsuru",
                serialNumber = "TS12345",
                arrivalDate = dateFormat.parse("20/05/2023")!!,
                clientName = "Pepito",
                clientPhone = "2288019209",
                status = "En taller",
                statusColor = R.color.ultraLightBlue
            ),
            // otros...
        )
    }
}
