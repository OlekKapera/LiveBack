package com.aleksanderkapera.liveback.model

/**
 * Created by kapera on 26-Jun-18.
 */

data class User(val uid: String = "",
                val username: String = "",
                val email: String = "",
                var profilePicPath: String? = "",
                var commentAddedOnYour: Boolean = true,
                var commentAddedOnFav: Boolean = true,
                var voteAddedOnYour: Boolean = true,
                var voteAddedOnFav: Boolean = true,
                var reminder: Int = 60) {
    constructor() : this("", "", "", "", true,
            true, true, true, 60)
}