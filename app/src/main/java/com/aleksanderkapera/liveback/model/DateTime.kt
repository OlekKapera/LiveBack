package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data class containing parsed values of data and time
 */
@Parcelize
data class DateTime(var year: Int = 0,
                    var month: Int = 0,
                    var day: Int = 0,
                    var hour: String = "0",
                    var minute: String = "0"): Parcelable