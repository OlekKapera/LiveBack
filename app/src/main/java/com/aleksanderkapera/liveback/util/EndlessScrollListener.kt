package com.aleksanderkapera.liveback.util

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView


/**
 * Created by kapera on 01-Sep-18.
 */
abstract class EndlessScrollListener(private val manager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    private var previousTotal = 0 // The total number of items in the data set after the last load.
    private var loading = true // True if we are still waiting for the last set of data to load.
    private var visibleThreshold = 3 // The minimum amount of items to have below your current scroll position before loading more.

    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        visibleItemCount = recyclerView.childCount
        totalItemCount = manager.itemCount
        firstVisibleItem = manager.findFirstVisibleItemPosition()

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // end has been reached, load another page
            onLoadMore()
            loading = true
        }
    }

    // Defines the process for actually loading more data
    abstract fun onLoadMore()

    fun resetPaging() {
        previousTotal = 0
    }
}