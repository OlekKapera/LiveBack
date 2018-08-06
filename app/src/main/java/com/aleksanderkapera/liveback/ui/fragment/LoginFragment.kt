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
import com.aleksanderkapera.liveback.util.decodeSampledBitmapFromResource
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signing.*
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * Created by kapera on 05-Jul-18.
 */
class LoginFragment : BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment {
            return LoginFragment()
        }
    }

    private lateinit var mAuth: FirebaseAuth

    override fun getLayoutRes(): Int {
        return R.layout.fragment_login
    }

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

        login_button_logIn.setOnClickListener(onLogInClick)
        login_button_signUp.setOnClickListener(onSignUpClick)
    }

    private val onLogInClick = View.OnClickListener {
        //show loader
        (activity as SigningActivity).signing_view_load.show()

        mAuth.signInWithEmailAndPassword(login_input_email.text.toString(), login_input_password.text.toString()).addOnCompleteListener {
            if (it.isSuccessful) {
                MainActivity.startActivity(activity as Activity)
            } else {
                Toast.makeText(context, "Error with logging in", Toast.LENGTH_SHORT).show()
            }
            //hide loader
            (activity as SigningActivity).signing_view_load.hide()
        }
    }

    private val onSignUpClick = View.OnClickListener {
        (activity as SigningActivity).showFragment(RegisterFragment.newInstance())
    }
}