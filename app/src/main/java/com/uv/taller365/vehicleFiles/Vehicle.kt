package com.uv.taller365.vehicleFiles

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.uv.taller365.R


data class Vehicle(
    var id: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var brand: String? = null,
    var model: String? = null,
    var serialNumber: String? = null,
    var arrivalDate: String? = null,
    var clientName: String? = null,
    var clientPhone: String? = null,
    var status: String? = "En registro"
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    constructor() : this(null, null, null, null, null, null, null, null, null, "En registro")

    @Exclude
    fun getStatusColor(context: Context): Int {
        return when (status) {
            "En registro" -> R.color.green
            "En taller" -> R.color.orange
            "Listo para entregar" -> R.color.blue
            else -> R.color.dark_gray
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(imageUrl)
        parcel.writeString(name)
        parcel.writeString(brand)
        parcel.writeString(model)
        parcel.writeString(serialNumber)
        parcel.writeString(arrivalDate)
        parcel.writeString(clientName)
        parcel.writeString(clientPhone)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        override fun createFromParcel(parcel: Parcel): Vehicle {
            return Vehicle(parcel)
        }

        override fun newArray(size: Int): Array<Vehicle?> {
            return arrayOfNulls(size)
        }
    }
}