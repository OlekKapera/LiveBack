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
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.app_bar_event.*
import kotlinx.android.synthetic.main.fragment_event.*


/**
 * Created by kapera on 27-Jul-18.
 */
class EventFragment : BaseFragment() {

    private val mAboutString = R.string.about.asString()
    private val mCommentsString = R.string.comments.asString()
    private val mVoteString = R.string.vote.asString()
    private val mErrorString = R.string.event_error.asString()

    private var isFaded = false
    private var isAnimating = false
    private var currentOffset = 0
    private val breakPoint = -dpToPx(48)

    private lateinit var mEventDoc: DocumentReference

    private var mEvent: Event? = null
    private var mComments: List<Comment>? = null
    private var mVotes: List<Vote>? = null

    private var currentTab = 0

    companion object {
        fun newInstance(event: Event): BaseFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_EVENT, event)
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
        }

        mEventDoc = FirebaseFirestore.getInstance().document("events/${mEvent?.eventUid}")
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

        event_fab.setOnClickListener(onFabClick)

        setToolbarViews()
        setToolbarAnimation()
        getComments()
    }

    /**
     * Fill toolbar with event data
     */
    private fun setToolbarViews() {
        mEvent?.let { event ->
            if (event.backgroundPicturePath.isNotEmpty()) {
                Glide.with(context)
                        .using(FirebaseImageLoader())
                        .load(FirebaseStorage.getInstance().getReference(event.backgroundPicturePath))
                        .into(event_image_background)
            } else
                setBackgroundCategory(event.category, event_image_background)

            if (event.userProfilePath.isNotEmpty()) {
                Glide.with(context)
                        .using(FirebaseImageLoader())
                        .load(FirebaseStorage.getInstance().getReference(event.userProfilePath))
                        .into(event_image_profile)
            } else
                event_image_profile.setImageDrawable(R.drawable.ic_round_user.asDrawable())

            event_text_eventName.text = event.title
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
        mEventDoc.collection("comments").get().addOnCompleteListener {
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
    private fun getVotes(){
        mEventDoc.collection("votes").get().addOnCompleteListener {
            when{
                it.isSuccessful -> {
                    mVotes = it.result.toObjects(Vote::class.java)
                    setupTabs()
                }
            }
            event_view_load.hide()
        }
    }

    /**
     * Setup tab layout and view pager
     */
    private fun setupTabs() {
        val adapter = ViewPagerAdapter(childFragmentManager)

        mEvent?.let {
            adapter.addFragment(EventAboutFragment.newInstance(it), mAboutString)
            adapter.addFragment(EventCommentsFragment.newInstance(mComments), mCommentsString)
            adapter.addFragment(EventVoteFragment.newInstance(), mVoteString)
        }

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
                        0 -> event_fab.setImageDrawable(R.drawable.ic_heart_outline.asDrawable())
                        else -> event_fab.setImageDrawable(R.drawable.ic_add.asDrawable())
                    }
                }
            }
        })
    }

    /**
     * Handle on fab clicks on individual tabs
     */
    private val onFabClick = View.OnClickListener {
        when (currentTab){
            0 -> {

            }
            1 -> {
                AddFeedbackDialogFragment.newInstance().show(fragmentManager, TAG_ADD_FEEDBACK)
            }
            2 -> {

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