package com.aleksanderkapera.liveback.model

import com.aleksanderkapera.liveback.rest.model.IGNORE_UNKNOWN
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Created by kapera on 23-May-18.
 */

@JsonIgnoreProperties(ignoreUnknown = IGNORE_UNKNOWN)
data class SimpleEvent(val backgroundUuid: String,
                       val profileUuid: String,
                       val userName: String,
                       val date: Long,
                       val title: String,
                       val description: String,
                       val category: String,
                       val favourites: Int,
                       val feedback: Int,
                       val votes: Int)

