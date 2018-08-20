package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.fragment.ProfileFragment
import com.aleksanderkapera.liveback.util.asDrawable
import com.aleksanderkapera.liveback.util.context
import com.aleksanderkapera.liveback.util.longToStringAgo
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.item_comment.view.*

/**
 * Created by kapera on 28-Jul-18.
 */
class EventCommentsAdapter(val context: Context) : BaseRecyclerAdapter<EventCommentsAdapter.ViewHolder, Comment>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_comment, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Comment
        private lateinit var mStorageRef: StorageReference

        override fun bind(position: Int) {
            item = mData[position]

            itemView.eventComment_text_title.text = item.authorName
            itemView.eventComment_text_description.text = item.description
            itemView.eventComment_text_time.text = longToStringAgo(item.postedTime)

            itemView.eventComment_image_profile.setOnClickListener { (context as MainActivity).showFragment(ProfileFragment.newInstance(item.commentAuthorUid)) }

            item.profilePictureUrl.let {
                if (it.isNotEmpty()) {
                    mStorageRef = FirebaseStorage.getInstance().getReference(it)
                    Glide.with(context)
                            .using(FirebaseImageLoader())
                            .load(mStorageRef)
                            .into(itemView.eventComment_image_profile)
                } else
                    itemView.eventComment_image_profile.setImageDrawable(R.drawable.ic_round_user_solid.asDrawable())
            }
        }

        override fun onClick(p0: View?) {
            itemView.eventComment_image_profile.setOnClickListener(this)
        }
    }
}