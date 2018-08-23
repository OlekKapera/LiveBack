package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.activity.SettingsActivity
import com.aleksanderkapera.liveback.util.LoggedUser
import kotlinx.android.synthetic.main.dialog_fragment_reminder.view.*

/**
 * Created by kapera on 23-Aug-18.
 */
class ReminderDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(): ReminderDialogFragment = ReminderDialogFragment()
    }

    override fun onResume() {
        val params = dialog.window?.attributes
        params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_reminder, null)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        rootView.reminderDialog_layout_buttons.check(when (LoggedUser.reminder) {
            0 -> R.id.reminderDialog_button_none
            10 -> R.id.reminderDialog_button_ten
            30 -> R.id.reminderDialog_button_thirty
            60 -> R.id.reminderDialog_button_hour
            120 -> R.id.reminderDialog_button_twoHours
            180 -> R.id.reminderDialog_button_threeHours
            360 -> R.id.reminderDialog_button_sixHours
            720 -> R.id.reminderDialog_button_twelveHours
            else -> R.id.reminderDialog_button_twentyFourHours
        })

        rootView.reminderDialog_layout_buttons.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.reminderDialog_button_none -> LoggedUser.reminder = 0
                R.id.reminderDialog_button_ten -> LoggedUser.reminder = 10
                R.id.reminderDialog_button_thirty -> LoggedUser.reminder = 30
                R.id.reminderDialog_button_hour -> LoggedUser.reminder = 60
                R.id.reminderDialog_button_twoHours -> LoggedUser.reminder = 120
                R.id.reminderDialog_button_threeHours -> LoggedUser.reminder = 180
                R.id.reminderDialog_button_sixHours -> LoggedUser.reminder = 360
                R.id.reminderDialog_button_twelveHours -> LoggedUser.reminder = 720
                R.id.reminderDialog_button_twentyFourHours -> LoggedUser.reminder = 1440
            }
            (activity as SettingsActivity).setupViews()
            dismiss()
        }

        rootView.reminderDialog_button_negative.setOnClickListener { dismiss() }

        return rootView
    }
}