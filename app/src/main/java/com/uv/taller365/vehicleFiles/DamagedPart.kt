package com.uv.taller365.vehicleFiles

import android.os.Parcel
import android.os.Parcelable

data class DamagedPart(
    val partName: String?,
    val subPartName: String?,
    val cost: Double,
    val imageResId: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(partName)
        parcel.writeString(subPartName)
        parcel.writeDouble(cost)
        parcel.writeInt(imageResId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DamagedPart> {
        override fun createFromParcel(parcel: Parcel): DamagedPart {
            return DamagedPart(parcel)
        }

        override fun newArray(size: Int): Array<DamagedPart?> {
            return arrayOfNulls(size)
        }
    }
}