package com.aleksanderkapera.liveback.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by kapera on 26-Jun-18.
 */

data class User(val uid: String = "",
                val username: String = "",
                val email: String = "",
                var profilePicPath: String = "",
                var commentAddedOnYour: Boolean = true,
                var commentAddedOnFav: Boolean = true,
                var voteAddedOnYour: Boolean = true,
                var voteAddedOnFav: Boolean = true,
                var reminder: Int = 60,
                var profilePicTime: Long = 0) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readLong())

    constructor() : this("", "", "", "", true,
            true, true, true, 60, 0)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(profilePicPath)
        parcel.writeByte(if (commentAddedOnYour) 1 else 0)
        parcel.writeByte(if (commentAddedOnFav) 1 else 0)
        parcel.writeByte(if (voteAddedOnYour) 1 else 0)
        parcel.writeByte(if (voteAddedOnFav) 1 else 0)
        parcel.writeInt(reminder)
        parcel.writeLong(profilePicTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}