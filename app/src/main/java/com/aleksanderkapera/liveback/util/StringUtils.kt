package com.aleksanderkapera.liveback.util

import android.text.format.DateFormat
import com.aleksanderkapera.liveback.R
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

private val secToMin = 60
private val secToHour = 3600
private val secToDay = 86400
private val secToWeek = 604800
private val secToMonth = 2629743
private val secToYear =  31556926

fun longToStringAgo(time: Long): String{
    val difference = (System.currentTimeMillis() - time) / 1000

    return when{
        difference >= secToYear -> "${difference.div(secToYear)} ${R.string.years_short.asString()}"
        difference >= secToMonth -> "${difference.div(secToMonth)} ${R.string.months_short.asString()}"
        difference >= secToWeek -> "${difference.div(secToWeek)} ${R.string.week_short.asString()}"
        difference >= secToDay -> "${difference.div(secToDay)} ${R.string.days_short.asString()}"
        difference >= secToHour -> "${difference.div(secToHour)} ${R.string.hours_short.asString()}"
        difference >= secToMin -> "${difference.div(secToMin)} ${R.string.minutes_short.asString()}"
        else -> "$difference ${R.string.seconds_short.asString()}"
    }
}
