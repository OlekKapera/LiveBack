package com.aleksanderkapera.liveback.util

import android.text.format.DateFormat
import java.util.*

/**
 * Created by kapera on 29-May-18.
 */
object StringUtils {

    fun convertLongToDate(timestamp: Long): String{
        return DateFormat.format("dd MMM yyyy", Date(timestamp)).toString()
    }
}