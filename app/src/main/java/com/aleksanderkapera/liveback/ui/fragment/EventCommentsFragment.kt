package com.aleksanderkapera.liveback.ui.fragment

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.ui.adapter.EventCommentsAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_event_comments.*

/**
 * Created by kapera on 27-Jul-18.
 */
class EventCommentsFragment : BaseFragment() {

    private lateinit var commentsAdapter: EventCommentsAdapter

    companion object {
        fun newInstance(): BaseFragment = EventCommentsFragment()
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_comments

    override fun setupViews(rootView: View) {
        initAdapter()
    }

    /**
     * Mock DATA
     */
    private fun mockData(): List<Comment> {
        // TODO replace with proper data
        return listOf(Comment("Birgit Kos", "I love it! You are awesome!", 1532932348494, "", ""),
                Comment("Christine Everhart", "OMG! It was amazing, can't wait for another one.", 1532932235023, "", ""))
    }

    /**
     * Fill recycler with data
     */
    private fun initAdapter() {
        context?.let {
            commentsAdapter = EventCommentsAdapter(it)
            commentsAdapter.replaceData(mockData())
            val layout = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            eventComment_layout_comments.layoutManager = layout
            eventComment_layout_comments.adapter = commentsAdapter
        }
    }
}