package com.aleksanderkapera.liveback.model

import java.io.Serializable

/**
 * Created by kapera on 29-Jun-18.
 */
data class Vote(val voteUid: String = "",
                val title: String = "",
                val text: String = "",
                val voteAuthorUid: String = "",
                val upVotes: MutableList<String> = mutableListOf(),
                val downVotes: MutableList<String> = mutableListOf()) : Serializable {
    constructor() : this("", "","", "", mutableListOf(), mutableListOf())
}