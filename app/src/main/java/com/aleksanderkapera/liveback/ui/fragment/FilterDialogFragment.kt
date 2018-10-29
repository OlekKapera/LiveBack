package com.aleksanderkapera.liveback.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.DateTime
import com.aleksanderkapera.liveback.model.Filter
import com.aleksanderkapera.liveback.util.*
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
    private var mDateTime: DateTime? = null

    private lateinit var mFilter: Filter

    companion object {
        fun newInstance(filter: Filter?): FilterDialogFragment {
            val fragment = FilterDialogFragment()
            val bundle = Bundle()

            bundle.putParcelable(BUNDLE_FILTER_DIALOG, filter)
            fragment.arguments = bundle

            return fragment
        }
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
        mFilter = arguments?.get(BUNDLE_FILTER_DIALOG) as Filter? ?: Filter()

        if (arguments?.get(BUNDLE_FILTER_DIALOG) as Filter? != null)
            setupViews()

        rootView.filterDialog_popup_sort.setOnClickListener { showSortMenu() }
        rootView.filterDialog_button_sortDirection.setOnClickListener { switchSortDirection() }

        rootView.filterDialog_layout_timeFrom.setOnClickListener { showTimeFragment(true) }

        rootView.filterDialog_layout_timeTo.setOnClickListener { showTimeFragment(false) }

        setupSeekBar()

        rootView.filterDialog_button_positive.setOnClickListener { positiveButtonClick() }
        rootView.filterDialog_button_negative.setOnClickListener { dismiss() }

        return rootView
    }

    /**
     * Set time interval accordingly to user's input
     */
    override fun timePicked(dateTime: DateTime) {
        mDateTime = dateTime

        if (mTimeFromClicked) {
            filterDialog_button_timeFrom.text = "${dateTime.day}.${dateTime.month}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}"
            mFilter.timeFrom = dateTime.getLong()
        } else {
            filterDialog_button_timeTo.text = "${dateTime.day}.${dateTime.month}.${dateTime.year} ${dateTime.hour}:${dateTime.minute}"
            mFilter.timeTo = dateTime.getLong()
        }

    }

    /**
     * Fill views with data passed from parent fragment
     */
    private fun setupViews() {
        rootView.filterDialog_popup_sort.text = when (mFilter.sortBy) {
            SortType.TITLE -> titleString
            SortType.LIKES -> likesString
            else -> dateString
        }

        when {
            mFilter.directionAsc -> rootView.filterDialog_button_sortDirection.setImageDrawable(ascendingDrawable)
            else -> rootView.filterDialog_button_sortDirection.setImageDrawable(descendingDrawable)
        }

        if (mFilter.timeFrom != 0L)
            rootView.filterDialog_button_timeFrom.text = convertLongToDate(mFilter.timeFrom, "dd MMM yyyy HH:mm")

        if (mFilter.timeTo != 0L)
            rootView.filterDialog_button_timeTo.text = convertLongToDate(mFilter.timeTo, "dd MMM yyyy HH:mm")
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
     * Adjust range min and max values. When max value is selected replace it with "more then" sign
     */
    private fun adjustLikesRangeValues(){
        rootView.filterDialog_text_likesMin.text = when(mFilter.likesFrom == filterLikesTo){
            true -> "$filterLikesTo"
            else -> mFilter.likesFrom.toString()
        }

        rootView.filterDialog_text_likesMax.text = when(mFilter.likesTo == filterLikesTo){
            true -> ">$filterLikesTo"
            else -> mFilter.likesTo.toString()
        }
    }

    /**
     * Switch sorting option and display in filter
     */
    private fun switchSort(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_sort_title -> {
                filterDialog_popup_sort.text = titleString
                mFilter.sortBy = SortType.TITLE
            }
            R.id.filter_sort_likes -> {
                filterDialog_popup_sort.text = likesString
                mFilter.sortBy = SortType.LIKES
            }
            else -> {
                filterDialog_popup_sort.text = dateString
                mFilter.sortBy = SortType.DATE
            }
        }

        return true
    }

    /**
     * Switch sorting direction
     */
    private fun switchSortDirection() {
        mFilter.directionAsc = if (mFilter.directionAsc) {
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
        rootView.filterDialog_slider_likes.setRange(0f, filterLikesTo.toFloat())
        rootView.filterDialog_slider_likes.setValue(mFilter.likesFrom.toFloat(), mFilter.likesTo.toFloat())
        adjustLikesRangeValues()

        rootView.filterDialog_slider_likes.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                mFilter.likesFrom = leftValue.toInt()
                mFilter.likesTo = rightValue.toInt()

                adjustLikesRangeValues()
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }
        })
    }

    private fun showTimeFragment(timeFromClicked: Boolean) {
        mTimeFromClicked = timeFromClicked

        val dialog = if (mTimeFromClicked)
            DatePickerDialogFragment.newInstance(null)
        else
            DatePickerDialogFragment.newInstance(mDateTime)

        dialog.setTargetFragment(this, REQUEST_TARGET_FILTER_FRAGMENT)
        dialog.show(fragmentManager, TAG_FILTER_TIME)
    }

    /**
     * Store every filter value inserted by user and pass them to [MainFragment] where they will be handled
     */
    private fun positiveButtonClick() {
        val i = Intent()
                .putExtra(INTENT_MAIN_FILTER, mFilter)

        targetFragment?.onActivityResult(REQUEST_TARGET_MAIN_FRAGMENT, Activity.RESULT_OK, i)
        dismiss()
    }
}