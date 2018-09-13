package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.DateTime
import com.aleksanderkapera.liveback.util.REQUEST_TARGET_FILTER_FRAGMENT
import com.aleksanderkapera.liveback.util.TAG_FILTER_TIME
import com.aleksanderkapera.liveback.util.asDrawable
import com.aleksanderkapera.liveback.util.asString
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import kotlinx.android.synthetic.main.dialog_fragment_filter.*
import kotlinx.android.synthetic.main.dialog_fragment_filter.view.*

/**
 * Created by kapera on 06-Sep-18.
 */
class FilterDialogFragment : DialogFragment(), TimePickerDialogFragment.TimePickerChoseListener {

    private val dateString = R.string.date.asString()
    private val titleString = R.string.title.asString()
    private val likesString = R.string.likes.asString()

    private val ascendingDrawable = R.drawable.ic_arrow_up.asDrawable()
    private val descendingDrawable = R.drawable.ic_arrow_down.asDrawable()

    private lateinit var rootView: View
    private var mTimeFromClicked = false
    private var mDateTime = DateTime()

    private var mDirectionAsc = true


    companion object {
        fun newInstance(): FilterDialogFragment = FilterDialogFragment()
    }

    override fun onResume() {
        val params = dialog.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.dialog_fragment_filter, container, false)

        rootView.filterDialog_popup_sort.setOnClickListener { showSortMenu() }
        rootView.filterDialog_button_sortDirection.setOnClickListener { switchSortDirection() }

        rootView.filterDialog_layout_timeFrom.setOnClickListener { showTimeFragment(true) }

        rootView.filterDialog_layout_timeTo.setOnClickListener { showTimeFragment(false) }

        setupSeekBar()

        return rootView
    }

    /**
     * Set time interval accordingly to user's input
     */
    override fun timePicked(dateTime: DateTime) {
        mDateTime = dateTime

        if (mTimeFromClicked)
            filterDialog_button_timeFrom.text = "${dateTime.day}.${dateTime.month}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}"
        else
            filterDialog_button_timeTo.text = "${dateTime.day}.${dateTime.month}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}"

    }

    /**
     * Open popup menu containing sorting options
     */
    private fun showSortMenu() {
        context?.let {
            val popup = PopupMenu(it, filterDialog_popup_sort)
            popup.inflate(R.menu.menu_filter_sort)
            popup.setOnMenuItemClickListener { switchSort(it) }
            popup.show()
        }
    }

    /**
     * Switch sorting option and display in filter
     */
    private fun switchSort(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_sort_title -> filterDialog_popup_sort.text = titleString
            R.id.filter_sort_likes -> filterDialog_popup_sort.text = likesString
            else -> filterDialog_popup_sort.text = dateString
        }

        return true
    }

    /**
     * Switch sorting direction
     */
    private fun switchSortDirection() {
        mDirectionAsc = if (mDirectionAsc) {
            filterDialog_button_sortDirection.setImageDrawable(descendingDrawable)
            false
        } else {
            filterDialog_button_sortDirection.setImageDrawable(ascendingDrawable)
            true
        }
    }

    /**
     * Setup seekbar view and handle slide events
     */
    private fun setupSeekBar() {
        rootView.filterDialog_slider_likes.setValue(0f, rootView.filterDialog_slider_likes.maxProgress)

        rootView.filterDialog_slider_likes.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                rootView.filterDialog_text_likesMin.text = leftValue.toInt().toString()
                rootView.filterDialog_text_likesMax.text = rightValue.toInt().toString()
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }
        })
    }

    private fun showTimeFragment(timeFromClicked: Boolean) {
        mTimeFromClicked = timeFromClicked

        var dialog = if (mTimeFromClicked)
            DatePickerDialogFragment.newInstance(null)
        else
            DatePickerDialogFragment.newInstance(mDateTime)

        dialog.setTargetFragment(this, REQUEST_TARGET_FILTER_FRAGMENT)
        dialog.show(fragmentManager, TAG_FILTER_TIME)
    }
}