package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.BUNDLE_EVENT_ABOUT
import com.aleksanderkapera.liveback.util.convertLongToDate
import kotlinx.android.synthetic.main.fragment_event_about.view.*
import kotlinx.android.synthetic.main.item_event_about.view.*

/**
 * Created by kapera on 27-Jul-18.
 */
class EventAboutFragment : BaseFragment() {

    private lateinit var mEvent: Event

    companion object {
        fun newInstance(event: Event): Fragment {
            val fragment = EventAboutFragment()
            val bundle = Bundle()

            bundle.putParcelable(BUNDLE_EVENT_ABOUT, event)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_about

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.getParcelable<Event>(BUNDLE_EVENT_ABOUT)?.let {
            mEvent = it
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setupViews(rootView: View) {
        rootView.eventAbout_container_description.eventItem_text_description.text = mEvent.description
        rootView.eventAbout_container_address.eventItem_text_description.text = mEvent.address
        rootView.eventAbout_container_date.eventItem_text_description.text = convertLongToDate(mEvent.date, "dd MMMM yyyy HH:mm")
        rootView.eventAbout_container_likes.eventItem_text_description.text = mEvent.likes.toString()
    }
}