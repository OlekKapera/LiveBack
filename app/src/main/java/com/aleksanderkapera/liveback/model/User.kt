package com.aleksanderkapera.liveback.model

/**
 * Created by kapera on 26-Jun-18.
 */

data class User(val name: String = "",
                val email: String = "") {
    constructor() : this("", "")
}