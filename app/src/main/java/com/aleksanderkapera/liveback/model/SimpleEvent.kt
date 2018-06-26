package com.aleksanderkapera.liveback.model

/**
 * Created by kapera on 23-May-18.
 */

data class SimpleEvent(val user: SimpleUser,
                       val date: Long,
                       val title: String,
                       val description: String,
                       val category: String,
                       val likes: Int,
                       val comments: Int,
                       val votes: Int)

