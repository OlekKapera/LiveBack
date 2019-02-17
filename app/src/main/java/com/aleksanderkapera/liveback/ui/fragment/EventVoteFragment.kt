package com.aleksanderkapera.liveback.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.adapter.EventVotesAdapter
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.BottomOffsetDecoration
import com.aleksanderkapera.liveback.util.*
import kotlinx.android.synthetic.main.fragment_event_comments.*
import kotlinx.android.synthetic.main.fragment_event_vote.*
import java.io.Serializable

/**
 * Created by kapera on 27-Jul-18.
 */
class EventVoteFragment : BaseFragment() {

    var votes = mutableListOf<Vote>()
    private var mEventUid = ""
    lateinit var votesAdapter: EventVotesAdapter

    companion object {
        fun newInstance(votes: List<Vote>?, eventUid: String): EventVoteFragment {
            val fragment = EventVoteFragment()
            val bundle = Bundle()

            bundle.putSerializable(BUNDLE_EVENT_VOTE, votes as Serializable)
            bundle.putString(BUNDLE_EVENT_VOTE_UID, eventUid)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event_vote

    override fun setupViews(rootView: View) {
        votes = arguments?.get(BUNDLE_EVENT_VOTE) as MutableList<Vote>
        mEventUid = arguments?.get(BUNDLE_EVENT_VOTE_UID) as? String ?: ""
        initAdapter()
    }

    /**
     * Fill recycler with data
     */
    private fun initAdapter() {
        context?.let {
            votesAdapter = EventVotesAdapter(it, mEventUid, parentFragment as EventFragment)
            votesAdapter.replaceData(votes.sortedByDescending { vote -> vote.upVotes.size - vote.downVotes.size })
            val layout = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
            eventVote_recycler_votes.layoutManager = layout
            eventVote_recycler_votes.adapter = votesAdapter
            eventVote_recycler_votes.addItemDecoration(BottomOffsetDecoration(getNavigationBarHeight()+ dpToPx(R.dimen.spacing16.asDimen().toInt())))
        }

        (parentFragment as EventFragment).switchEmptyView(votes as MutableList<Any>, eventVote_recycler_votes, eventVote_view_emptyScreen)
    }

    /**
     * Add newly added vote to the end of a list
     */
    fun addData(vote: Vote) {
        votesAdapter.addItem(votes.size-1, vote)
        votes.add(vote)
    }
}