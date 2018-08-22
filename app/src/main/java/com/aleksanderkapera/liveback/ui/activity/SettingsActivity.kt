package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.util.getStatusBarHeight
import kotlinx.android.synthetic.main.app_bar_settings.*

/**
 * Created by kapera on 21-Aug-18.
 */
class SettingsActivity : BaseActivity() {

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set toolbar
        setSupportActionBar(settings_layout_toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowHomeEnabled(true)
        }

        // move only toolbar below status bar
        val toolbarParams = settings_layout_toolbar.layoutParams as AppBarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        settings_layout_toolbar.layoutParams = toolbarParams

        settings_button_accept.setOnClickListener { }
    }
}