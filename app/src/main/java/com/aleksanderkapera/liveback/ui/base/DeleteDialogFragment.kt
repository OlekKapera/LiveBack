package com.aleksanderkapera.liveback.ui.base

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.activity.AddEventActivity
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.fragment.DeleteDialogType
import com.aleksanderkapera.liveback.ui.fragment.EventFragment
import com.aleksanderkapera.liveback.util.BUNDLE_DELETE_DIALOG
import com.aleksanderkapera.liveback.util.BUNDLE_DELETE_DIALOG_ITEM_ID
import com.aleksanderkapera.liveback.util.asString
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.dialog_fragment_delete.view.*
import kotlinx.android.synthetic.main.fragment_event_comments.*
import kotlinx.android.synthetic.main.fragment_event_vote.*

/**
 * Created by kapera on 28-Aug-18.
 */
class DeleteDialogFragment : DialogFragment() {

    private val deleteEvent = R.string.delete_event.asString()
    private val deleteEventQuestion = R.string.delete_event_question.asString()
    private val deleteEventMessage = R.string.delete_event_message.asString()
    private val deleteComment = R.string.delete_comment.asString()
    private val deleteCommentQuestion = R.string.delete_comment_question.asString()
    private val deleteCommentMessage = R.string.delete_comment_message.asString()
    private val deleteVote = R.string.delete_vote.asString()
    private val deleteVoteQuestion = R.string.delete_vote_question.asString()
    private val deleteVoteMessage = R.string.delete_vote_message.asString()
    private val deleteEventSuccess = R.string.delete_event_successful.asString()
    private val deleteCommentSuccess = R.string.delete_comment_successful.asString()
    private val deleteVoteSuccess = R.string.delete_vote_successful.asString()
    private val deleteEventFail = R.string.delete_event_error.asString()
    private val deleteCommentFail = R.string.delete_comment_error.asString()
    private val deleteVoteFail = R.string.delete_vote_error.asString()

    private lateinit var mType: DeleteDialogType
    private lateinit var mEvent: Event
    private var mItemId = ""
    private var mCommentsDone = false
    private var mVotesDone = false
    private lateinit var rootView: View

    private lateinit var mEventDocRef: DocumentReference
    private lateinit var mDocumentRef: DocumentReference
    private lateinit var mStorageRef: StorageReference
    private lateinit var mBatch: WriteBatch

    companion object {
        fun newInstance(type: DeleteDialogType, itemUid: String?): DeleteDialogFragment {
            val fragment = DeleteDialogFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_DELETE_DIALOG, type)
            bundle.putString(BUNDLE_DELETE_DIALOG_ITEM_ID, itemUid)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onResume() {
        val params = dialog.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.dialog_fragment_delete, container, false)

        arguments?.let {
            mType = it.getSerializable(BUNDLE_DELETE_DIALOG) as DeleteDialogType
            mItemId = it.getString(BUNDLE_DELETE_DIALOG_ITEM_ID) ?: ""
        }

        (activity as? AddEventActivity)?.mEvent?.let { mEvent = it }
        (targetFragment as? EventFragment)?.event?.let { mEvent = it }

        mBatch = FirebaseFirestore.getInstance().batch()
        mStorageRef = FirebaseStorage.getInstance().reference
        mEventDocRef = FirebaseFirestore.getInstance().document("events/${mEvent.eventUid}")

        when (mType) {
            DeleteDialogType.EVENT -> {
                rootView.deleteDialog_text_title.text = deleteEventQuestion
                rootView.deleteDialog_text_message.text = deleteEventMessage
                rootView.deleteDialog_button_positive.text = deleteEvent
            }

            DeleteDialogType.COMMENT -> {
                rootView.deleteDialog_text_title.text = deleteCommentQuestion
                rootView.deleteDialog_text_message.text = deleteCommentMessage
                rootView.deleteDialog_button_positive.text = deleteComment

                mDocumentRef = FirebaseFirestore.getInstance().document("events/${mEvent.eventUid}/comments/$mItemId")
            }

            DeleteDialogType.VOTE -> {
                rootView.deleteDialog_text_title.text = deleteVoteQuestion
                rootView.deleteDialog_text_message.text = deleteVoteMessage
                rootView.deleteDialog_button_positive.text = deleteVote

                mDocumentRef = FirebaseFirestore.getInstance().document("events/${mEvent.eventUid}/votes/$mItemId")
            }
        }

        rootView.deleteDialog_button_positive.setOnClickListener { positiveButtonClick() }
        rootView.deleteDialog_button_negative.setOnClickListener { dismiss() }

        return rootView
    }

