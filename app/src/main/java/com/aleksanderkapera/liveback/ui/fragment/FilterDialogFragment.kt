package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.util.asString
import kotlinx.android.synthetic.main.dialog_fragment_filter.*

/**
 * Created by kapera on 06-Sep-18.
 */
class FilterDialogFragment : DialogFragment() {

    private val dateString = R.string.date.asString()
    private val titleString = R.string.title.asString()
    private val likesString = R.string.likes.asString()

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
        val rootView = inflater.inflate(R.layout.dialog_fragment_filter, container, false)

        rootView.setOnClickListener { showSortMenu() }
        return rootView
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
}