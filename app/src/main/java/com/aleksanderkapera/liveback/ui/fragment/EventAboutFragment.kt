package com.aleksanderkapera.liveback.ui.fragment

import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment

/**
 * Created by kapera on 27-Jul-18.
 */
class EventAboutFragment: BaseFragment() {

    companion object {
        fun newInstance(): BaseFragment = EventAboutFragment()
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_about

    override fun setupViews(rootView: View) {
    }
}