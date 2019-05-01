package com.aleksanderkapera.liveback.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventsReceivedEvent
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.Filter
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.adapter.EventsRecyclerAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.BottomOffsetDecoration
import com.aleksanderkapera.liveback.ui.widget.EmptyScreenView
import com.aleksanderkapera.liveback.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit

/**
 * Created by kapera on 29-May-18.
 */
class MainFragment : BaseFragment() {

    private var mEvents = mutableListOf<Event>()
    private lateinit var mEndlessScrollListener: EndlessScrollListener
    private lateinit var mAdapter: EventsRecyclerAdapter
    private var mFilter: Filter? = null

    private val mEventsError = R.string.events_error.asString()

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
        appCompatActivity.setSupportActionBar(main_layout_toolbar)
        val actionbar: ActionBar? = appCompatActivity.supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        setToolbarMargin(main_container_toolbar)

        main_toolbar_search.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus && main_toolbar_search.query.isEmpty()) {
                main_toolbar_search.onActionViewCollapsed()
                main_toolbar_title.visibility = View.VISIBLE
            }
        }

        main_toolbar_search.setOnSearchClickListener {
            main_toolbar_title.visibility = View.GONE
        }

        main_layout_swipe.setOnRefreshListener {
            (activity as MainActivity).getEvents(mFilter?.sortBy, mFilter?.directionAsc, true)
        }
        main_toolbar_filter.setOnClickListener {
            val dialog = FilterDialogFragment.newInstance(mFilter)
            dialog.setTargetFragment(this, REQUEST_TARGET_MAIN_FRAGMENT)
            dialog.show(fragmentManager, TAG_MAIN_FILTER)
        }

        initAdapter()
        setSearchObservable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TARGET_MAIN_FRAGMENT && resultCode == Activity.RESULT_OK) {
            data?.let {
                val bundle = it.extras
                mFilter = bundle.getParcelable(INTENT_MAIN_FILTER) as Filter?

                mFilter?.let {
                    if (it.sortChanged)
                        (activity as MainActivity).getEvents(it.sortBy, it.directionAsc)
                    else
                        filter()
                }
            }
        }
    }

    private fun initAdapter() {
        context?.let {
            mAdapter = EventsRecyclerAdapter(it)
            val layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            main_recycler_events.layoutManager = layoutManager
            main_recycler_events.adapter = mAdapter
            mEndlessScrollListener = object : EndlessScrollListener(layoutManager) {
                override fun onLoadMore() {
                    main_recycler_events.stopScroll()
                    if (main_toolbar_search.query.isEmpty()) {
                        (activity as MainActivity).getEvents(mFilter?.sortBy, mFilter?.directionAsc, false)
                    } else
                        (activity as MainActivity).search(main_toolbar_search.query.toString())
                }
            }
            main_recycler_events.addOnScrollListener(mEndlessScrollListener)

            (activity as MainActivity).getEvents(mFilter?.sortBy, mFilter?.directionAsc)

            val bottomOffset = BottomOffsetDecoration(getNavigationBarHeight())
            main_recycler_events.addItemDecoration(bottomOffset)
        }
    }

    private fun setSearchObservable() {
        RxSearchObservable.fromView(main_toolbar_search)
                .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .filter { searchText ->
                    main_recycler_events.smoothScrollToPosition(0)
                    mEndlessScrollListener.resetPaging()
                    (activity as MainActivity).lastDocument = null
                    (activity as MainActivity).search(searchText)
                    true
                }
                .distinctUntilChanged()
                .switchMap { return@switchMap Observable.fromArray(mEvents) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mAdapter.replaceData(mEvents)
                }
    }

    /**
     * Perform client-side filtering when filter positive button has been clicked. Due to firebase
     * limitations it has to be client-side.
     */
    private fun filter() {
        var filteredEvents = listOf<Event>()

//        mEvents.forEach {
//            mFilter?.let { filter ->
//                if (((it.date >= filter.timeFrom && it.date <= filter.timeTo) || (filter.timeTo == 0L && it.date >= filter.timeFrom) ||
//                                (filter.timeFrom == 0L && it.date <= filter.timeTo))
//                        && it.likes.size >= filter.likesFrom && (it.likes.size <= filter.likesTo || filter.likesTo == filterLikesTo)) {
//                    filteredEvents.add(it)
//                }
//            }
//        }

        mFilter?.let {
            (activity as MainActivity).getFilteredEvents(it).addOnCompleteListener{
                when{
                    it.isSuccessful -> {
                        it.result?.let {
                            Log.e("TTAAGG", it[0].eventUid)
                            filteredEvents = it
                        }
                    } else -> Toast.makeText(context, mEventsError,Toast.LENGTH_SHORT).show()
                }

                mAdapter.replaceData(filteredEvents)
                main_recycler_events.scrollToPosition(main_recycler_events.scrollY + 1)

//                switchEmptyView(filteredEvents as MutableList<Any>, main_recycler_events, main_view_emptyScreen)
                main_layout_swipe.isRefreshing = false
            }
        }
    }

    /**
     * Switches between visibilities of recycler and empty view based on list's size
     */
    private fun switchEmptyView(list: MutableList<Any>?, recycler: RecyclerView, emptyView: EmptyScreenView) {
        list?.let {
            if (it.isNotEmpty()) {
                recycler.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            } else {
                recycler.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    @Subscribe
    fun onEventsReceivedEvent(event: EventsReceivedEvent) {
        mEvents.clear()
        event.events.forEach {
            if (!mEvents.contains(it))
                mEvents.add(it)
        }

        if (!event.loadMore) {
            main_recycler_events.smoothScrollToPosition(0)
            mEndlessScrollListener.resetPaging()
        }

        if (mFilter == Filter() || mFilter == null)
            mAdapter.replaceData(mEvents)
        else
            filter()

        main_layout_swipe.isRefreshing = false
    }
}