package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.InputType
import android.text.method.TextKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.base.DeleteDialogFragment
import com.aleksanderkapera.liveback.util.*
import kotlinx.android.synthetic.main.dialog_fragment_add_feedback.view.*

/**
 * Created by kapera on 08-Aug-18.
 */
class AddFeedbackDialogFragment : DialogFragment() {

    private val mDeleteCommentString = R.string.delete_comment.asString()
    private val mDeleteVoteString = R.string.delete_vote.asString()
    private val mReviewString = R.string.review.asString()
    private val mDescriptionString = R.string.description.asString()
    private val mRequiredField = R.string.required_field.asString()
    private val mAddComment = R.string.add_comment.asString()
    private val mAddVote = R.string.add_vote.asString()
    private val mEditComment = R.string.edit_comment.asString()
    private val mEditVote = R.string.edit_vote.asString()

    private lateinit var mDialogType: AddFeedbackDialogType
    private lateinit var mComment: Comment
    private lateinit var mVote: Vote

    private lateinit var mRootView: View
    private lateinit var mFeedbackSentListener: FeedbackSentListener

    companion object {
        fun newInstance(dialogType: AddFeedbackDialogType, comment: Comment?, vote: Vote?): AddFeedbackDialogFragment {
            val fragment = AddFeedbackDialogFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_EVENT_DIALOG_TYPE, dialogType)
            bundle.putSerializable(BUNDLE_EVENT_DIALOG_COMMENT, comment)
            bundle.putSerializable(BUNDLE_EVENT_DIALOG_VOTE, vote)
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
        mRootView = inflater.inflate(R.layout.dialog_fragment_add_feedback, null)

        mFeedbackSentListener = targetFragment as FeedbackSentListener

        arguments?.let {
            mDialogType = it.get(BUNDLE_EVENT_DIALOG_TYPE) as AddFeedbackDialogType
            mComment = it.get(BUNDLE_EVENT_DIALOG_COMMENT) as? Comment ?: Comment()
            mVote = it.get(BUNDLE_EVENT_DIALOG_VOTE) as? Vote ?: Vote()
        }

        setupViews()

        // set positive button action to validate fields and then send retrieved data further
        mRootView.feedbackDialog_button_positive.setOnClickListener {
            if (mRootView.feedbackDialog_input_subject.text.toString().isEmpty() && mDialogType == AddFeedbackDialogType.VOTE)
                mRootView.feedbackDialog_inputLayout_subject.error = mRequiredField
            else if (mRootView.feedbackDialog_input_description.text.toString().isEmpty())
                mRootView.feedbackDialog_inputLayout_description.error = mRequiredField
            else {
                when (mDialogType) {
                    AddFeedbackDialogType.COMMENT -> {
                        mComment.description = mRootView.feedbackDialog_input_description.text.toString()
                        mComment.postedTime = System.currentTimeMillis()
                        mComment.commentAuthorUid = LoggedUser.uid
                        mFeedbackSentListener.positiveButtonClicked(mComment, null)
                    }
                    AddFeedbackDialogType.VOTE -> {
                        mVote.title = mRootView.feedbackDialog_input_subject.text.toString()
                        mVote.text = mRootView.feedbackDialog_input_description.text.toString()
                        mVote.voteAuthorUid = LoggedUser.uid
                        mFeedbackSentListener.positiveButtonClicked(null, mVote)
                    }
                }
                dismiss()
            }
        }

        //set negative button to dismiss dialog
        mRootView.feedbackDialog_button_negative.setOnClickListener { dismiss() }

        //set delete button to open delete dialog
        when (mDialogType) {
            AddFeedbackDialogType.COMMENT -> mRootView.feedbackDialog_button_delete.setOnClickListener {
                val dialog = DeleteDialogFragment.newInstance(DeleteDialogType.COMMENT, mComment.commentUid)
                dialog.setTargetFragment(targetFragment, REQUEST_TARGET_DELETE_FRAGMENT)
                dialog.show(fragmentManager, TAG_COMMENT_DELETE)
                dismiss()
            }

            AddFeedbackDialogType.VOTE -> mRootView.feedbackDialog_button_delete.setOnClickListener {
                val dialog = DeleteDialogFragment.newInstance(DeleteDialogType.VOTE, mVote.voteUid)
                dialog.setTargetFragment(targetFragment, REQUEST_TARGET_DELETE_FRAGMENT)
                dialog.show(fragmentManager, TAG_VOTE_DELETE)
                dismiss()
            }
        }

        // open soft keyboard when dialog is displayed
        mRootView.feedbackDialog_input_subject.setOnFocusChangeListener { _, b ->
            if (b)
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        // open soft keyboard when dialog is displayed
        mRootView.feedbackDialog_input_description.setOnFocusChangeListener { _, b ->
            if (b)
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        return mRootView
    }

    /**
     * Fill views with data and adjust layout
     */
    private fun setupViews() {
        // set keyboard button to next and capitalize sentence
        mRootView.feedbackDialog_input_subject.imeOptions = EditorInfo.IME_ACTION_NEXT
        mRootView.feedbackDialog_input_subject.setRawInputType(InputType.TYPE_CLASS_TEXT)
        mRootView.feedbackDialog_input_subject.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        mRootView.feedbackDialog_input_subject.setHorizontallyScrolling(false)
        mRootView.feedbackDialog_input_subject.maxLines = 3
        mRootView.feedbackDialog_input_subject.keyListener = TextKeyListener(TextKeyListener.Capitalize.SENTENCES, true)

        // set keyboard button to done and capitalize sentence
        mRootView.feedbackDialog_input_description.imeOptions = EditorInfo.IME_ACTION_DONE
        mRootView.feedbackDialog_input_description.setRawInputType(InputType.TYPE_CLASS_TEXT)
        mRootView.feedbackDialog_input_description.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        mRootView.feedbackDialog_input_description.setHorizontallyScrolling(false)
        mRootView.feedbackDialog_input_description.maxLines = 10
        mRootView.feedbackDialog_input_description.keyListener = TextKeyListener(TextKeyListener.Capitalize.SENTENCES, true)

        // adjust layout according to comment or vote selection
        if (mDialogType == AddFeedbackDialogType.COMMENT) {
            mRootView.feedbackDialog_layout_subject.visibility = View.GONE
            mRootView.feedbackDialog_inputLayout_description.hint = mReviewString
            mRootView.feedbackDialog_text_title.text = mAddComment
            mRootView.feedbackDialog_button_delete.text = mDeleteCommentString
        } else {
            mRootView.feedbackDialog_layout_subject.visibility = View.VISIBLE
            mRootView.feedbackDialog_inputLayout_description.hint = mDescriptionString
            mRootView.feedbackDialog_text_title.text = mAddVote
            mRootView.feedbackDialog_button_delete.text = mDeleteVoteString
        }

        if (mComment.description.isNotEmpty()) {
            mRootView.feedbackDialog_input_description.setText(mComment.description)
            mRootView.feedbackDialog_button_delete.visibility = View.VISIBLE
            mRootView.feedbackDialog_text_title.text = mEditComment

        } else if (mVote.text.isNotEmpty() && mVote.title.isNotEmpty()) {
            mRootView.feedbackDialog_input_description.setText(mVote.text)
            mRootView.feedbackDialog_button_delete.visibility = View.VISIBLE
            mRootView.feedbackDialog_text_title.text = mEditVote
            mRootView.feedbackDialog_input_subject.setText(mVote.title)

        } else
            mRootView.feedbackDialog_button_delete.visibility = View.GONE
    }


    interface FeedbackSentListener {
        fun positiveButtonClicked(comment: Comment?, vote: Vote?)
    }
}