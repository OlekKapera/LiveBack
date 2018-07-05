package com.aleksanderkapera.liveback.ui.fragment

import android.support.constraint.ConstraintLayout
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.activity.SigningActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.AndroidUtils
import com.aleksanderkapera.liveback.util.ImageUtils
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

    override fun getLayoutRes(): Int {
        return R.layout.fragment_login
    }

    override fun setupViews(rootView: View) {
        login_image_background.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(AndroidUtils.getResources(), R.drawable.bg_login))

        //move the most bottom view above navigation bar
        val params = login_text_continue.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(0, 0, 0, AndroidUtils.getNavigationBarHeight() + 32)
        login_text_continue.layoutParams = params

        login_button_signUp.setOnClickListener(onSignUpClick)
    }

    private val onSignUpClick = View.OnClickListener { _ ->
        (activity as SigningActivity).showFragment(RegisterFragment.newInstance())
    }
}