package com.aleksanderkapera.liveback.model

/**
 * Created by kapera on 26-Jun-18.
 */

data class User(val uid : String = "",
                val username: String = "",
                val email: String = "") {
    constructor() : this("", "","")
}