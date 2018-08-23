package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.ui.fragment.ChangePasswordDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ReminderDialogFragment
import com.aleksanderkapera.liveback.util.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.app_bar_settings.*
import kotlinx.android.synthetic.main.container_settings_credentials.*
import kotlinx.android.synthetic.main.container_settings_notifications.*
import kotlinx.android.synthetic.main.container_settings_reminder.*

/**
 * Created by kapera on 21-Aug-18.
 */
class SettingsActivity : BaseActivity() {

    private val none = R.string.none.asString()
    private val tenMin = R.string.ten_before.asString()
    private val thirtyMin = R.string.thirty_before.asString()
    private val hour = R.string.hour_before.asString()
    private val twoHours = R.string.two_hours_before.asString()
    private val threeHours = R.string.three_hours_before.asString()
    private val sixHours = R.string.six_hours_before.asString()
    private val twelveHours = R.string.twelve_hours_before.asString()
    private val twentyFourHours = R.string.twenty_four_hours_before.asString()

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

        setupViews()

        settings_button_accept.setOnClickListener { }
        settings_container_password.setOnClickListener { ChangePasswordDialogFragment.newInstance().show(supportFragmentManager, TAG_SETTINGS_CHANGE_PASSWORD) }
        settings_container_reminder.setOnClickListener { ReminderDialogFragment.newInstance().show(supportFragmentManager, TAG_SETTINGS_REMINDER) }
    }

    /**
     * Fill views with user settings
     */
    fun setupViews() {
        settings_input_name.setText(LoggedUser.username)
        settings_input_email.setText(LoggedUser.email)

        settings_switch_yourEvent.isChecked = LoggedUser.commentAddedOnYour
        settings_switch_favEvent.isChecked = LoggedUser.commentAddedOnFav
        settings_switch_yourVote.isChecked = LoggedUser.voteAddedOnYour
        settings_switch_favVote.isChecked = LoggedUser.voteAddedOnFav

        when (LoggedUser.reminder) {
            0 -> settings_text_reminder.text = none
            10 -> settings_text_reminder.text = tenMin
            30 -> settings_text_reminder.text = thirtyMin
            60 -> settings_text_reminder.text = hour
            120 -> settings_text_reminder.text = twoHours
            180 -> settings_text_reminder.text = threeHours
            360 -> settings_text_reminder.text = sixHours
            720 -> settings_text_reminder.text = twelveHours
            1440 -> settings_text_reminder.text = twentyFourHours
        }
    }
}