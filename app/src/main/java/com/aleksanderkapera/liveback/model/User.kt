package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by kapera on 26-Jun-18.
 */

@Parcelize
data class User(val uid: String = "",
                val username: String = "",
                val email: String = "",
                var profilePicPath: String = "",
                var commentAddedOnYour: Boolean = true,
                var commentAddedOnFav: Boolean = true,
                var voteAddedOnYour: Boolean = true,
                var voteAddedOnFav: Boolean = true,
                var reminder: Int = 60,
                var profilePicTime: Long = 0,
                var likedEvents: MutableList<String> = mutableListOf()) : Parcelable