    /**
     * Accordingly to dialog type delete particular item
     */
    private fun positiveButtonClick() {
        rootView.deleteDialog_view_load.show()
        rootView.deleteDialog_view_load.visibility = View.VISIBLE
        when (mType) {
            DeleteDialogType.EVENT -> deleteImage()
            DeleteDialogType.COMMENT -> deleteComment()
            DeleteDialogType.VOTE -> deleteVote()
        }
    }

    /**
     * Delete image of an event from cloud
     */
    private fun deleteImage() {
        if (mEvent.backgroundPicturePath.isNotEmpty())
            mStorageRef.child(mEvent.backgroundPicturePath).delete().addOnCompleteListener {
                when {
                    it.isSuccessful -> deleteEvent()
                    else -> {
                        Toast.makeText(context, deleteEventFail, Toast.LENGTH_SHORT).show()
                        rootView.deleteDialog_view_load.hide()
                    }
                }
            }
        else
            deleteEvent()
    }

    /**
     * Delete event and all its comments and votes
     */
    private fun deleteEvent() {
        mBatch.delete(mEventDocRef)

        mEventDocRef.collection("comments").get().addOnCompleteListener { commentTask ->
            when {
                commentTask.isSuccessful -> {
                    commentTask.result?.let { result ->
                        result.documents.forEach {
                            mBatch.delete(it.reference)

                            if (it == result.documents.last()) {
                                mCommentsDone = true
                                commitBatch()
                            }
                        }

                        if (result.documents.size == 0) {
                            mCommentsDone = true
                            commitBatch()
                        }
                    }
                }
            }
        }

        mEventDocRef.collection("votes").get().addOnCompleteListener { votesTask ->
            when {
                votesTask.isSuccessful -> {
                    votesTask.result?.let { result ->
                        result.documents.forEach {
                            mBatch.delete(it.reference)

                            if (it == result.documents.last()) {
                                mVotesDone = true
                                commitBatch()
                            }
                        }

                        if (result.documents.size == 0) {
                            mVotesDone = true
                            commitBatch()
                        }
                    }
                }
            }
        }
    }

    /**
     * Commit batch changes when comments and votes tasks have been finalized
     */
    private fun commitBatch() {
        if (mCommentsDone && mVotesDone)
            mBatch.commit().addOnCompleteListener {
                when {
                    it.isSuccessful -> Toast.makeText(context, deleteEventSuccess, Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context, deleteEventFail, Toast.LENGTH_SHORT).show()
                }
                MainActivity.startActivity(activity as Activity, false)
                rootView.deleteDialog_view_load.hide()
            }
    }

    /**
     * Delete comment and update comments counter of an event
     */
    private fun deleteComment() {
        mBatch.delete(mDocumentRef)
        mBatch.update(mEventDocRef, "comments", mEvent.comments - 1)

        mBatch.commit().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    Toast.makeText(context, deleteCommentSuccess, Toast.LENGTH_SHORT).show()
                    mEvent.comments--

                    val comments = (targetFragment as EventFragment).commentFragment.comments as MutableList<Comment>
                    for (index in 0 until comments.size) {
                        if (comments[index].commentUid == mItemId) {
                            comments.removeAt(index)
                            break
                        }
                    }

                    (targetFragment as EventFragment).commentFragment.commentsAdapter.replaceData(comments)
                    (targetFragment as EventFragment).switchEmptyView(comments as MutableList<Any>, (targetFragment as EventFragment).eventComment_recycler_comments, (targetFragment as EventFragment).eventComment_view_emptyScreen)
                }
                else -> Toast.makeText(context, deleteCommentFail, Toast.LENGTH_SHORT).show()
            }
            dismiss()
            rootView.deleteDialog_view_load.hide()
        }
    }

    /**
     * Delete vote and update votes counter of an event
     */
    private fun deleteVote() {
        mBatch.delete(mDocumentRef)
        mBatch.update(mEventDocRef, "votes", mEvent.votes - 1)

        mBatch.commit().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    Toast.makeText(context, deleteVoteSuccess, Toast.LENGTH_SHORT).show()
                    mEvent.votes--

                    val votes = (targetFragment as EventFragment).votesFragment.votes as MutableList<Vote>
                    for (index in 0 until votes.size) {
                        if (votes[index].voteUid == mItemId) {
                            votes.removeAt(index)
                            break
                        }
                    }

                    (targetFragment as EventFragment).votesFragment.votesAdapter?.replaceData(votes)
                    (targetFragment as EventFragment).switchEmptyView(votes as MutableList<Any>, (targetFragment as EventFragment).eventVote_recycler_votes, (targetFragment as EventFragment).eventVote_view_emptyScreen)
                }
                else -> Toast.makeText(context, deleteVoteFail, Toast.LENGTH_SHORT).show()
            }
            dismiss()
            rootView.deleteDialog_view_load.hide()
        }
    }
}