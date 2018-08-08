package com.aleksanderkapera.liveback.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R

/**
 * Created by kapera on 08-Aug-18.
 */
class AddFeedbackDialogFragment: DialogFragment() {

    companion object {
        fun newInstance(): AddFeedbackDialogFragment = AddFeedbackDialogFragment()
    }

    override fun onResume() {
        val params = dialog.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_add_feedback, container, false)

        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val rootView = inflater.inflate(R.layout.dialog_fragment_add_feedback, null)

        return AlertDialog.Builder(context)
                .setView(rootView)
                .setCancelable(true)
                .setPositiveButton("OK"){_,_->

                }
                .setNegativeButton("NOPE"){_,_->
                    dismiss()
                }
                .create()
    }
}