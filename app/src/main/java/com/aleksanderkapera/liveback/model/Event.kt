package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by kapera on 23-May-18.
 */

@Parcelize
data class Event(var eventUid: String = "",
                 var userUid: String = "",
                 var title: String = "",
                 var description: String = "",
                 var address: String = "",
                 var date: Long = -1,
                 var category: String = "",
                 var backgroundPicturePath: String = "",
                 var backgroundPictureTime: Long = 0,
                 var likes: MutableList<String> = mutableListOf(),
                 var comments: Int = 0,
                 var votes: Int = 0) : Parcelable