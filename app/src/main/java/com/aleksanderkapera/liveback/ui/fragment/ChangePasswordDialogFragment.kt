package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.util.LoggedUser
import com.aleksanderkapera.liveback.util.asString
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.dialog_fragment_change_password.view.*

/**
 * Created by kapera on 22-Aug-18.
 */
class ChangePasswordDialogFragment : DialogFragment() {

    private val shortPassword = R.string.short_password.asString()
    private val oldMatchesNew = R.string.old_matches_new.asString()
    private val dontMatchPass = R.string.different_passwords.asString()
    private val requiredField = R.string.required_field.asString()
    private val changeSuccessful = R.string.success_password_change.asString()
    private val genericError = R.string.generic_error.asString()
    private val incorrectOldPassword = R.string.incorrect_password.asString()

    private var isOldValidated = false
    private var isNewValidated = false
    private var isConfirmValidated = false

    private lateinit var mAuthUser: FirebaseUser

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_change_password, null)

        FirebaseAuth.getInstance().currentUser?.let {
            mAuthUser = it
        }

        setFieldValidation(rootView)

        rootView.passwordDialog_button_positive.setOnClickListener { positiveButtonClick(rootView) }
        rootView.passwordDialog_button_negative.setOnClickListener { dismiss() }

        return rootView
    }

    /**
     * Change password if inputs are valid and old password matches the one which user inserted
     */
    private fun positiveButtonClick(rootView: View) {
        validateOldPassword(rootView)
        validateNewPassword(rootView)
        validateConfirmPassword(rootView)

        val oldPassword = rootView.passwordDialog_input_old.text.toString()
        val newPassword = rootView.passwordDialog_input_new.text.toString()

        if (isOldValidated && isNewValidated && isConfirmValidated) {
            rootView.passwordDialog_view_load.show()
            val credentials = EmailAuthProvider.getCredential(LoggedUser.email, oldPassword)
            mAuthUser.reauthenticate(credentials).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        mAuthUser.updatePassword(newPassword).addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    Toast.makeText(context, changeSuccessful, Toast.LENGTH_SHORT).show()
                                    dismiss()
                                }
                                else -> {
                                    Toast.makeText(context, genericError, Toast.LENGTH_SHORT).show()
                                    dismiss()
                                }
                            }
                            rootView.passwordDialog_view_load.hide()
                        }
                    }
                    else -> {
                        rootView.passwordDialog_layout_old.error = incorrectOldPassword
                        rootView.passwordDialog_view_load.hide()
                    }
                }
            }
        }
    }

    /**
     * Validate input whether old one doesn't match new one and confirmation one matches new password
     */
    private fun setFieldValidation(rootView: View) {

        rootView.passwordDialog_input_old.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)
                validateOldPassword(rootView)
        }

        rootView.passwordDialog_input_new.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)
                validateNewPassword(rootView)
        }

        rootView.passwordDialog_input_confirm.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)
                validateConfirmPassword(rootView)
        }
    }

    private fun validateOldPassword(rootView: View) {
        if (rootView.passwordDialog_input_old.text.isEmpty()) {
            rootView.passwordDialog_layout_old.error = requiredField
            isOldValidated = false
        } else {
            rootView.passwordDialog_layout_old.isErrorEnabled = false
            isOldValidated = true
        }
    }

    private fun validateNewPassword(rootView: View) {
        when {
            rootView.passwordDialog_input_new.text.isEmpty() -> {
                rootView.passwordDialog_layout_new.error = requiredField
                isNewValidated = false
            }
            rootView.passwordDialog_input_new.text.length < 8 -> {
                rootView.passwordDialog_layout_new.error = shortPassword
                isNewValidated = false
            }
            rootView.passwordDialog_input_new.text.toString() == rootView.passwordDialog_input_old.text.toString() -> {
                rootView.passwordDialog_layout_new.error = oldMatchesNew
                isNewValidated = false
            }
            else -> {
                rootView.passwordDialog_layout_new.isErrorEnabled = false
                isNewValidated = true
            }
        }
    }

    private fun validateConfirmPassword(rootView: View) {
        when {
            rootView.passwordDialog_input_confirm.text.isEmpty() -> {
                rootView.passwordDialog_layout_confirm.error = requiredField
                isConfirmValidated = false
            }
            rootView.passwordDialog_input_confirm.text.toString() != rootView.passwordDialog_input_new.text.toString() -> {
                rootView.passwordDialog_layout_confirm.error = dontMatchPass
                isConfirmValidated = false
            }
            else -> {
                rootView.passwordDialog_layout_confirm.isErrorEnabled = false
                isConfirmValidated = true
            }
        }
    }
}