package com.aleksanderkapera.liveback.ui

import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.SimpleEvent
import com.aleksanderkapera.liveback.ui.adapter.EventsRecyclerAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by kapera on 29-May-18.
 */
class MainFragment : BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment {
            return MainFragment()
        }
    }

    private val mEvents = arrayListOf(SimpleEvent("14513543", "1532135", "Mari Sheibley", 1543056268, "The Glossary of Telescopes", "Buying the right telescope to take your love of astronomy to the next level is a big homework like this, you will find just the right telescope for this next big step in the evolution of your passion for astronomy.", "Presentation", 164, 31, 82),
            SimpleEvent("14513543", "1532135", "Birgit Kos", 1544352268, "The Basic Of Buying a Telescope", "Buying the right telescope to take your love of astronomy to the next level is a big homework like this, you will find just the right telescope for this next big step in the evolution of your passion for astronomy.", "Presentation", 164, 31, 82))

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