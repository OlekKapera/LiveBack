package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.util.asDrawable
import com.aleksanderkapera.liveback.util.context
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.item_vote.view.*
import kotlin.math.absoluteValue

/**
 * Created by kapera on 28-Jul-18.
 */
class EventVotesAdapter(context: Context) : BaseRecyclerAdapter<EventVotesAdapter.ViewHolder, Vote>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_vote, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Vote
        private lateinit var mStorageRef: StorageReference

        override fun bind(position: Int) {
            item = mData[position]

            itemView.eventVote_text_title.text = item.title
            itemView.eventVote_text_description.text = item.text

            val voteDif = item.upVotes.size - item.downVotes.size
            itemView.eventVote_text_votes.text = when {
                voteDif.absoluteValue >= 1000 -> "${voteDif.div(1000)} k"
                else -> voteDif.toString()
            }

            item.profilePictureUrl.let {
                if (it.isNotEmpty()) {
                    mStorageRef = FirebaseStorage.getInstance().getReference(it)
                    Glide.with(context)
                            .using(FirebaseImageLoader())
                            .load(mStorageRef)
                            .into(itemView.eventVote_image_profile)
                } else
                    itemView.eventVote_image_profile.setImageDrawable(R.drawable.ic_round_user_solid.asDrawable())
            }
        }

        override fun onClick(p0: View?) {
        }
    }
}