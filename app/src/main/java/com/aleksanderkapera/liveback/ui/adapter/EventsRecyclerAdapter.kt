package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.fragment.EventFragment
import com.aleksanderkapera.liveback.util.asDrawable
import com.aleksanderkapera.liveback.util.convertLongToDate
import com.aleksanderkapera.liveback.util.setBackgroundCategory
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.card_main.view.*

/**
 * Created by kapera on 29-May-18.
 */
class EventsRecyclerAdapter(val context: Context) : BaseRecyclerAdapter<EventsRecyclerAdapter.ViewHolder, Event>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.card_main, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerAdapter.ViewHolder(itemView) {

        private lateinit var item: Event
        private lateinit var mUser: User
        private lateinit var mStorageRef: StorageReference
        private val mUsersRef = FirebaseFirestore.getInstance().collection("users")

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(position: Int) {
            item = mData[position]

            mUsersRef.document(item.userUid).get().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result?.toObject(User::class.java)?.let { user ->
                            itemView.cardMain_text_name.text = user.username
                            mUser = user

                            if (user.profilePicPath.isNotEmpty()) {
                                mStorageRef = FirebaseStorage.getInstance().getReference(user.profilePicPath)
                                Glide.with(context)
                                        .using(FirebaseImageLoader())
                                        .load(mStorageRef)
                                        .signature(StringSignature(user.profilePicTime.toString()))
                                        .into(itemView.cardMain_image_profile)
                            } else
                                itemView.cardMain_image_profile.setImageDrawable(R.drawable.ic_user_round_solid.asDrawable())
                        }
                    }
                }
            }

            itemView.cardMain_text_date.text = convertLongToDate(item.date)
            itemView.cardMain_text_title.text = item.title
            itemView.cardMain_text_description.text = item.description
            itemView.cardMain_text_category.text = item.category
            itemView.cardMain_text_favourite.text = item.likes.size.toString()
            itemView.cardMain_text_feedback.text = item.comments.toString()
            itemView.cardMain_text_vote.text = item.votes.toString()

            if (item.backgroundPicturePath.isNotEmpty()) {
                mStorageRef = FirebaseStorage.getInstance().getReference(item.backgroundPicturePath)
                Glide.with(context)
                        .using(FirebaseImageLoader())
                        .load(mStorageRef)
                        .signature(StringSignature(item.backgroundPictureTime.toString()))
                        .into(itemView.cardMain_image_background)
            } else {
                setBackgroundCategory(item.category, itemView.cardMain_image_background)
            }
        }

        override fun onClick(view: View?) {
            (context as MainActivity).showFragment(EventFragment.newInstance(item, mUser))
        }
    }
}