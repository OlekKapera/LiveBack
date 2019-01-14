package com.aleksanderkapera.liveback.ui.fragment

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.NotificationCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventNotificationsReceiver
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.activity.AddEventActivity
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.widget.EmptyScreenView
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
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
    private val mCommentEditSuccString = R.string.comment_edit_successful.asString()
    private val mVoteEditSuccString = R.string.vote_edit_successful.asString()
    private val mCommentEditFailString = R.string.comment_edit_error.asString()
    private val mVoteEditFailString = R.string.vote_edit_error.asString()

    private var isFaded = false
    private var isAnimating = false
    private var currentOffset = 0
    private val breakPoint = -dpToPx(48)

    private lateinit var mFireStore: FirebaseFirestore
    private lateinit var mEventDoc: DocumentReference
    private lateinit var mFireMessaging: FirebaseMessaging

    var event: Event? = null
    private var mUser: User? = null
    private var mComments: MutableList<Comment>? = null
    private var mVotes: MutableList<Vote>? = null
    private var mIsLiked = false

    lateinit var commentFragment: EventCommentsFragment
    lateinit var votesFragment: EventVoteFragment

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
            event = it.getParcelable(BUNDLE_EVENT)
            mUser = it.getParcelable(BUNDLE_EVENT_USER)
        }

        mFireStore = FirebaseFirestore.getInstance()
        mEventDoc = mFireStore.document("events/${event?.eventUid}")
        mFireMessaging = FirebaseMessaging.getInstance()
    }

    @SuppressLint("RestrictedApi")
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

        event_layout_swipe.setOnRefreshListener { getComments() }

        if (LoggedUser.uid.isEmpty())
            event_fab.visibility = View.GONE
        else
            event_fab.visibility = View.VISIBLE

        event?.let {
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
    override fun positiveButtonClicked(comment: Comment?, vote: Vote?) {
        if (comment != null)
            addComment(comment)
        else if (vote != null)
            addVote(vote)
    }

    /**
     * Fill toolbar with event data
     */
    private fun setToolbarViews() {
        event?.let { event ->
            event_text_eventName.text = event.title

            context?.let { context ->
                if (event.backgroundPicturePath.isNotEmpty()) {
                    GlideApp.with(context)
                            .load(FirebaseStorage.getInstance().getReference(event.backgroundPicturePath))
                            .signature(ObjectKey(event.backgroundPictureTime.toString()))
                            .into(event_image_background)
                } else
                    setBackgroundCategory(event.category, event_image_background)

                mUser?.let { user ->
                    if (user.profilePicPath.isNotEmpty()) {
                        GlideApp.with(context)
                                .load(FirebaseStorage.getInstance().getReference(user.profilePicPath))
                                .signature(ObjectKey(user.profilePicTime.toString()))
                                .displayRoundPlaceholder()
                                .into(event_image_profile)
                    } else
                        event_image_profile.setImageDrawable(R.drawable.ic_user_round.asDrawable())

                    event_image_edit.visibility = when {
                        event.userUid == LoggedUser.uid -> View.VISIBLE
                        else -> View.GONE
                    }

                    event_image_edit.setOnClickListener { AddEventActivity.startActivity(activity as Activity, event) }
                    event_image_profile.setOnClickListener { (activity as MainActivity).showFragment(ProfileFragment.newInstance(user.uid)) }
                }
            }
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

            // enable swipe layout when layout scrolled position is top
            event_layout_swipe.isEnabled = Math.abs(verticalOffset) <= 5
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
                    mComments = it.result?.toObjects(Comment::class.java)
                    getVotes()
                }
                else -> {
                    event_view_load.hide()
                    showToast(mErrorString)
                    event_layout_swipe.isRefreshing = false
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
                    mVotes = it.result?.toObjects(Vote::class.java)
                    setupTabs()
                }
            }
            event_layout_swipe.isRefreshing = false
            event_view_load.hide()
        }
    }

    /**
     * Adding or deleting like. Also saving info about like into the BE of event and user
     */
    private fun switchLike() {
        event_view_load.show()

        event?.let { event ->
            if (event.userUid != LoggedUser.uid) {
                event.date.let {
                    if (!mIsLiked)
                        scheduleNotification(it, true)
                    else
                        scheduleNotification(it, false)
                }
            }
        }

        event?.let { event ->
            val batch = mFireStore.batch()
            val userRef = mFireStore.document("users/${LoggedUser.uid}")

            if (!mIsLiked) {
                batch.update(mEventDoc, "likes", FieldValue.arrayUnion(LoggedUser.uid))
                batch.update(userRef, "likedEvents", FieldValue.arrayUnion(event.eventUid))

                batch.commit().addOnCompleteListener {
                    event_view_load.hide()
                    when {
                        it.isSuccessful -> {
                            mIsLiked = true
                            event.likes.add(LoggedUser.uid)
                            event_fab.setImageDrawable(R.drawable.ic_heart_white.asDrawable())
                            eventAbout_container_likes.eventItem_text_description.text = R.plurals.event_likes.asPluralsString(event.likes.size)

                            LoggedUser.likedEvents.add(event.eventUid)
                        }
                    }
                }

                if (LoggedUser.voteAddedOnFav)
                    mFireMessaging.subscribeToTopic("V${event.eventUid}")
                if (LoggedUser.commentAddedOnFav)
                    mFireMessaging.subscribeToTopic("C${event.eventUid}")
            } else {
                batch.update(mEventDoc, "likes", FieldValue.arrayRemove(LoggedUser.uid))
                batch.update(userRef, "likedEvents", FieldValue.arrayRemove(event.eventUid))

                batch.commit().addOnCompleteListener {
                    event_view_load.hide()
                    when {
                        it.isSuccessful -> {
                            mIsLiked = false
                            event.likes.remove(LoggedUser.uid)
                            event_fab.setImageDrawable(R.drawable.ic_heart_outline.asDrawable())
                            eventAbout_container_likes.eventItem_text_description.text = R.plurals.event_likes.asPluralsString(event.likes.size)

                            LoggedUser.likedEvents.remove(event.eventUid)
                        }
                    }
                }

                if ((event.userUid != LoggedUser.uid) or (!LoggedUser.voteAddedOnYour and (event.userUid == LoggedUser.uid)))
                    mFireMessaging.unsubscribeFromTopic("V${event.eventUid}")

                if ((event.userUid != LoggedUser.uid) or (!LoggedUser.commentAddedOnYour and (event.userUid == LoggedUser.uid)))
                    mFireMessaging.unsubscribeFromTopic("C${event.eventUid}")
            }
        }
    }

    /**
     * Adding comment for event
     */
    private fun addComment(comment: Comment) {
        val newComment = comment.commentUid.isEmpty()

        val commentRef = when {
            comment.commentUid.isEmpty() -> mFireStore.collection("events/${event?.eventUid}/comments").document()
            else -> mFireStore.document("events/${event?.eventUid}/comments/${comment.commentUid}")
        }
        comment.commentUid = commentRef.id

        event_view_load.show()
        commentRef.set(comment).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    if (newComment) {
                        showToast(mCommentSuccString)
                        mComments?.let {
                            it.add(0, comment)
                            commentFragment.commentsAdapter.replaceData(it)
                        }
                        event?.let {
                            it.comments++
                            mFireStore.document("events/${event?.eventUid}").update("comments", it.comments)
                        }
                    } else {
                        showToast(mCommentEditSuccString)
                        mComments?.let { comments ->
                            for (index in 0 until comments.size) {
                                if (comments[index].commentUid == comment.commentUid) {
                                    comments.removeAt(index)
                                    comments.add(0, comment)
                                    break
                                }
                            }
                            commentFragment.commentsAdapter.replaceData(comments)
                        }
                    }
                }
                else -> if (comment.commentUid.isEmpty()) showToast(mCommentFailString) else showToast(mCommentEditFailString)
            }

            switchEmptyView(mComments as MutableList<Any>, eventComment_recycler_comments, eventComment_view_emptyScreen)
            event_view_load.hide()
        }
    }

    /**
     * Adding vote
     */
    private fun addVote(vote: Vote) {
        val newVote = vote.voteUid.isEmpty()

        val voteRef = when {
            vote.voteUid.isEmpty() -> mFireStore.collection("events/${event?.eventUid}/votes").document()
            else -> mFireStore.document("events/${event?.eventUid}/votes/${vote.voteUid}")
        }
        vote.voteUid = voteRef.id

        event_view_load.show()
        voteRef.set(vote).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    if (newVote) {
                        showToast(mVoteSuccString)
                        mVotes?.let {
                            it.add(0, vote)
                            votesFragment.votesAdapter.replaceData(it)
                        }
                        event?.let {
                            it.votes++
                            mFireStore.document("events/${it.eventUid}").update("votes", it.votes)
                        }
                    } else {
                        showToast(mVoteEditSuccString)
                        mVotes?.let { votes ->
                            for (index in 0 until votes.size) {
                                if (votes[index].voteUid == vote.voteUid) {
                                    votes.removeAt(index)
                                    votes.add(0, vote)
                                    break
                                }
                            }
                            votesFragment.votesAdapter.replaceData(votes)
                        }
                    }
                }
                else -> if (vote.voteUid.isEmpty()) showToast(mVoteFailString) else showToast(mVoteEditFailString)
            }

            switchEmptyView(mVotes as MutableList<Any>, eventVote_recycler_votes, eventVote_view_emptyScreen)
            event_view_load.hide()
        }
    }

    /**
     * Setup tab layout and view pager
     */
    private fun setupTabs() {
        event?.let {
            val adapter = ViewPagerAdapter(childFragmentManager)
            commentFragment = EventCommentsFragment.newInstance(mComments)
            votesFragment = EventVoteFragment.newInstance(mVotes, it.eventUid)

            adapter.addFragment(EventAboutFragment.newInstance(it), mAboutString)
            adapter.addFragment(commentFragment, mCommentsString)
            adapter.addFragment(votesFragment, mVoteString)

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
     * Create notification pending intent when user likes the event. If the event is disliked cancel
     * pending notification. Don't create alarm if the event already happened
     */
    private fun scheduleNotification(time: Long, set: Boolean) {
        val builder = NotificationCompat.Builder(context)
                .setContentTitle("${event?.title}")
                .setContentText(when{
                    LoggedUser.reminder < 60 -> "${R.string.event_you_liked.asString()} ${R.plurals.starts_in_minutes.asPluralsString(LoggedUser.reminder)}"
                    else ->"${R.string.event_you_liked.asString()} ${R.plurals.starts_in_hours.asPluralsString(LoggedUser.reminder / 60)}"
                })
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setChannelId(NOTIFICATION_REMINDER_CHANNEL)

        val applicationContext = activity?.applicationContext

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(INTENT_NOTIFICATION_EVENT, event)
        intent.putExtra(INTENT_NOTIFICATION_USER, mUser)
        val activity = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(activity)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(alarmSound)

        val notification = builder.build()

        val notificationIntent = Intent(applicationContext, EventNotificationsReceiver::class.java)
        notificationIntent.putExtra(NOTIFICATION_RECEIVER_ID, NOTIFICATION_ID_EVENT)
        notificationIntent.putExtra(NOTIFICATION_RECEIVER_TEXT, notification)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, NOTIFICATION_ID_EVENT, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = applicationContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (set) {
            if (time > (System.currentTimeMillis()  + (LoggedUser.reminder * 60000)) && LoggedUser.reminder != 0)
                alarmManager.set(AlarmManager.RTC_WAKEUP, time - (LoggedUser.reminder * 60000), pendingIntent)
        } else
            alarmManager.cancel(pendingIntent)
    }

    /**
     * Handle on fab clicks on individual tabs
     */
    private val onFabClick = View.OnClickListener {
        when (currentTab) {
            0 -> switchLike()
            1 -> {
                val dialog = AddFeedbackDialogFragment.newInstance(AddFeedbackDialogType.COMMENT, null, null)
                dialog.setTargetFragment(this, REQUEST_TARGET_EVENT_FRAGMENT)
                dialog.show(fragmentManager, TAG_ADD_FEEDBACK)
            }
            2 -> {
                val dialog = AddFeedbackDialogFragment.newInstance(AddFeedbackDialogType.VOTE, null, null)
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