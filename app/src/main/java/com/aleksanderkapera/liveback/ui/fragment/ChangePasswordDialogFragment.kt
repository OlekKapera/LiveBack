package com.aleksanderkapera.liveback.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.util.asString

/**
 * Created by kapera on 22-Aug-18.
 */
class ChangePasswordDialogFragment : DialogFragment() {

    private val changePassword = R.string.change_password.asString()
    private val cancel = R.string.cancel.asString()


    companion object {
        fun newInstance(): ChangePasswordDialogFragment = ChangePasswordDialogFragment()
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
        val rootView = inflater.inflate(R.layout.dialog_fragment_change_password, null)

        val dialog = AlertDialog.Builder(context)
                .setView(rootView)
                .setCancelable(true)
                .setPositiveButton(changePassword, null)
                .setNegativeButton(cancel) { _, _ ->
                    dismiss()
                }
                .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = Html.fromHtml("<b>$changePassword</b>")
        }

        return dialog
    }
}