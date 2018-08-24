package com.aleksanderkapera.liveback.model

import java.io.Serializable

/**
 * Created by kapera on 28-Jun-18.
 */
data class Comment(var commentUid: String = "",
                   var description: String = "",
                   var postedTime: Long = 0,
                   var commentAuthorUid: String = "") : Serializable {
    constructor() : this("","", 0, "")
}