package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.util.StringUtils
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

        init {

        }

        override fun bind(position: Int) {
            item = mData[position]

            itemView.eventComment_text_title.text = item.authorName
            itemView.eventComment_text_description.text = item.description
            itemView.eventComment_text_time.text = StringUtils.convertLongToDate(item.postedTime,"m")
        }

        override fun onClick(p0: View?) {
        }
    }
}