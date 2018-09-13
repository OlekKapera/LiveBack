package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by kapera on 28-Jun-18.
 */

@Parcelize
data class Comment(var commentUid: String = "",
                   var description: String = "",
                   var postedTime: Long = 0,
                   var commentAuthorUid: String = "") : Parcelable