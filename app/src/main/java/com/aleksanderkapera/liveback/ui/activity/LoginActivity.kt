package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity

/**
 * Created by kapera on 03-Jul-18.
 */
class LoginActivity: BaseActivity() {

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_login
    }
}