package com.aleksanderkapera.liveback.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by kapera on 23-May-18.
 */

data class Event(val userUid: String = "",
                 val date: Long = -1,
                 val title: String = "",
                 val description: String = "",
                 val category: String = "",
                 val likes: Int = -1,
                 val comments: Int = -1,
                 val votes: Int = -1) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt())

    constructor() : this("", -1, "", "", "", -1, -1, -1)

    override fun writeToParcel(write: Parcel?, flags: Int) {
        write?.writeString(userUid)
        write?.writeLong(date)
        write?.writeString(title)
        write?.writeString(description)
        write?.writeString(category)
        write?.writeInt(likes)
        write?.writeInt(comments)
        write?.writeInt(votes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}