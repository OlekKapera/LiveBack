package com.aleksanderkapera.liveback.ui.fragment

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.activity.SettingsActivity
import com.aleksanderkapera.liveback.ui.activity.SettingsCaller
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.EmptyScreenView
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.app_bar_profile.*
import kotlinx.android.synthetic.main.fragment_event_comments.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile_events.*

/**
 * Created by kapera on 18-Aug-18.
 */
class ProfileFragment : BaseFragment() {

    private val mCommentsString = R.string.comments.asString()
    private val mEventsString = R.string.events.asString()
    private val mProfileErrorString = R.string.event_error.asString()
    private val mEventsErrorString = R.string.events_error.asString()
    private val mCommentsErrorString = R.string.comments_error.asString()

    private val mProfilePhoto = R.drawable.ic_user_round_solid.asDrawable()
    private val mBackgroundPhoto = R.drawable.bg_add_event.asDrawable()

    private var isFaded = false
    private var isAnimating = false
    private var currentOffset = 0
    private val breakPoint = -dpToPx(48)

    var mUserid = ""
    private var mUser: User? = null
    private var mComments = mutableListOf<Comment>()
    private var mEvents = mutableListOf<Event>()
    private var mEventsRef = mutableListOf<DocumentReference>()

    private lateinit var mFireStore: FirebaseFirestore
    private lateinit var mCommentsFragment: EventCommentsFragment
    private lateinit var mEventsFragment: ProfileEventsFragment

    companion object {
        fun newInstance(userId: String): ProfileFragment {
            val fragment = ProfileFragment()
            val bundle = Bundle()

            bundle.putString(BUNDLE_PROFILE_USER, userId)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve user id
        arguments?.let {
            mUserid = it.getString(BUNDLE_PROFILE_USER)
        }

        mFireStore = FirebaseFirestore.getInstance()
    }

    override fun setupViews(rootView: View) {
        appCompatActivity.setSupportActionBar(profile_layout_toolbar)
        val actionbar = appCompatActivity.supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        // move only toolbar below status bar
        val toolbarParams = profile_layout_toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        profile_layout_toolbar.layoutParams = toolbarParams

        profile_layout_swipe.setOnRefreshListener { getProfile() }

        setToolbarAnimation()
        setupTabs()

        profile_button_settings.setOnClickListener { SettingsActivity.startActivity(activity as Activity, SettingsCaller.PROFILE_FRAGMENT) }
    }

    override fun onResume() {
        super.onResume()
        getProfile()
    }

    /**
     * Fill toolbar's images and user name with data retrieved
     */
    private fun setupToolbar() {
        mUser?.let { user ->

            profile_text_profileName.text = user.username

            if (user.uid == LoggedUser.uid)
                profile_button_settings.visibility = View.VISIBLE
            else
                profile_button_settings.visibility = View.GONE

            if (user.profilePicPath.isNotEmpty()) {
                val storageRef = FirebaseStorage.getInstance().getReference(user.profilePicPath)

                Glide.with(context)
                        .using(FirebaseImageLoader())
                        .load(storageRef)
                        .signature(StringSignature(user.profilePicTime.toString()))
                        .into(profile_image_profile)

                Glide.with(context)
                        .using(FirebaseImageLoader())
                        .load(storageRef)
                        .signature(StringSignature(user.profilePicTime.toString()))
                        .into(profile_image_background)
            } else {
                profile_image_profile.setImageDrawable(mProfilePhoto)
                profile_image_background.setImageDrawable(mBackgroundPhoto)
            }
        }
    }

    /**
     * Set toolbar animation
     */
    private fun setToolbarAnimation() {
        val view = profile_layout_header

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

            // enable swipe layout when layout scrolled position is top
            profile_layout_swipe.isEnabled = Math.abs(verticalOffset) <= 5
        }

        profile_layout_appBar.addOnOffsetChangedListener(listener)
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
            if (Math.abs(currentOffset) - profile_layout_appBar.totalScrollRange > breakPoint && !isFaded) {
                //Collapsed
                fadeIn(view, currentOffset)
            } else if (Math.abs(currentOffset) - profile_layout_appBar.totalScrollRange < (breakPoint - 20) && isFaded) {
                //Expanded
                fadeOut(view, currentOffset)
            }
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
     * Setup tab layout and view pager
     */
    private fun setupTabs() {
        val adapter = EventFragment.ViewPagerAdapter(childFragmentManager)
        mEventsFragment = ProfileEventsFragment.newInstance(mEvents)
        mCommentsFragment = EventCommentsFragment.newInstance(mComments)

        adapter.addFragment(mEventsFragment, mEventsString)
        adapter.addFragment(mCommentsFragment, mCommentsString)

        profile_layout_viewPager.adapter = adapter
        profile_layout_tabs.setupWithViewPager(profile_layout_viewPager)
    }

    /**
     * Retrieves profile information about particular user
     */
    private fun getProfile() {
        profile_view_load.show()
        mFireStore.document("users/$mUserid").get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    mUser = it.result.toObject(User::class.java)
                    setupToolbar()
                    getEvents()
                }
                else -> {
                    showToast(mProfileErrorString)
                    profile_view_load.hide()
                    profile_layout_swipe.isRefreshing = false
                }
            }
        }
    }

    /**
     * Retrieve user's events
     */
    private fun getEvents() {
        mEventsRef.clear()
        mFireStore.collection("events").whereEqualTo("userUid", mUserid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            querySnapshot?.documents?.forEach {
                mEventsRef.add(it.reference)
            }
        }

        mFireStore.collection("events").whereEqualTo("userUid", mUserid).get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    mEvents = it.result.toObjects(Event::class.java)
                    mEventsFragment.mEventsAdapter.replaceData(mEvents)
                    if (mEvents.isNotEmpty())
                        getComments()
                    else {
                        profile_view_load.hide()
                        profile_layout_swipe.isRefreshing = false
                    }
                }
                else -> {
                    showToast(mEventsErrorString)
                    profile_view_load.hide()
                    profile_layout_swipe.isRefreshing = false
                }
            }

            switchEmptyView(mEvents as MutableList<Any>, profileEvents_recycler_events, profileEvents_view_emptyScreen)
        }
    }

    /**
     * Retrieves comments on user's events
     */
    private fun getComments() {
        mComments.clear()
        mEventsRef.forEach { docRef ->
            docRef.collection("comments").get().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result.toObjects(Comment::class.java).forEach {
                            mComments.add(it)
                        }
                        if (docRef == mEventsRef.last()) {
                            profile_view_load.hide()
                            mCommentsFragment.commentsAdapter.replaceData(mComments)
                            switchEmptyView(mComments as MutableList<Any>, eventComment_recycler_comments, eventComment_view_emptyScreen)
                        }
                    }
                    else -> {
                        if (docRef == mEventsRef.last()) {
                            showToast(mCommentsErrorString)
                            profile_view_load.hide()
                            switchEmptyView(mComments as MutableList<Any>, profileEvents_recycler_events, profileEvents_view_emptyScreen)
                        }
                    }
                }
                profile_layout_swipe.isRefreshing = false
            }
        }
    }
}