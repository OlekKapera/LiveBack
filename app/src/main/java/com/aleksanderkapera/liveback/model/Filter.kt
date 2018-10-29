package com.aleksanderkapera.liveback.model

import android.os.Parcelable
import com.aleksanderkapera.liveback.ui.fragment.SortType
import com.aleksanderkapera.liveback.util.filterLikesTo
import kotlinx.android.parcel.Parcelize

/**
 * Data class containing filter parameters
 */
@Parcelize
data class Filter(var sortBy: SortType = SortType.DATE,
                  var directionAsc: Boolean = true,
                  var likesFrom: Int = 0,
                  var likesTo: Int = filterLikesTo,
                  var timeFrom: Long = 0L,
                  var timeTo: Long = 0L) : Parcelable