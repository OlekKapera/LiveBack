package com.aleksanderkapera.liveback.ui.base

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.fragment.DeleteDialogType
import com.aleksanderkapera.liveback.util.BUNDLE_DELETE_DIALOG
import com.aleksanderkapera.liveback.util.BUNDLE_DELETE_DIALOG_EVENT
import com.aleksanderkapera.liveback.util.BUNDLE_DELETE_DIALOG_ITEM_ID
import com.aleksanderkapera.liveback.util.asString
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.dialog_fragment_delete.view.*

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

    private lateinit var mDocumentRef: DocumentReference
    private lateinit var mStorageRef: StorageReference
    private lateinit var mBatch: WriteBatch

    companion object {
        fun newInstance(type: DeleteDialogType, event: Event, itemUid: String?): DeleteDialogFragment {
            val fragment = DeleteDialogFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_DELETE_DIALOG, type)
            bundle.putSerializable(BUNDLE_DELETE_DIALOG_EVENT, event)
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
        rootView = inflater.inflate(R.layout.dialog_fragment_delete, null)

        arguments?.let {
            mType = it.getSerializable(BUNDLE_DELETE_DIALOG) as DeleteDialogType
            mEvent = it.getSerializable(BUNDLE_DELETE_DIALOG_EVENT) as Event
            mItemId = it.getString(BUNDLE_DELETE_DIALOG_ITEM_ID) ?: ""
        }

        mBatch = FirebaseFirestore.getInstance().batch()
        mStorageRef = FirebaseStorage.getInstance().reference

        when (mType) {
            DeleteDialogType.EVENT -> {
                rootView.deleteDialog_text_title.text = deleteEventQuestion
                rootView.deleteDialog_text_message.text = deleteEventMessage
                rootView.deleteDialog_button_positive.text = deleteEvent

                mDocumentRef = FirebaseFirestore.getInstance().document("events/${mEvent.eventUid}")
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
        when (mType) {
            DeleteDialogType.EVENT -> deleteImage()
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
        mBatch.delete(mDocumentRef)

        mDocumentRef.collection("comments").get().addOnCompleteListener { commentTask ->
            when {
                commentTask.isSuccessful -> {
                    commentTask.result.documents.forEach {
                        mBatch.delete(it.reference)

                        if (it == commentTask.result.documents.last()) {
                            mCommentsDone = true
                            commitBatch()
                        }
                    }
                }
            }
        }

        mDocumentRef.collection("votes").get().addOnCompleteListener { votesTask ->
            when {
                votesTask.isSuccessful -> {
                    votesTask.result.documents.forEach {
                        mBatch.delete(it.reference)

                        if (it == votesTask.result.documents.last()) {
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
                MainActivity.startActivity(activity as Activity,false)
                rootView.deleteDialog_view_load.hide()
            }
    }
}