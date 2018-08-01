package com.aleksanderkapera.liveback.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import java.util.*
import android.app.DatePickerDialog
import android.widget.DatePicker
import com.aleksanderkapera.liveback.ui.activity.AddEventActivity


/**
 * Created by kapera on 01-Aug-18.
 */
class DatePickerDialogFragment: DialogFragment(), DatePickerDialog.OnDateSetListener{

    private val TIME_PICKER = "TIME PICKER"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day:Int){
        (activity as AddEventActivity).year = year
        (activity as AddEventActivity).month = month
        (activity as AddEventActivity).day = day

        TimePickerDialogFragment().show(fragmentManager,TIME_PICKER)
    }
}