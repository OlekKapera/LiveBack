package com.aleksanderkapera.liveback.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by kapera on 23-May-18.
 */

data class Event(var eventUid: String = "",
                 var userUid: String = "",
                 var title: String = "",
                 var description: String = "",
                 var address: String = "",
                 var date: Long = -1,
                 var category: String = "",
                 var backgroundPicturePath: String = "",
                 var likes: MutableList<String> = mutableListOf(),
                 var comments: Int = 0,
                 var votes: Int = 0) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readSerializable() as MutableList<String>,
            parcel.readInt(),
            parcel.readInt())

    constructor() : this("", "", "", "", "", 0, "", "", mutableListOf(), 0, 0)

    override fun writeToParcel(write: Parcel?, flags: Int) {
        write?.writeString(eventUid)
        write?.writeString(userUid)
        write?.writeLong(date)
        write?.writeString(title)
        write?.writeString(description)
        write?.writeString(category)
        write?.writeStringList(likes)
        write?.writeInt(comments)
        write?.writeInt(votes)
        write?.writeString(backgroundPicturePath)
        write?.writeString(address)
    }

    override fun describeContents(): Int = 0


    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}