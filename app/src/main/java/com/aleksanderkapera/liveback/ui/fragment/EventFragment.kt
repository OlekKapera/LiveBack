package com.aleksanderkapera.liveback.ui.fragment

import android.animation.Animator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.EmptyScreenView
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.app_bar_event.*
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event_about.*
import kotlinx.android.synthetic.main.fragment_event_comments.*
import kotlinx.android.synthetic.main.fragment_event_vote.*
import kotlinx.android.synthetic.main.item_event_about.view.*


/**
 * Created by kapera on 27-Jul-18.
 */
class EventFragment : BaseFragment(), AddFeedbackDialogFragment.FeedbackSentListener {

    private val mAboutString = R.string.about.asString()
    private val mCommentsString = R.string.comments.asString()
    private val mVoteString = R.string.vote.asString()
    private val mErrorString = R.string.event_error.asString()
    private val mCommentSuccString = R.string.comment_successful.asString()
    private val mVoteSuccString = R.string.vote_successful.asString()
    private val mCommentFailString = R.string.comment_add_error.asString()
    private val mVoteFailString = R.string.vote_error.asString()

    private var isFaded = false
    private var isAnimating = false
    private var currentOffset = 0
    private val breakPoint = -dpToPx(48)

    private lateinit var mFireStore: FirebaseFirestore
    private lateinit var mEventDoc: DocumentReference

    private var mEvent: Event? = null
    private var mUser: User? = null
    private var mComments: MutableList<Comment>? = null
    private var mVotes: MutableList<Vote>? = null
    private var mIsLiked = false

    private lateinit var mCommentFragment: EventCommentsFragment
    private lateinit var mVotesFragment: EventVoteFragment

    private var currentTab = 0

    companion object {
        fun newInstance(event: Event, user: User): BaseFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_EVENT, event)
            bundle.putParcelable(BUNDLE_EVENT_USER, user)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //retrieve event from main fragment
        arguments?.let {
            mEvent = it.getParcelable(BUNDLE_EVENT)
            mUser = it.getParcelable(BUNDLE_EVENT_USER)
        }

        mFireStore = FirebaseFirestore.getInstance()
        mEventDoc = mFireStore.document("events/${mEvent?.eventUid}")
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

        // move only toolbar below status bar
        val toolbarParams = event_layout_toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        event_layout_toolbar.layoutParams = toolbarParams

        // move fab and cards above navigation bar
        val fabParams = event_fab.layoutParams as CoordinatorLayout.LayoutParams
        fabParams.setMargins(0, 0, dpToPx(R.dimen.spacing8.asDimen().toInt()), getNavigationBarHeight())
        event_fab.layoutParams = fabParams

        if (LoggedUser.uid.isEmpty())
            event_fab.visibility = View.GONE
        else
            event_fab.visibility = View.VISIBLE

        mEvent?.let {
            mIsLiked = it.likes.contains(LoggedUser.uid)
            if (mIsLiked)
                event_fab.setImageDrawable(R.drawable.ic_heart_white.asDrawable())
            else
                event_fab.setImageDrawable(R.drawable.ic_heart_outline.asDrawable())

            getComments()
        }

        event_fab.setOnClickListener(onFabClick)

