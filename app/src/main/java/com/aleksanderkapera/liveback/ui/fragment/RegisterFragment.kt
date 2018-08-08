package com.aleksanderkapera.liveback.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.activity.SigningActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.TAG_REGISTER_DIALOG
import com.aleksanderkapera.liveback.util.asString
import com.aleksanderkapera.liveback.util.decodeSampledBitmapFromResource
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signing.*
import kotlinx.android.synthetic.main.fragment_register.*

/**
 * Created by kapera on 05-Jul-18.
 */
class RegisterFragment : BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment = RegisterFragment()
    }

    private val requiredField = R.string.required_field.asString()
    private val incorrectEmail = R.string.incorrect_email.asString()
    private val shortPassword = R.string.short_password.asString()
    private val differentPasswords = R.string.different_passwords.asString()

    private lateinit var mAuth: FirebaseAuth

    var mImageUri: Uri? = null

    override fun getLayoutRes(): Int = R.layout.fragment_register

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mAuth = (activity as SigningActivity).mAuth

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setupViews(rootView: View) {
        register_image_background.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_register))

        //move the most bottom view above navigation bar
        val params = register_button_signUp.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(0, 0, 0, getNavigationBarHeight() + 32)
        register_button_signUp.layoutParams = params

        register_image_profile.setOnClickListener(onRegisterImageClick)
        register_button_signUp.setOnClickListener(onSignUpClick)
    }

    private val onRegisterImageClick = View.OnClickListener {
        ImagePickerDialogFragment.newInstance().show(fragmentManager, TAG_REGISTER_DIALOG)
    }

    /**
     * If all fields are valid perform auth call, write data to user database and upload his picture
     */
    private val onSignUpClick = View.OnClickListener {
        val userName = register_input_username.text.toString()
        val email = register_input_email.text.toString()
        val password = register_input_password.text.toString()
        val confirmPassword = register_input_confirm.text.toString()

        if (areValid(userName, email, password, confirmPassword)) {
            //show loader
            activity?.signing_view_load?.show()

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    val userId = mAuth.currentUser!!.uid
                    val userPojo = User(userId, userName, email, null)

                    mImageUri?.let {
                        (activity as SigningActivity).uploadImage(it, userPojo)
                    } ?: run {
                        (activity as SigningActivity).uploadUser(userPojo)
                    }
                } else {
                    activity?.signing_view_load?.hide()
                    Toast.makeText(context, R.string.signUp_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Validate input fields in empty condition
     */
    private fun isFilled(userName: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            userName.isEmpty() -> {
                register_input_username.error = requiredField
                false
            }
            email.isEmpty() -> {
                register_input_email.error = requiredField
                false
            }
            password.isEmpty() -> {
                register_input_password.error = requiredField
                false
            }
            confirmPassword.isEmpty() -> {
                register_input_confirm.error = requiredField
                false
            }
            else -> true
        }
    }

    /**
     * Validate input fields by specific conditions
     */
    private fun areValid(userName: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            !isFilled(userName, email, password, confirmPassword) -> false
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                register_input_email.error = incorrectEmail
                false
            }
            password.length < 8 -> {
                register_input_password.error = shortPassword
                false
            }
            confirmPassword != password -> {
                register_input_confirm.error = differentPasswords
                false
            }
            else -> true
        }
    }
}