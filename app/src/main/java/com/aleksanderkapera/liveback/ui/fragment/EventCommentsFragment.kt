package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.ui.adapter.EventCommentsAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.BUNDLE_EVENT_ABOUT
import com.aleksanderkapera.liveback.util.BUNDLE_EVENT_COMMENT
import kotlinx.android.synthetic.main.fragment_event_comments.*
import java.io.Serializable

/**
 * Created by kapera on 27-Jul-18.
 */
class EventCommentsFragment : BaseFragment() {

    private var mComments = listOf<Comment>()
    private lateinit var commentsAdapter: EventCommentsAdapter

    companion object {
        fun newInstance(comments: List<Comment>?): BaseFragment {
            val fragment = EventCommentsFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_EVENT_ABOUT, comments as Serializable)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_comments

    override fun setupViews(rootView: View) {
        mComments = arguments?.get(BUNDLE_EVENT_COMMENT) as List<Comment>
        initAdapter()
    }

    /**
     * Fill recycler with data
     */
    private fun initAdapter() {
        context?.let {
            commentsAdapter = EventCommentsAdapter(it)
            commentsAdapter.replaceData(mComments)
            val layout = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            eventComment_layout_comments.layoutManager = layout
            eventComment_layout_comments.adapter = commentsAdapter
        }
    }
}