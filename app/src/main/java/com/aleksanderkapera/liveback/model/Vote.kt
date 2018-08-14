package com.aleksanderkapera.liveback.model

import java.io.Serializable

/**
 * Created by kapera on 29-Jun-18.
 */
data class Vote(val title: String = "",
                val text: String = "",
                val voteAuthorUid: String = "",
                val profilePictureUrl: String = "",
                val upVotes: List<String> = listOf(),
                val downVotes: List<String> = listOf()) : Serializable {
    constructor() : this("", "", "", "", listOf(), listOf())
}