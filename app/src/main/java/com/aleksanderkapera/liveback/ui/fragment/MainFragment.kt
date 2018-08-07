package com.aleksanderkapera.liveback.ui.fragment

import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventsReceivedEvent
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.ui.adapter.EventsRecyclerAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import com.aleksanderkapera.liveback.util.setToolbarMargin
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by kapera on 29-May-18.
 */
class MainFragment : BaseFragment() {

    private lateinit var mEvents: List<Event>

    companion object {
        fun newInstance(): BaseFragment {
            return MainFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // register broadcast listener
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_main
    }

    override fun setupViews(rootView: View) {
        // set toolbar
        appCompatActivity.setSupportActionBar(event_layout_toolbar)
        val actionbar: ActionBar? = appCompatActivity.supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        setToolbarMargin(main_container_toolbar)
    }

    private fun initAdapter() {
        context?.let {
            val adapter = EventsRecyclerAdapter(it)
            adapter.addData(mEvents)
            val layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            main_recycler_events.layoutManager = layoutManager
            main_recycler_events.adapter = adapter

            val bottomOffset = BottomOffsetDecoration(getNavigationBarHeight())
            main_recycler_events.addItemDecoration(bottomOffset)
        }
    }

    @Subscribe
    fun onEventsReceivedEvent(event: EventsReceivedEvent) {
        mEvents = event.events
        initAdapter()
    }

    inner class BottomOffsetDecoration(val offset: Int): RecyclerView.ItemDecoration(){
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
}