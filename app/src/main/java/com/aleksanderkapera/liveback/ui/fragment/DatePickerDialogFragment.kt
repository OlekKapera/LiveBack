package com.aleksanderkapera.liveback.ui.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import com.aleksanderkapera.liveback.model.DateTime
import com.aleksanderkapera.liveback.util.REQUEST_TARGET_FILTER_FRAGMENT
import com.aleksanderkapera.liveback.util.TAG_DATE_PICKER
import com.aleksanderkapera.liveback.util.convertStringToLongTime
import java.util.*


/**
 * Created by kapera on 01-Aug-18.
 */
class DatePickerDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private val TIME_PICKER = "TIME PICKER"
    private val mDateTime = DateTime()
    private var mFromLong: Long = 0

    private var year = 0
    private var month = 0
    private var day = 0
    private lateinit var calendar: Calendar

    companion object {
        fun newInstance(dateTime: DateTime?): DatePickerDialogFragment {
            val fragment = DatePickerDialogFragment()
            val bundle = Bundle()

            dateTime?.let {
                val dateLong = convertStringToLongTime("${it.day}.${it.month}.${it.year} ${it.hour}:${it.minute}")
                bundle.putLong(TAG_DATE_PICKER, dateLong)
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // If from date is 0, use the current date as the default date in the picker
        mFromLong = arguments?.getLong(TAG_DATE_PICKER) ?: 0
        calendar = Calendar.getInstance()

        if (mFromLong != 0L) {
            val date = Date(mFromLong)
            year = date.year
            month = date.month
            day = date.day
        }
        else{
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
        }

        // Create a new instance of DatePickerDialog
        val dialog = DatePickerDialog(activity, this, year, month, day)

        if (mFromLong != 0L)
            dialog.datePicker.minDate = mFromLong

        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        mDateTime.year = year
        mDateTime.month = month+1
        mDateTime.day = day

        val dialog = TimePickerDialogFragment.newInstance(mDateTime)
        dialog.setTargetFragment(targetFragment, REQUEST_TARGET_FILTER_FRAGMENT)
        dialog.show(fragmentManager, TIME_PICKER)
    }
}