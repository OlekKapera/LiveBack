package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.util.ImageUtils
import com.aleksanderkapera.liveback.util.StringUtils
import kotlinx.android.synthetic.main.card_main.view.*

/**
 * Created by kapera on 29-May-18.
 */
class EventsRecyclerAdapter(context: Context) : BaseRecyclerAdapter<EventsRecyclerAdapter.ViewHolder, Event>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.card_main, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Event

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(position: Int) {
            item = mData[position]

            itemView.cardMain_text_name.text = item.userUid
            itemView.cardMain_text_date.text = StringUtils.convertLongToDate(item.date)
            itemView.cardMain_text_title.text = item.title
            itemView.cardMain_text_description.text = item.description
            itemView.cardMain_text_category.text = item.category
            itemView.cardMain_text_favourite.text = item.likes.toString()
            itemView.cardMain_text_feedback.text = item.comments.toString()
            itemView.cardMain_text_vote.text = item.votes.toString()

            itemView.cardMain_image_background.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(mRes, R.drawable.green_nature, 500, 150))
            itemView.cardMain_image_profile.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(mRes, R.drawable.mari_profile, 75, 75))
        }

        override fun onClick(view: View?) {
        }
    }
}