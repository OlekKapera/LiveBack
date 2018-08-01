package com.aleksanderkapera.liveback.ui.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import android.widget.TimePicker
import com.aleksanderkapera.liveback.ui.activity.AddEventActivity
import java.util.*


/**
 * Created by kapera on 01-Aug-18.
 */
class TimePickerDialogFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        (activity as AddEventActivity).hour = hour
        (activity as AddEventActivity).minute = minute

        (activity as AddEventActivity).updateDate()
    }
}