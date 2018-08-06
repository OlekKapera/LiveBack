package com.aleksanderkapera.liveback.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kapera on 29-May-18.
 */

fun convertLongToDate(timestamp: Long): String {
    return DateFormat.format("dd MMM yyyy", Date(timestamp)).toString()
}

fun convertLongToDate(timestamp: Long, dateFormat: String): String {
    return DateFormat.format(dateFormat, Date(timestamp)).toString()
}

fun convertStringToLongTime(text: String): Long{
    return convertStringToLongTime(text, "d.M.yyyy HH:mm")
}

fun convertStringToLongTime(text: String, dateFormat: String): Long{
    val format = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    return format.parse(text).time
}
