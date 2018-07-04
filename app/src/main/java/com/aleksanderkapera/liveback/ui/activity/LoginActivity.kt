package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.util.AndroidUtils
import kotlinx.android.synthetic.main.activity_login.*

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
        return R.layout.activity_register
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val params = login_text_continue.layoutParams as ConstraintLayout.LayoutParams
//        params.setMargins(0, 0, 0, AndroidUtils.getNavigationBarHeight()+32)
//        register_text_continue.layoutParams = params
    }
}