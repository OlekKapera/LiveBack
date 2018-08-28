package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.ui.adapter.EventCommentsAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.BottomOffsetDecoration
import com.aleksanderkapera.liveback.util.BUNDLE_EVENT_COMMENT
import com.aleksanderkapera.liveback.util.asDimen
import com.aleksanderkapera.liveback.util.dpToPx
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import kotlinx.android.synthetic.main.fragment_event_comments.*
import java.io.Serializable

/**
 * Created by kapera on 27-Jul-18.
 */
class EventCommentsFragment : BaseFragment() {

    var comments = listOf<Comment>()
    lateinit var commentsAdapter: EventCommentsAdapter

    companion object {
        fun newInstance(comments: List<Comment>?): EventCommentsFragment {
            val fragment = EventCommentsFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_EVENT_COMMENT, comments as Serializable)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_comments

    override fun setupViews(rootView: View) {
        comments = arguments?.get(BUNDLE_EVENT_COMMENT) as List<Comment>
        initAdapter()
    }

    /**
     * Fill recycler with data
     */
    private fun initAdapter() {
        context?.let {
            commentsAdapter = when (parentFragment) {
                is EventFragment -> EventCommentsAdapter(it, parentFragment as EventFragment)
                else -> EventCommentsAdapter(it, parentFragment as ProfileFragment)
            }

            commentsAdapter.replaceData(comments)
            val layout = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            eventComment_recycler_comments.layoutManager = layout
            eventComment_recycler_comments.adapter = commentsAdapter
            eventComment_recycler_comments.addItemDecoration(BottomOffsetDecoration(getNavigationBarHeight() + dpToPx(R.dimen.spacing16.asDimen().toInt())))
        }

        (parentFragment as? EventFragment)?.switchEmptyView(comments as MutableList<Any>, eventComment_recycler_comments, eventComment_view_emptyScreen)
        (parentFragment as? ProfileFragment)?.switchEmptyView(comments as MutableList<Any>, eventComment_recycler_comments, eventComment_view_emptyScreen)
    }
}