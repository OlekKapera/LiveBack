package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.ui.adapter.EventsRecyclerAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.BottomOffsetDecoration
import com.aleksanderkapera.liveback.util.BUNDLE_PROFILE_EVENTS
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import kotlinx.android.synthetic.main.fragment_profile_events.*
import java.io.Serializable

/**
 * Created by kapera on 20-Aug-18.
 */
class ProfileEventsFragment : BaseFragment() {

    private var mEvents = mutableListOf<Event>()
    lateinit var mEventsAdapter:EventsRecyclerAdapter

    companion object {
        fun newInstance(events: MutableList<Event>): ProfileEventsFragment {
            val fragment = ProfileEventsFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_PROFILE_EVENTS, events as Serializable)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_profile_events

    override fun setupViews(rootView: View) {
        mEvents = arguments?.getSerializable(BUNDLE_PROFILE_EVENTS) as MutableList<Event>

        context?.let {
            mEventsAdapter = EventsRecyclerAdapter(it)
            mEventsAdapter.addData(mEvents)
            val layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            profileEvents_recycler_events.layoutManager = layoutManager
            profileEvents_recycler_events.adapter = mEventsAdapter

            val bottomOffset = BottomOffsetDecoration(getNavigationBarHeight())
            profileEvents_recycler_events.addItemDecoration(bottomOffset)
        }

        (parentFragment as ProfileFragment).switchEmptyView(mEvents as MutableList<Any>, profileEvents_recycler_events, profileEvents_view_emptyScreen)
    }
}