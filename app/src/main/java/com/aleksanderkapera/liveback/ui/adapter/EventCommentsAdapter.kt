package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.fragment.AddFeedbackDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.AddFeedbackDialogType
import com.aleksanderkapera.liveback.ui.fragment.EventFragment
import com.aleksanderkapera.liveback.ui.fragment.ProfileFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.item_comment.view.*

/**
 * Created by kapera on 28-Jul-18.
 */
class EventCommentsAdapter(val context: Context, val fragment: BaseFragment) : BaseRecyclerAdapter<EventCommentsAdapter.ViewHolder, Comment>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_comment, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Comment
        private lateinit var mUser: User
        private lateinit var mStorageRef: StorageReference
        private val mUsersRef: CollectionReference = FirebaseFirestore.getInstance().collection("users")

        override fun bind(position: Int) {
            item = mData[position]

            mUsersRef.document(item.commentAuthorUid).get().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result?.toObject(User::class.java)?.let { user ->
                            mUser = user
                            itemView.eventComment_text_title.text = user.username
                            if (user.profilePicPath.isNotEmpty()) {
                                mStorageRef = FirebaseStorage.getInstance().getReference(user.profilePicPath)
                                GlideApp.with(context)
                                        .load(mStorageRef)
                                        .signature(ObjectKey(user.profilePicTime.toString()))
                                        .displayRoundPlaceholder()
                                        .into(itemView.eventComment_image_profile)
                            } else
                                itemView.eventComment_image_profile.setImageDrawable(R.drawable.ic_user_round_solid.asDrawable())
                        }
                    }
                }
            }

            itemView.eventComment_text_description.text = item.description
            itemView.eventComment_text_time.text = longToStringAgo(item.postedTime)

            itemView.setOnClickListener {
                safeLet(LoggedUser, mUser) { loggedUser, user ->
                    if (fragment is EventFragment && loggedUser.uid == user.uid) {
                        val dialog = AddFeedbackDialogFragment.newInstance(AddFeedbackDialogType.COMMENT, item, null)
                        dialog.setTargetFragment(fragment, REQUEST_TARGET_EVENT_FRAGMENT)
                        dialog.show((context as MainActivity).supportFragmentManager, TAG_ADD_FEEDBACK_COMMENT_FILLED)
                    }
                }
            }
            itemView.eventComment_image_profile.setOnClickListener { (context as MainActivity).showFragment(ProfileFragment.newInstance(item.commentAuthorUid)) }
        }

        override fun onClick(p0: View?) {
            itemView.setOnClickListener(this)
            itemView.eventComment_image_profile.setOnClickListener(this)
        }
    }
}