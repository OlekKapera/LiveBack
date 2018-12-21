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
        var reminder = 60
        var profilePicTime: Long = 0
        var likedEvents: MutableList<String> = mutableListOf()
        var yourEvents: MutableList<String> = mutableListOf()

        fun clear() {
            uid = ""
            username = ""
            email = ""
            profilePicPath = ""
            commentAddedOnYour = true
            commentAddedOnFav = true
            voteAddedOnYour = true
            voteAddedOnFav = true
            reminder = 60
            profilePicTime = 0
            likedEvents = mutableListOf()
            yourEvents = mutableListOf()
        }
    }
}