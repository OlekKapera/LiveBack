package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.fragment.ProfileFragment
import com.aleksanderkapera.liveback.ui.widget.LoadView
import com.aleksanderkapera.liveback.util.LoggedUser
import com.aleksanderkapera.liveback.util.asColor
import com.aleksanderkapera.liveback.util.asDrawable
import com.aleksanderkapera.liveback.util.asString
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.item_vote.view.*
import kotlin.math.absoluteValue

/**
 * Created by kapera on 28-Jul-18.
 */
class EventVotesAdapter(val context: Context, val eventUid: String, val loader: LoadView) : BaseRecyclerAdapter<EventVotesAdapter.ViewHolder, Vote>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_vote, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Vote
        private lateinit var mStorageRef: StorageReference
        private lateinit var mVoteRef: DocumentReference

        private val mUpVoteString = R.string.up_vote_error.asString()
        private val mDownVoteString = R.string.down_vote_error.asString()
        private val mButtonColorDefault = R.color.darkGrey.asColor()
        private val mButtonColorRed = R.color.red.asColor()

        private var mUpVoted = false
        private var mDownVoted = false
        private var mVoteDif = 0

        override fun bind(position: Int) {
            item = mData[position]
            mVoteRef = FirebaseFirestore.getInstance().document("events/$eventUid/votes/${item.voteUid}")

            itemView.eventVote_text_title.text = item.title
            itemView.eventVote_text_description.text = item.text
            itemView.eventVote_text_votes.text = convertVotesDifference()

            itemView.eventVote_image_profile.setOnClickListener { (context as MainActivity).showFragment(ProfileFragment.newInstance(item.voteAuthorUid)) }

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

            if (item.upVotes.contains(LoggedUser.uid)) {
                mUpVoted = true
                itemView.eventVote_button_upVote.setColorFilter(mButtonColorRed)
            } else if (item.downVotes.contains(LoggedUser.uid)) {
                mDownVoted = true
                itemView.eventVote_button_downVote.setColorFilter(mButtonColorRed)
            }

            itemView.eventVote_button_upVote.setOnClickListener { upVote() }
            itemView.eventVote_button_downVote.setOnClickListener { downVote() }
        }

        override fun onClick(p0: View?) {
            itemView.eventVote_image_profile.setOnClickListener(this)
        }

        /**
         * Convert votes difference to desirable format
         */
        private fun convertVotesDifference(): String {
            mVoteDif = item.upVotes.size - item.downVotes.size
            return when {
                mVoteDif.absoluteValue >= 1000 -> "${mVoteDif.div(1000)} k"
                else -> mVoteDif.toString()
            }
        }

        /**
         * Add or remove user's uid to the up votes list, but first remove it from down votes list
         */
        private fun upVote() {
            loader.show()
            mVoteRef.update("downVotes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                if (it.isSuccessful) {
                    item.downVotes.remove(LoggedUser.uid)
                    mDownVoted = false

                    if (!mUpVoted) {
                        mVoteRef.update("upVotes", FieldValue.arrayUnion(LoggedUser.uid)).addOnCompleteListener {
                            loader.hide()

                            when {
                                it.isSuccessful -> {
                                    mUpVoted = true
                                    item.upVotes.add(LoggedUser.uid)
                                    itemView.eventVote_button_upVote.setColorFilter(mButtonColorRed)
                                    itemView.eventVote_button_downVote.setColorFilter(mButtonColorDefault)
                                    itemView.eventVote_text_votes.text = convertVotesDifference()
                                }
                                else -> {
                                    Toast.makeText(context, mUpVoteString, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        item.upVotes.remove(LoggedUser.uid)
                        mVoteRef.update("upVotes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                            loader.hide()
                            when {
                                it.isSuccessful -> {
                                    mUpVoted = false
                                    item.upVotes.remove(LoggedUser.uid)
                                    itemView.eventVote_button_upVote.setColorFilter(mButtonColorDefault)
                                    itemView.eventVote_text_votes.text = convertVotesDifference()
                                }
                                else -> {
                                    Toast.makeText(context, mUpVoteString, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } else {
                    loader.hide()
                    Toast.makeText(context, mUpVoteString, Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Add or remove user's uid to the down votes list, but first remove it from add votes list
         */
        private fun downVote() {
            loader.show()
            mVoteRef.update("upVotes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                if (it.isSuccessful) {
                    item.upVotes.remove(LoggedUser.uid)
                    mUpVoted = false

                    if (!mDownVoted) {
                        mVoteRef.update("downVotes", FieldValue.arrayUnion(LoggedUser.uid)).addOnCompleteListener {
                            loader.hide()
                            when {
                                it.isSuccessful -> {
                                    mDownVoted = true
                                    item.downVotes.add(LoggedUser.uid)
                                    itemView.eventVote_button_downVote.setColorFilter(mButtonColorRed)
                                    itemView.eventVote_button_upVote.setColorFilter(mButtonColorDefault)
                                    itemView.eventVote_text_votes.text = convertVotesDifference()
                                }
                                else -> {
                                    Toast.makeText(context, mDownVoteString, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        mVoteRef.update("downVotes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                            loader.hide()
                            when {
                                it.isSuccessful -> {
                                    mDownVoted = false
                                    item.downVotes.remove(LoggedUser.uid)
                                    itemView.eventVote_button_downVote.setColorFilter(mButtonColorDefault)
                                    itemView.eventVote_text_votes.text = convertVotesDifference()
                                }
                                else -> {
                                    Toast.makeText(context, mDownVoteString, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } else {
                    loader.hide()
                    Toast.makeText(context, mDownVoteString, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}