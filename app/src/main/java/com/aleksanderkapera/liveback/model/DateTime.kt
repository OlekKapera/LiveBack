package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import com.aleksanderkapera.liveback.util.convertStringToLongTime
import kotlinx.android.parcel.Parcelize

/**
 * Data class containing parsed values of data and time
 */
@Parcelize
data class DateTime(var year: Int = 0,
                    var month: Int = 0,
                    var day: Int = 0,
                    var hour: String = "0",
                    var minute: String = "0"): Parcelable {

    /**
     * Convert to long timestamp
     */
    fun getLong(): Long{
        val unitedString = "$day.$month.$year $hour:$minute"
        return convertStringToLongTime(unitedString)
    }
}