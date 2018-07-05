package com.aleksanderkapera.liveback.ui.fragment

import android.support.constraint.ConstraintLayout
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.AndroidUtils
import com.aleksanderkapera.liveback.util.ImageUtils
import kotlinx.android.synthetic.main.fragment_register.*

/**
 * Created by kapera on 05-Jul-18.
 */
class RegisterFragment : BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment {
            return RegisterFragment()
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_register
    }

    override fun setupViews(rootView: View) {
        register_image_background.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(AndroidUtils.getResources(),R.drawable.bg_register))

        //move the most bottom view above navigation bar
        val params = register_button_signUp.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(0, 0, 0, AndroidUtils.getNavigationBarHeight()+32)
        register_button_signUp.layoutParams = params
    }
}