package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.util.asString
import com.aleksanderkapera.liveback.util.setToolbarMargin
import kotlinx.android.synthetic.main.activity_add_event.*

/**
 * Created by kapera on 30-Jul-18.
 */
class AddEventActivity: BaseActivity() {

    companion object {
        fun startActivity(activity: Activity){
            val intent = Intent(activity, AddEventActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.activity_add_event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set toolbar
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = R.string.add_event.asString()
        }

//        setToolbarMargin(addEvent_layout_appBar)
    }
}