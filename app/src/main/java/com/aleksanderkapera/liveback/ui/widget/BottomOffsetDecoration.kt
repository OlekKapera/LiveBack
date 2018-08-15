package com.aleksanderkapera.liveback.ui.widget

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Display blank space at the end of recycler view
 */
class BottomOffsetDecoration(val offset: Int): RecyclerView.ItemDecoration(){
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val dataSize = state.itemCount
        val position = parent.getChildAdapterPosition(view)
        if (dataSize > 0 && position == dataSize - 1) {
            outRect.set(0, 0, 0, offset)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}