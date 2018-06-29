package com.aleksanderkapera.liveback.ui

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.ModelConstants
import com.aleksanderkapera.liveback.ui.adapter.EventsRecyclerAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by kapera on 29-May-18.
 */
class MainFragment : BaseFragment() {

    private lateinit var mEvents : List<Event>

    companion object {
        fun newInstance(events: ArrayList<Event>): BaseFragment {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ModelConstants.EVENTS, events)
            MainFragment().arguments = bundle
            return MainFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments
        mEvents = ArrayList(bundle?.getParcelableArrayList(ModelConstants.EVENTS))
    }

    override fun getLayoutRes(): Int {
        return R.layout.fragment_main
    }

    override fun setupViews(rootView: View) {
        // set toolbar
        appCompatActivity.setSupportActionBar(toolbar)
        val actionbar: ActionBar? = appCompatActivity.supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        initAdapter()
    }

    private fun initAdapter() {
        val adapter = EventsRecyclerAdapter(context!!)
        adapter.addData(mEvents)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        main_recycler_events.layoutManager = layoutManager
        main_recycler_events.adapter = adapter
    }
}