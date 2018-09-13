package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by kapera on 29-Jun-18.
 */
@Parcelize
data class Vote(var voteUid: String = "",
                var title: String = "",
                var text: String = "",
                var voteAuthorUid: String = "",
                var upVotes: MutableList<String> = mutableListOf(),
                var downVotes: MutableList<String> = mutableListOf()) : Parcelable