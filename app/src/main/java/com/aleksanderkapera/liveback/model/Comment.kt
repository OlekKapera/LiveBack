package com.aleksanderkapera.liveback.model

import java.io.Serializable

/**
 * Created by kapera on 28-Jun-18.
 */
data class Comment(val authorName: String = "",
                   val description: String = "",
                   val postedTime: Long = 0,
                   val profilePictureUrl: String = "",
                   val commentAuthorUid: String = "") : Serializable {
    constructor() : this("", "", 0, "", "")
}