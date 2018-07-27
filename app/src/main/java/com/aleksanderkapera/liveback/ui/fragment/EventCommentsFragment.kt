package com.aleksanderkapera.liveback.ui.fragment

import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment

/**
 * Created by kapera on 27-Jul-18.
 */
class EventCommentsFragment: BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment = EventCommentsFragment()
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_comments

    override fun setupViews(rootView: View) {
    }
}