package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.DeleteDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.DeleteDialogType
import com.aleksanderkapera.liveback.ui.fragment.ProfileFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.item_vote.view.*
import kotlin.math.absoluteValue

/**
 * Created by kapera on 28-Jul-18.
 */
class EventVotesAdapter(val context: Context, val eventUid: String, val fragment: BaseFragment) : BaseRecyclerAdapter<EventVotesAdapter.ViewHolder, Vote>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_vote, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Vote
        private lateinit var mStorageRef: StorageReference
        private lateinit var mVoteRef: DocumentReference
        private val mUsersRef = FirebaseFirestore.getInstance().collection("users")

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

            mUsersRef.document(item.voteAuthorUid).get().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result?.toObject(User::class.java)?.let { user ->
                            user.profilePicPath?.let {
                                if (it.isNotEmpty()) {
                                    mStorageRef = FirebaseStorage.getInstance().getReference(it)
                                    Glide.with(context)
                                            .using(FirebaseImageLoader())
                                            .load(mStorageRef)
                                            .signature(StringSignature(user.profilePicTime.toString()))
                                            .into(itemView.eventVote_image_profile)
                                } else
                                    itemView.eventVote_image_profile.setImageDrawable(R.drawable.ic_user_round_solid.asDrawable())
                            }
                        }
                    }
                }
            }

            itemView.eventVote_button_upVote.setColorFilter(mButtonColorDefault)
            itemView.eventVote_button_downVote.setColorFilter(mButtonColorDefault)

            when {
                item.upVotes.contains(LoggedUser.uid) -> {
                    mUpVoted = true
                    itemView.eventVote_button_upVote.setColorFilter(mButtonColorRed)
                }
                item.downVotes.contains(LoggedUser.uid) -> {
                    mDownVoted = true
                    itemView.eventVote_button_downVote.setColorFilter(mButtonColorRed)
                }
            }

            itemView.setOnClickListener {
                val dialog = DeleteDialogFragment.newInstance(DeleteDialogType.VOTE, item.voteUid)
                dialog.setTargetFragment(fragment, REQUEST_TARGET_DELETE_FRAGMENT)
                dialog.show((context as MainActivity).supportFragmentManager, TAG_VOTE_DELETE)
            }

            itemView.eventVote_button_upVote.setOnClickListener { upVote() }
            itemView.eventVote_button_downVote.setOnClickListener { downVote() }
        }

        override fun onClick(p0: View?) {
            itemView.setOnClickListener(this)
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
            fragment.event_view_load.show()
            mVoteRef.update("downVotes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                if (it.isSuccessful) {
                    item.downVotes.remove(LoggedUser.uid)
                    mDownVoted = false

                    if (!mUpVoted) {
                        mVoteRef.update("upVotes", FieldValue.arrayUnion(LoggedUser.uid)).addOnCompleteListener {
                            fragment.event_view_load.hide()

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
                            fragment.event_view_load.hide()
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
                    fragment.event_view_load.hide()
                    Toast.makeText(context, mUpVoteString, Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Add or remove user's uid to the down votes list, but first remove it from add votes list
         */
        private fun downVote() {
            fragment.event_view_load.show()
            mVoteRef.update("upVotes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                if (it.isSuccessful) {
                    item.upVotes.remove(LoggedUser.uid)
                    mUpVoted = false

                    if (!mDownVoted) {
                        mVoteRef.update("downVotes", FieldValue.arrayUnion(LoggedUser.uid)).addOnCompleteListener {
                            fragment.event_view_load.hide()
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
                            fragment.event_view_load.hide()
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
                    fragment.event_view_load.hide()
                    Toast.makeText(context, mDownVoteString, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}