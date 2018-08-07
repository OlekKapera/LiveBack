package com.aleksanderkapera.liveback.util

/**
 * Created by kapera on 06-Aug-18.
 */
class LoggedUser {
    companion object {
        var uid = ""
        var username = ""
        var email = ""
        var profilePicPath = ""

        fun clear(){
            uid = ""
            username = ""
            email = ""
            profilePicPath = ""
        }
    }
}