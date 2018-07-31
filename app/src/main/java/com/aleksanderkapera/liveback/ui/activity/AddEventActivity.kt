package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.util.getStatusBarHeight
import kotlinx.android.synthetic.main.app_bar_add_event.*

/**
 * Created by kapera on 30-Jul-18.
 */
class AddEventActivity : BaseActivity() {

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, AddEventActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_add_event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set toolbar
        setSupportActionBar(addEvent_layout_toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowHomeEnabled(true)
        }

        // move only toolbar below status bar
        val toolbarParams = addEvent_layout_toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        addEvent_layout_toolbar.layoutParams = toolbarParams

    }


}