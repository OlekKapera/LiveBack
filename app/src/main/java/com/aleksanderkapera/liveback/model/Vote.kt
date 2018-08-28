package com.aleksanderkapera.liveback.model

import java.io.Serializable

/**
 * Created by kapera on 29-Jun-18.
 */
data class Vote(var voteUid: String = "",
                var title: String = "",
                var text: String = "",
                var voteAuthorUid: String = "",
                var upVotes: MutableList<String> = mutableListOf(),
                var downVotes: MutableList<String> = mutableListOf()) : Serializable {
    constructor() : this("", "","", "", mutableListOf(), mutableListOf())
}