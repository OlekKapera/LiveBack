package com.aleksanderkapera.liveback.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import android.widget.TimePicker
import com.aleksanderkapera.liveback.model.DateTime
import com.aleksanderkapera.liveback.ui.activity.AddEventActivity
import com.aleksanderkapera.liveback.util.BUNDLE_TIME_DIALOG
import java.util.*


/**
 * Created by kapera on 01-Aug-18.
 */
class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var mDateTime: DateTime
    private var mTimePickerChoseListener: TimePickerChoseListener? = null

    companion object {
        fun newInstance(dateTime: DateTime): TimePickerDialogFragment {
            val fragment = TimePickerDialogFragment()
            val bundle = Bundle()

            bundle.putParcelable(BUNDLE_TIME_DIALOG, dateTime)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is AddEventActivity)
            mTimePickerChoseListener = activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mDateTime = arguments?.getParcelable(BUNDLE_TIME_DIALOG) ?: DateTime()

        if (mTimePickerChoseListener == null) {
            mTimePickerChoseListener = targetFragment as? TimePickerChoseListener
        }

        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        if (hour < 10)
            mDateTime.hour = "0$hour"
        else
            mDateTime.hour = "$hour"
        if (minute < 10)
            mDateTime.minute = "0$minute"
        else
            mDateTime.minute = "$minute"

        mTimePickerChoseListener?.timePicked(mDateTime)
    }

    interface TimePickerChoseListener {
        fun timePicked(dateTime: DateTime)
    }
}