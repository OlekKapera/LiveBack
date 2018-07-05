package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.fragment.LoginFragment

/**
 * Created by kapera on 03-Jul-18.
 */
class SigningActivity: FragmentActivity() {

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, SigningActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_signing
    }

    override fun getDefaultFragment(): BaseFragment {
        return LoginFragment.newInstance()
    }
}