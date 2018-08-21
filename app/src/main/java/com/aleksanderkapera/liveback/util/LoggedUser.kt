package com.aleksanderkapera.liveback.util

/**
 * Singleton containing information about currently logged user
 */
class LoggedUser {
    companion object {
        var uid = ""
        var username = ""
        var email = ""
        var profilePicPath = ""
        var commentAddedOnYour = true
        var commentAddedOnFav = true
        var voteAddedOnYour = true
        var voteAddedOnFav = true
        var reminder = 0

        fun clear() {
            uid = ""
            username = ""
            email = ""
            profilePicPath = ""
            commentAddedOnYour = true
            commentAddedOnFav = true
            voteAddedOnYour = true
            voteAddedOnFav = true
            reminder = 0
        }
    }
}