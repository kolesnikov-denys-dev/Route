package com.task.test.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class LocationWithName(val locationName: String, val position: LatLng, val from: Boolean) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(locationName)
        parcel.writeParcelable(position, flags)
        parcel.writeByte(if (from) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationWithName> {
        override fun createFromParcel(parcel: Parcel): LocationWithName {
            return LocationWithName(parcel)
        }

        override fun newArray(size: Int): Array<LocationWithName?> {
            return arrayOfNulls(size)
        }
    }
}