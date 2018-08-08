package com.aleksanderkapera.liveback.model

import java.io.Serializable

/**
 * Created by kapera on 29-Jun-18.
 */
data class Vote(val title: String = "",
                val text: String = "",
                val voteAuthorUid: String = "",
                val profilePictureUrl: String = "",
                val upVotes: Int = 0,
                val downVotes: Int = 0) : Serializable {
    constructor() : this("", "", "", "", 0, 0)
}