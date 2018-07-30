package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.util.StringUtils
import com.aleksanderkapera.liveback.util.asString
import kotlinx.android.synthetic.main.item_comment.view.*

/**
 * Created by kapera on 28-Jul-18.
 */
class EventCommentsAdapter(context: Context) : BaseRecyclerAdapter<EventCommentsAdapter.ViewHolder, Comment>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        return ViewHolder(mInflater.inflate(R.layout.item_comment, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item:Comment

        private val secToMin = 60
        private val secToHour = 3600
        private val secToDay = 86400
        private val secToWeek = 604800
        private val secToMonth = 2678400
        private val secToYear =  31536000

        override fun bind(position: Int) {
            item = mData[position]

            itemView.eventComment_text_title.text = item.authorName
            itemView.eventComment_text_description.text = item.description
            itemView.eventComment_text_time.text = convertLong(item.postedTime)
        }

        override fun onClick(p0: View?) {
        }

        /**
         * Convert long to desired displayable format
         */
        private fun convertLong(time: Long): String{
            val difference = (System.currentTimeMillis() - time) / 1000

            return when{
                difference >= secToYear -> "${StringUtils.convertLongToDate(difference,"y")} ${R.string.years_short.asString()}"
                difference >= secToMonth -> "${StringUtils.convertLongToDate(difference,"M")} ${R.string.months_short.asString()}"
                difference >= secToWeek -> "${StringUtils.convertLongToDate(difference,"w")} ${R.string.week_short.asString()}"
                difference >= secToDay -> "${StringUtils.convertLongToDate(difference,"d")} ${R.string.days_short.asString()}"
                difference >= secToHour -> "${StringUtils.convertLongToDate(difference,"h")} ${R.string.hours_short.asString()}"
                difference >= secToMin -> "${StringUtils.convertLongToDate(difference,"m")} ${R.string.minutes_short.asString()}"
                else -> "${StringUtils.convertLongToDate(difference,"s")} ${R.string.seconds_short.asString()}"
            }
        }
    }
}