package com.aleksanderkapera.liveback.util

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log


/**
 * Created by kapera on 01-Sep-18.
 */
abstract class EndlessScrollListener(private val manager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    private var previousTotal = 0 // The total number of items in the data set after the last load.
    private var loading = false // True if we are still waiting for the last set of data to load.
    private var visibleThreshold = 1 // The minimum amount of items to have below your current scroll position before loading more.

    private var lastVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        visibleItemCount = recyclerView.childCount
        totalItemCount = manager.itemCount
        lastVisibleItem = manager.findLastVisibleItemPosition()

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
            }
        } else if (!loading && (totalItemCount <= (lastVisibleItem + visibleThreshold)) && previousTotal != totalItemCount) {
            // end has been reached, load another page
            loading = true
            onLoadMore()
        }
    }

    // Defines the process for actually loading more data
    abstract fun onLoadMore()

    fun resetPaging() {
        previousTotal = 0
    }
}