package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.adapter.EventVotesAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.BottomOffsetDecoration
import com.aleksanderkapera.liveback.util.BUNDLE_EVENT_VOTE
import com.aleksanderkapera.liveback.util.asDimen
import com.aleksanderkapera.liveback.util.dpToPx
import com.aleksanderkapera.liveback.util.getNavigationBarHeight
import kotlinx.android.synthetic.main.fragment_event_vote.*
import java.io.Serializable

/**
 * Created by kapera on 27-Jul-18.
 */
class EventVoteFragment : BaseFragment() {

    private var mVotes = listOf<Vote>()
    lateinit var votesAdapter: EventVotesAdapter

    companion object {
        fun newInstance(votes: List<Vote>?): EventVoteFragment {
            val fragment = EventVoteFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_EVENT_VOTE, votes as Serializable)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_vote

    override fun setupViews(rootView: View) {
        mVotes = arguments?.get(BUNDLE_EVENT_VOTE) as List<Vote>
        initAdapter()
    }

    /**
     * Fill recycler with data
     */
    private fun initAdapter() {
        context?.let {
            votesAdapter = EventVotesAdapter(it)
            votesAdapter.replaceData(mVotes)
            val layout = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            eventVote_recycler_votes.layoutManager = layout
            eventVote_recycler_votes.adapter = votesAdapter
            eventVote_recycler_votes.addItemDecoration(BottomOffsetDecoration(getNavigationBarHeight()+ dpToPx(R.dimen.spacing16.asDimen().toInt())))
        }

        (parentFragment as EventFragment).switchEmptyView(mVotes as MutableList<Any>, eventVote_recycler_votes, eventVote_view_emptyScreen)
    }
}