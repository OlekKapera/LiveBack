package com.aleksanderkapera.liveback.model

/**
 * Created by kapera on 29-Jun-18.
 */
data class Vote(val title: String = "",
                val text: String = "",
                val voteAuthorUid: String = "",
                val upVotes: Int = 0,
                val downVotes: Int = 0) {
    constructor() : this("", "", "", 0, 0)
}