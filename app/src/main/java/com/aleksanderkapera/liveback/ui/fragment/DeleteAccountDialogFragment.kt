package com.aleksanderkapera.liveback.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.util.LoggedUser
import com.aleksanderkapera.liveback.util.asString
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.dialog_fragment_delete_account.*
import kotlinx.android.synthetic.main.dialog_fragment_delete_account.view.*

/**
 * Created by kapera on 25-Aug-18.
 */
class DeleteAccountDialogFragment : DialogFragment() {

    private val deleteSuccessful = R.string.delete_account_successful.asString()
    private val deleteError = R.string.delete_account_error.asString()
    private val requiredField = R.string.required_field.asString()
    private val incorrectPassword = R.string.incorrect_password.asString()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUserDoc: DocumentReference
    private lateinit var mFireStoreRef: FirebaseFirestore
    private lateinit var mStorageRef: FirebaseStorage
    private lateinit var mBatch: WriteBatch

    private lateinit var rootView: View
    private lateinit var mActivity: Activity
    private var mCommentsDone = false
    private var mVotesDone = false
    private var mCommentEventIndex = 0
    private var mVoteEventIndex = 0

    companion object {
        fun newInstance(): DeleteAccountDialogFragment = DeleteAccountDialogFragment()
    }

    override fun onResume() {
        val params = dialog.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.dialog_fragment_delete_account, null)

        mAuth = FirebaseAuth.getInstance()
        mUserDoc = FirebaseFirestore.getInstance().document("users/${LoggedUser.uid}")
        mFireStoreRef = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance()
        mBatch = mFireStoreRef.batch()

        rootView.deleteAccount_button_positive.setOnClickListener { positiveButtonClick() }
        rootView.deleteAccount_button_negative.setOnClickListener { dismiss() }

