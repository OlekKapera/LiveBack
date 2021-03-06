package com.aleksanderkapera.liveback.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.activity.SigningActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.asString
import com.aleksanderkapera.liveback.util.decodeSampledBitmapFromResource
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signing.*
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * Created by kapera on 05-Jul-18.
 */
class LoginFragment : BaseFragment() {

    private val incorrectCredentials = R.string.incorrect_email_password.asString()
    private val requiredField = R.string.required_field.asString()

    companion object {
        fun newInstance(): BaseFragment = LoginFragment()
    }

    private lateinit var mAuth: FirebaseAuth

    override fun getLayoutRes(): Int = R.layout.fragment_login

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mAuth = (activity as SigningActivity).mAuth

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setupViews(rootView: View) {
        login_image_background.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_login))

        //move the most bottom view above navigation bar
        val params = login_text_continue.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(0, 0, 0, getNavigationBarHeight() + 32)
        login_text_continue.layoutParams = params

        login_button_logIn.setOnClickListener { onLogInClick() }
        login_button_signUp.setOnClickListener { onSignUpClick() }
        login_text_continue.setOnClickListener { onContinueClick() }
    }

    private fun onLogInClick() {
        if (isFilled(login_input_email.text.toString(), login_input_password.text.toString())) {
            //show loader
            (activity as SigningActivity).signing_view_load.show()

            mAuth.signInWithEmailAndPassword(login_input_email.text.toString(), login_input_password.text.toString()).addOnCompleteListener {
                if (it.isSuccessful) {
                    MainActivity.startActivity(activity as Activity, false)
                } else {
                    Toast.makeText(context, incorrectCredentials, Toast.LENGTH_SHORT).show()
                }
                //hide loader
                (activity as SigningActivity).signing_view_load.hide()
            }
        }
    }

    private fun onSignUpClick() {
        (activity as SigningActivity).showFragment(RegisterFragment.newInstance())
    }

    private fun onContinueClick() {
        MainActivity.startActivity(activity as Activity, true)
    }

    /**
     * Validate input fields in empty condition
     */
    private fun isFilled(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                login_input_email.error = requiredField
                false
            }
            password.isEmpty() -> {
                login_input_password.error = requiredField
                false
            }
            else -> true
        }
    }
}