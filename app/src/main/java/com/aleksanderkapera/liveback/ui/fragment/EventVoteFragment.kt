package com.aleksanderkapera.liveback.ui.fragment

import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment

/**
 * Created by kapera on 27-Jul-18.
 */
class EventVoteFragment: BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment = EventVoteFragment()
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_vote

    override fun setupViews(rootView: View) {
    }
}