        return rootView
    }

    override fun onAttach(activity: Activity?) {
        activity?.let {
            mActivity = it
        }
        super.onAttach(activity)
    }

    /**
     * Deletes user's account all his events, comments and votes he posted
     */
    private fun positiveButtonClick() {
        reauthenticateAuth()
    }

    /**
     * Reauthenticates user so he can be removed from server
     */
    private fun reauthenticateAuth() {
        if (deleteAccount_input_password.text.isEmpty())
            deleteAccount_layout_password.error = requiredField
        else {
            rootView.deleteAccount_view_load.show()
            val credentials = EmailAuthProvider.getCredential(LoggedUser.email, deleteAccount_input_password.text.toString())
            mAuth.currentUser?.reauthenticate(credentials)?.addOnCompleteListener {
                when {
                    it.isSuccessful -> executeDelete()
                    else -> {
                        deleteAccount_layout_password.error = incorrectPassword
                        rootView.deleteAccount_view_load.hide()
                    }
                }
            }
        }
    }

    /**
     * Deletes firebase user auth
     */
    private fun deleteAuth() {
        mAuth.currentUser?.delete()?.addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    Toast.makeText(context, deleteSuccessful, Toast.LENGTH_SHORT).show()
                    LoggedUser.clear()
                    mAuth.signOut()
                    MainActivity.startActivity(mActivity, false)
                }
                else -> Toast.makeText(context, deleteError, Toast.LENGTH_SHORT).show()
            }
            rootView.deleteAccount_view_load.hide()
        }
    }

    /**
     * Deletes image with specified path
     */
    private fun deleteImage(path: String) {
        mStorageRef.getReference(path).delete()
    }

    /**
     * Deletes user's events and all its subcollections
     */
    private fun executeDelete() {
        mBatch.delete(mUserDoc)
        if (LoggedUser.profilePicPath.isNotEmpty())
            deleteImage(LoggedUser.profilePicPath)

        mFireStoreRef.collection("events").get().addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    val eventsSize = task.result.documents.size
                    task.result.documents.forEachIndexed { _, event ->

                        if (event.get("userUid") == LoggedUser.uid) {
                            // it's logged user's event, delete event
                            mBatch.delete(event.reference)
                            if (event.get("backgroundPicturePath").toString().isNotEmpty())
                                deleteImage(event.get("backgroundPicturePath").toString())
                        }

                        // delete like
                        if ((event.get("likes") as List<String>).contains(LoggedUser.uid) && event.get("userUid") != LoggedUser.uid)
                            mBatch.update(event.reference, "likes", FieldValue.arrayRemove(LoggedUser.uid))

                        event.reference.collection("comments").get().addOnCompleteListener { commentTask ->
                            when {
                                commentTask.isSuccessful -> {
                                    if (mCommentEventIndex == eventsSize - 1 && commentTask.result.size() == 0) {
                                        mCommentsDone = true
                                        commitBatch()
                                    }

                                    val commentsSize = commentTask.result.documents.size
                                    var eventComments = event.get("comments") as Long

                                    commentTask.result.documents.forEachIndexed { commentIndex, comment ->
                                        if (event.get("userUid") == LoggedUser.uid) {
                                            // it's logged user's event, delete comment
                                            mBatch.delete(comment.reference)
                                        } else if (comment.get("commentAuthorUid") == LoggedUser.uid) {
                                            // it's comment placed by user, delete it and deduct one from comments counter
                                            mBatch.delete(comment.reference)
                                            eventComments--
                                            mBatch.update(event.reference, "comments", eventComments)
                                        }

                                        if (mCommentEventIndex == eventsSize - 1 && commentIndex == commentsSize - 1) {
                                            mCommentsDone = true
                                            commitBatch()
                                        }
                                    }

                                    mCommentEventIndex++
                                }
                            }
                        }

                        event.reference.collection("votes").get().addOnCompleteListener { votesTask ->
                            when {
                                votesTask.isSuccessful -> {
                                    if (mVoteEventIndex == eventsSize - 1 && votesTask.result.size() == 0) {
                                        mVotesDone = true
                                        commitBatch()
                                    }
                                    val votesSize = votesTask.result.documents.size
                                    var eventVotes = event.get("votes") as Long

                                    votesTask.result.documents.forEachIndexed { voteIndex, vote ->
                                        when {
                                            event.get("userUid") == LoggedUser.uid -> mBatch.delete(vote.reference)
                                            vote.get("voteAuthorUid") == LoggedUser.uid -> {
                                                mBatch.delete(vote.reference)
                                                eventVotes--
                                                mBatch.update(event.reference, "votes", eventVotes)
                                            }
                                            (vote.get("downVotes") as List<String>).contains(LoggedUser.uid) -> mBatch.update(vote.reference, "downVotes", FieldValue.arrayRemove(LoggedUser.uid))
                                            (vote.get("upVotes") as List<String>).contains(LoggedUser.uid) -> mBatch.update(vote.reference, "upVotes", FieldValue.arrayRemove(LoggedUser.uid))
                                        }

                                        if (mVoteEventIndex == eventsSize - 1 && voteIndex == votesSize - 1) {
                                            mVotesDone = true
                                            commitBatch()
                                        }
                                    }

                                    mVoteEventIndex++
                                }
                            }
                        }
                    }
                }
                else -> {
                    Toast.makeText(context, deleteError, Toast.LENGTH_SHORT).show()
                    rootView.deleteAccount_view_load.hide()
                }
            }
        }
    }

    /**
     * Commit changes after everything was added to batch
     */
    private fun commitBatch() {
        if (mCommentsDone && mVotesDone) {
            mBatch.commit().addOnCompleteListener {
                when {
                    it.isSuccessful -> deleteAuth()
                    else -> {
                        Toast.makeText(context, deleteError, Toast.LENGTH_SHORT).show()
                        rootView.deleteAccount_view_load.hide()
                    }
                }
            }
        }
    }
}