        setToolbarViews()
        setToolbarAnimation()
    }

    /**
     * Handle events when user approves dialog messages
     */
    override fun positiveButtonClicked(title: String, description: String) {
        if (title.isEmpty())
            addComment(description)
        else
            addVote(title, description)
    }

    /**
     * Fill toolbar with event data
     */
    private fun setToolbarViews() {
        mEvent?.let { event ->
            event_text_eventName.text = event.title

            if (event.backgroundPicturePath.isNotEmpty()) {
                Glide.with(context)
                        .using(FirebaseImageLoader())
                        .load(FirebaseStorage.getInstance().getReference(event.backgroundPicturePath))
                        .into(event_image_background)
            } else
                setBackgroundCategory(event.category, event_image_background)
        }
        mUser?.let { user ->
            user.profilePicPath?.let {
                if (it.isNotEmpty()) {
                    Glide.with(context)
                            .using(FirebaseImageLoader())
                            .load(FirebaseStorage.getInstance().getReference(it))
                            .signature(StringSignature(user.profilePicTime.toString()))
                            .into(event_image_profile)
                } else
                    event_image_profile.setImageDrawable(R.drawable.ic_user_round.asDrawable())
            }

            event_image_profile.setOnClickListener { (activity as MainActivity).showFragment(ProfileFragment.newInstance(user.uid)) }
        }
    }

    /**
     * Set toolbar animation
     */
    private fun setToolbarAnimation() {
        val view = event_layout_header

        val listener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            currentOffset = verticalOffset

            if (!isAnimating) {
                if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange > breakPoint && !isFaded) {
                    //Collapsed
                    fadeIn(view, verticalOffset)
                } else if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange < (breakPoint - 20) && isFaded) {
                    //Expanded
                    fadeOut(view, verticalOffset)
                }
            }
        }

        event_layout_appBar.addOnOffsetChangedListener(listener)
    }

    /**
     * Fade in a view
     */
    private fun fadeIn(view: View, offset: Int) {
        view.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        isFaded = true
                        isAnimating = false

                        checkIfMoved(view, offset)
                    }

                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {
                        isAnimating = true
                    }
                })
    }

    /**
     * Fade in a view
     */
    private fun fadeOut(view: View, offset: Int) {
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        isFaded = false
                        isAnimating = false

                        checkIfMoved(view, offset)
                    }

                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {
                        isAnimating = true
                    }
                })
    }

    /**
     * Checks if user has swiped and calls animation
     */
    private fun checkIfMoved(view: View, offset: Int) {
        if (currentOffset != offset) {
            if (Math.abs(currentOffset) - event_layout_appBar.totalScrollRange > breakPoint && !isFaded) {
                //Collapsed
                fadeIn(view, currentOffset)
            } else if (Math.abs(currentOffset) - event_layout_appBar.totalScrollRange < (breakPoint - 20) && isFaded) {
                //Expanded
                fadeOut(view, currentOffset)
            }
        }
    }

    /**
     * Retrieve all comments for this event
     */
    private fun getComments() {
        event_view_load.show()
        mEventDoc.collection("comments").orderBy("postedTime", Query.Direction.DESCENDING).get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    mComments = it.result.toObjects(Comment::class.java)
                    getVotes()
                }
                else -> {
                    event_view_load.hide()
                    showToast(mErrorString)
                }
            }
        }
    }

    /**
     * Retrieve all votes for this event
     */
    private fun getVotes() {
        mEventDoc.collection("votes").get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    mVotes = it.result.toObjects(Vote::class.java)
                    setupTabs()
                }
            }
            event_view_load.hide()
        }
    }

    /**
     * Adding or deleting switchLike
     */
    private fun switchLike() {
        event_view_load.show()

        if (!mIsLiked) {
            mFireStore.document("events/${mEvent?.eventUid}").update("likes", FieldValue.arrayUnion(LoggedUser.uid)).addOnCompleteListener {
                event_view_load.hide()
                when {
                    it.isSuccessful -> {
                        mIsLiked = true
                        mEvent?.let {
                            it.likes.add(LoggedUser.uid)
                            event_fab.setImageDrawable(R.drawable.ic_heart_white.asDrawable())
                            eventAbout_container_likes.eventItem_text_description.text = R.plurals.event_likes.asPluralsString(it.likes.size)
                        }
                    }
                }
            }
        } else {
            mFireStore.document("events/${mEvent?.eventUid}").update("likes", FieldValue.arrayRemove(LoggedUser.uid)).addOnCompleteListener {
                event_view_load.hide()
                when {
                    it.isSuccessful -> {
                        mIsLiked = false
                        mEvent?.let {
                            it.likes.remove(LoggedUser.uid)
                            event_fab.setImageDrawable(R.drawable.ic_heart_outline.asDrawable())
                            eventAbout_container_likes.eventItem_text_description.text = R.plurals.event_likes.asPluralsString(it.likes.size)
                        }
                    }
                }
            }
        }
    }

    /**
     * Adding comment for event
     */
    private fun addComment(review: String) {
        val commentRef = mFireStore.collection("events/${mEvent?.eventUid}/comments").document()

        val commentPojo = Comment(commentRef.id, review, System.currentTimeMillis(), LoggedUser.uid)

        event_view_load.show()
        commentRef.set(commentPojo).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    showToast(mCommentSuccString)
                    mComments?.let {
                        it.add(commentPojo)
                        mCommentFragment.commentsAdapter.replaceData(it)
                    }
                    mEvent?.let {
                        it.comments++
                        mFireStore.document("events/${mEvent?.eventUid}").update("comments", it.comments)
                    }
                }
                else -> showToast(mCommentFailString)
            }

            switchEmptyView(mComments as MutableList<Any>, eventComment_recycler_comments, eventComment_view_emptyScreen)
            event_view_load.hide()
        }
    }

    /**
     * Adding vote
     */
    private fun addVote(title: String, description: String) {
        val voteRef = mFireStore.collection("events/${mEvent?.eventUid}/votes").document()

        val votePojo = Vote(voteRef.id, title, description, LoggedUser.uid, mutableListOf(), mutableListOf())
        event_view_load.show()
        voteRef.set(votePojo).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    showToast(mVoteSuccString)
                    mVotes?.let {
                        it.add(votePojo)
                        mVotesFragment.votesAdapter.replaceData(it)
                    }
                    mEvent?.let {
                        it.votes++
                        mFireStore.document("events/${it.eventUid}").update("votes", it.votes)
                    }
                }
                else -> showToast(mVoteFailString)
            }

            switchEmptyView(mVotes as MutableList<Any>, eventVote_recycler_votes, eventVote_view_emptyScreen)
            event_view_load.hide()
        }
    }

    /**
     * Setup tab layout and view pager
     */
    private fun setupTabs() {
        mEvent?.let {
            val adapter = ViewPagerAdapter(childFragmentManager)
            mCommentFragment = EventCommentsFragment.newInstance(mComments)
            mVotesFragment = EventVoteFragment.newInstance(mVotes, it.eventUid)

            adapter.addFragment(EventAboutFragment.newInstance(it), mAboutString)
            adapter.addFragment(mCommentFragment, mCommentsString)
            adapter.addFragment(mVotesFragment, mVoteString)

            event_layout_viewPager.adapter = adapter
            event_layout_tabs.setupWithViewPager(event_layout_viewPager)

            event_layout_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                // set fab drawable according to current tab
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let {
                        currentTab = it
                        when (it) {
                            0 -> {
                                if (mIsLiked)
                                    event_fab.setImageDrawable(R.drawable.ic_heart_white.asDrawable())
                                else
                                    event_fab.setImageDrawable(R.drawable.ic_heart_outline.asDrawable())
                            }
                            else -> event_fab.setImageDrawable(R.drawable.ic_add.asDrawable())
                        }
                    }
                }
            })
        }
    }

    /**
     * Switches between visibilities of recycler and empty view based on list's size
     */
    fun switchEmptyView(list: MutableList<Any>?, recycler: RecyclerView, emptyView: EmptyScreenView) {
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

    /**
     * Handle on fab clicks on individual tabs
     */
    private val onFabClick = View.OnClickListener {
        when (currentTab) {
            0 -> {
                switchLike()
            }
            1 -> {
                val dialog = AddFeedbackDialogFragment.newInstance(AddFeedbackDialogType.COMMENT)
                dialog.setTargetFragment(this, REQUEST_TARGET_EVENT_FRAGMENT)
                dialog.show(fragmentManager, TAG_ADD_FEEDBACK)
            }
            2 -> {
                val dialog = AddFeedbackDialogFragment.newInstance(AddFeedbackDialogType.VOTE)
                dialog.setTargetFragment(this, REQUEST_TARGET_EVENT_FRAGMENT)
                dialog.show(fragmentManager, TAG_ADD_FEEDBACK)
            }
        }
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val mFragmentList = mutableListOf<Fragment>()
        private val mFragmentListTitle = mutableListOf<String>()

        override fun getItem(position: Int): Fragment = mFragmentList[position]

        override fun getCount(): Int = mFragmentList.size

        override fun getPageTitle(position: Int): CharSequence? = mFragmentListTitle[position]

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentListTitle.add(title)
        }
    }
}