package com.aleksanderkapera.liveback.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
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
import com.aleksanderkapera.liveback.util.BUNDLE_EVENT_DIALOG_TYPE
import com.aleksanderkapera.liveback.util.asString
import kotlinx.android.synthetic.main.dialog_fragment_add_feedback.view.*

/**
 * Created by kapera on 08-Aug-18.
 */
class AddFeedbackDialogFragment : DialogFragment() {

    private val mSendString = R.string.send.asString()
    private val mCancelString = R.string.cancel.asString()
    private val mReviewString = R.string.review.asString()
    private val mDescriptionString = R.string.description.asString()
    private val mRequiredField = R.string.required_field.asString()
    private val mAddComment = R.string.add_comment.asString()
    private val mAddVote = R.string.add_vote.asString()

    private lateinit var mDialogType: AddFeedbackDialogType
    private lateinit var mFeedbackSentListener: FeedbackSentListener

    companion object {
        fun newInstance(dialogType: AddFeedbackDialogType): AddFeedbackDialogFragment {
            val fragment = AddFeedbackDialogFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_EVENT_DIALOG_TYPE, dialogType)
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val rootView = inflater.inflate(R.layout.dialog_fragment_add_feedback, null)

        mFeedbackSentListener = targetFragment as FeedbackSentListener

        arguments?.let {
            mDialogType = it.get(BUNDLE_EVENT_DIALOG_TYPE) as AddFeedbackDialogType
        }

        // set keyboard button to next and capitalize sentence
        rootView.feedbackDialog_input_subject.imeOptions = EditorInfo.IME_ACTION_NEXT
        rootView.feedbackDialog_input_subject.setRawInputType(InputType.TYPE_CLASS_TEXT)
        rootView.feedbackDialog_input_subject.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        rootView.feedbackDialog_input_subject.setHorizontallyScrolling(false)
        rootView.feedbackDialog_input_subject.maxLines = 3
        rootView.feedbackDialog_input_subject.keyListener = TextKeyListener(TextKeyListener.Capitalize.SENTENCES, true)

        // set keyboard button to done and capitalize sentence
        rootView.feedbackDialog_input_description.imeOptions = EditorInfo.IME_ACTION_DONE
        rootView.feedbackDialog_input_description.setRawInputType(InputType.TYPE_CLASS_TEXT)
        rootView.feedbackDialog_input_description.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        rootView.feedbackDialog_input_description.setHorizontallyScrolling(false)
        rootView.feedbackDialog_input_description.maxLines = 10
        rootView.feedbackDialog_input_description.keyListener = TextKeyListener(TextKeyListener.Capitalize.SENTENCES, true)

        // adjust layout according to comment or vote selection
        if (mDialogType == AddFeedbackDialogType.COMMENT) {
            rootView.feedbackDialog_layout_subject.visibility = View.GONE
            rootView.feedbackDialog_inputLayout_description.hint = mReviewString
            rootView.feedbackDialog_text_title.text = mAddComment
        } else {
            rootView.feedbackDialog_layout_subject.visibility = View.VISIBLE
            rootView.feedbackDialog_inputLayout_description.hint = mDescriptionString
            rootView.feedbackDialog_text_title.text = mAddVote
        }

        val dialog = AlertDialog.Builder(context)
                .setView(rootView)
                .setCancelable(true)
                .setPositiveButton(mSendString, null)
                .setNegativeButton(mCancelString) { _, _ ->
                    dismiss()
                }
                .create()

        dialog.show()

        // set positive button action to validate fields and then send retrieved data further
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (rootView.feedbackDialog_input_subject.text.toString().isEmpty() && mDialogType == AddFeedbackDialogType.VOTE)
                rootView.feedbackDialog_input_subject.error = mRequiredField
            else if (rootView.feedbackDialog_input_description.text.toString().isEmpty())
                rootView.feedbackDialog_input_description.error = mRequiredField
            else {
                mFeedbackSentListener.positiveButtonClicked(rootView.feedbackDialog_input_subject.text.toString(),
                        rootView.feedbackDialog_input_description.text.toString())
                dismiss()
            }
        }

        // open soft keyboard when dialog is displayed
        rootView.feedbackDialog_input_subject.setOnFocusChangeListener { _, b ->
            if (b)
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        // open soft keyboard when dialog is displayed
        rootView.feedbackDialog_input_description.setOnFocusChangeListener { _, b ->
            if (b)
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        return dialog
    }

    interface FeedbackSentListener {
        fun positiveButtonClicked(title: String, description: String)
    }
}