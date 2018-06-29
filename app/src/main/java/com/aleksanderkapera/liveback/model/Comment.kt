package com.aleksanderkapera.liveback.model

/**
 * Created by kapera on 28-Jun-18.
 */
data class Comment(val text: String = "",
                   val postedTime: Long = 0,
                   val commentAuthorUid: String = "") {
    constructor() : this("",0,"")
}