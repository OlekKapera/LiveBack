package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.SimpleEvent
import com.aleksanderkapera.liveback.ui.widget.RoundedTopImageView
import com.aleksanderkapera.liveback.util.StringUtils
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by kapera on 29-May-18.
 */
class EventsRecyclerAdapter(context: Context) : BaseRecyclerAdapter<EventsRecyclerAdapter.ViewHolder, SimpleEvent>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.card_main, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        // @format:off
        @BindView(R.id.cardMain_image_background) private lateinit var mBackgroundImage:RoundedTopImageView
        @BindView(R.id.cardMain_image_profile) private lateinit var mProfileImage:CircleImageView
        @BindView(R.id.cardMain_text_name) private lateinit var mNameText:TextView
        @BindView(R.id.cardMain_text_date) private lateinit var mDateText:TextView
        @BindView(R.id.cardMain_text_title) private lateinit var mTitleText:TextView
        @BindView(R.id.cardMain_text_description) private lateinit var mDescriptionText:TextView
        @BindView(R.id.cardMain_text_category) private lateinit var mCategoryText:TextView
        @BindView(R.id.cardMain_text_favourite) private lateinit var mFavouriteText:TextView
        @BindView(R.id.cardMain_text_feedback) private lateinit var mFeedbackText:TextView
        @BindView(R.id.cardMain_text_vote) private lateinit var mVoteText:TextView
        // @format:on

        private lateinit var item: SimpleEvent

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(position: Int) {
            item = mData[position]

            mNameText.text = item.userName
            mDateText.text = StringUtils.convertLongToDate(item.date)
            mTitleText.text = item.title
            mDescriptionText.text = item.description
            mCategoryText.text = item.category
            mFavouriteText.text = item.favourites.toString()
            mFeedbackText.text = item.feedback.toString()
            mVoteText.text = item.votes.toString()
        }

        override fun onClick(view: View?) {
        }
    }
}