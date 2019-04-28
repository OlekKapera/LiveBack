package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventsReceivedEvent
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.Filter
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.fragment.EventFragment
import com.aleksanderkapera.liveback.ui.fragment.MainFragment
import com.aleksanderkapera.liveback.ui.fragment.SortType
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
import com.aleksanderkapera.liveback.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.EventBus


class MainActivity : FragmentActivity() {

    private val mGenericErrorString = R.string.generic_error.asString()

    companion object {
        fun startActivity(activity: Activity, anonymousUser: Boolean) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(INTENT_MAIN_LOGGING, anonymousUser)
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity, anonymousUser: Boolean, filter: Filter) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(INTENT_MAIN_LOGGING, anonymousUser)
            intent.putExtra(INTENT_MAIN_FILTER, filter)
            activity.startActivity(intent)
        }
    }

    private lateinit var mNavigationDrawer: NavigationViewHelper
    private lateinit var mDrawerLayout: DrawerLayout

    lateinit var mAuth: FirebaseAuth
    private lateinit var mFireStoreRef: FirebaseFirestore
    private var mEventsCol: CollectionReference? = null
    private lateinit var mEvents: MutableList<Event>

    var lastDocument: DocumentSnapshot? = null
    private var mUser: User? = null
    private var mStorageRef: StorageReference? = null
    private var isAnonymousUser = false
    private val mEventsPerPage = 8
    private lateinit var mOrderString: String
    private lateinit var mOrderDirection: Query.Direction

    override fun getLayoutRes(): Int = R.layout.activity_main

    override fun getDefaultFragment(): BaseFragment = MainFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mFireStoreRef = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        mFireStoreRef.firestoreSettings = settings

        //get intent extras regarding from where it was called
        isAnonymousUser = intent.getBooleanExtra(INTENT_MAIN_LOGGING, false)

        openEvent()
        createNotificationChannels()

        intent.getStringExtra(INTENT_NOTIFICATION_EVENTUID)?.let {
            showEventFragment(it)
        }

        //when no user is logged in open login fragment
        if (mAuth.currentUser != null || isAnonymousUser) {
            mEventsCol = mFireStoreRef.collection("events")
            mAuth.currentUser?.let {
                getUser(it.uid)
            }

            mDrawerLayout = main_layout_drawer
            mNavigationDrawer = NavigationViewHelper(this, mDrawerLayout)

        } else
            SigningActivity.startActivity(this)
    }

    override fun onBackPressed() {
        if (main_layout_drawer.isDrawerOpen(GravityCompat.START))
            main_layout_drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * If activity contains intents with event and user open event fragment directly. If it contains
     * eventUid fetch it with user and then open fragment.
     */
    private fun openEvent() {
        val notificationEvent = intent.getParcelableExtra<Event>(INTENT_NOTIFICATION_EVENT)
        val notificationUser = intent.getParcelableExtra<User>(INTENT_NOTIFICATION_USER)

        notificationEvent?.let { event ->
            notificationUser?.let { user ->
                showFragment(EventFragment.newInstance(event, user))
            }
        }

        val notificationEventUid = intent.getStringExtra(INTENT_NOTIFICATION_EVENTUID)
        notificationEventUid?.let { eventUid ->
            var event: Event
            var user: User
            mFireStoreRef.document("events/$eventUid").get().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result?.toObject(Event::class.java)?.let {
                            event = it
                            mFireStoreRef.document("users/${event.userUid}").get().addOnCompleteListener {
                                when {
                                    it.isSuccessful -> {
                                        it.result?.toObject(User::class.java)?.let {
                                            user = it
                                            showFragment(EventFragment.newInstance(event, user))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Perform search action for events.
     */
    fun search(query: String) {
        main_view_load.show()

        mEventsCol?.let { eventsCol ->
            lastDocument?.let {
                eventsCol.orderBy("title", Query.Direction.ASCENDING)
                        .startAt(query)
                        .endAt("$query\uf8ff")
                        .limit(mEventsPerPage.toLong())
                        .startAfter(it)
                        .get().addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    it.result?.let {
                                        mEvents.addAll(it.toObjects(Event::class.java))
                                        sendEvents(it, true)
                                    }
                                }
                                else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                            }

                            //hide loader
                            main_view_load.hide()
                        }
            } ?: run {
                eventsCol.orderBy("title", Query.Direction.ASCENDING)
                        .startAt(query)
                        .endAt("$query\uf8ff")
                        .limit(mEventsPerPage.toLong())
                        .get().addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    it.result?.let {
                                        mEvents = it.toObjects(Event::class.java)
                                        sendEvents(it, false)
                                    }
                                }
                                else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                            }

                            //hide loader
                            main_view_load.hide()
                        }
            }
        }
    }

    /**
     * Retrieve events from database. When lastDocument is not null it means that initial call has been made
     * and it's calling for adding more events.
     *
     * @param sortBy defines by what user wants to have events ordered
     * @param directionAsc order direction ascending or descending
     * @param flushEvents delete all existing events and replace them with new ones
     */
    fun getEvents(sortBy: SortType? = SortType.DATE, directionAsc: Boolean? = true, flushEvents: Boolean = true) {
        main_view_load.show()

        mOrderString = when (sortBy) {
            SortType.TITLE -> "title"
            SortType.LIKES -> "likes"
            else -> "date"
        }

        mOrderDirection = when (directionAsc) {
            true -> Query.Direction.ASCENDING
            else -> Query.Direction.DESCENDING
        }

        //get logged user's events
        mEventsCol?.whereEqualTo("userUid", mAuth.currentUser?.uid)?.get()?.addOnSuccessListener {
            LoggedUser.yourEvents = mutableListOf()

            it.toObjects(Event::class.java).forEach {
                LoggedUser.yourEvents.add(it.eventUid)
            }
        }

        if (flushEvents)
            lastDocument = null

        mEventsCol?.let { eventsCol ->
            lastDocument?.let {lastDocument ->
                eventsCol.orderBy(mOrderString, mOrderDirection)
                        .startAfter(lastDocument)
                        .limit(mEventsPerPage.toLong())
                        .get().addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    it.result?.let {
                                        mEvents.addAll(it.toObjects(Event::class.java))
                                        sendEvents(it, true)
                                    }
                                }
                                else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                            }

                            //hide loader
                            main_view_load.hide()
                        }
            } ?: run {
                eventsCol.orderBy(mOrderString, mOrderDirection)
                        .limit(mEventsPerPage.toLong())
                        .get().addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    it.result?.let {
                                        mEvents = it.toObjects(Event::class.java)
                                        sendEvents(it, true)
                                    }
                                }
                                else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                            }

                            //hide loader
                            main_view_load.hide()
                        }
            }
        }
    }

    /**
     * Create notification channels for all notification types
     */
    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel(NOTIFICATION_COMMENT_CHANNEL, R.string.notification_comment_channel.asString(), NotificationManager.IMPORTANCE_DEFAULT))
            notificationManager.createNotificationChannel(NotificationChannel(NOTIFICATION_VOTE_CHANNEL, R.string.notification_vote_channel.asString(), NotificationManager.IMPORTANCE_DEFAULT))
            notificationManager.createNotificationChannel(NotificationChannel(NOTIFICATION_REMINDER_CHANNEL, R.string.notification_reminder_channel.asString(), NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    /**
     * Switch visibility of empty events view and recycler view and send events to @MainFragment
     */
    private fun sendEvents(querySnapshot: QuerySnapshot, loadMore: Boolean) {
        if (!mEvents.isEmpty()) {
            main_view_emptyScreen.visibility = View.GONE
            main_recycler_events.visibility = View.VISIBLE
            if (querySnapshot.documents.isNotEmpty())
                lastDocument = querySnapshot.documents.last()
            EventBus.getDefault().post(EventsReceivedEvent(mEvents, loadMore))
        } else {
            //show empty screen
            main_view_emptyScreen.visibility = View.VISIBLE
            main_recycler_events.visibility = View.GONE
        }
    }

    /**
     * Get specific event and user and then open event fragment
     */
    private fun showEventFragment(eventUid: String) {
        mFireStoreRef.document("events/$eventUid").get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    it.result?.toObject(Event::class.java)?.let { event ->
                        mFireStoreRef.document("users/${event.userUid}").get().addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    it.result?.toObject(User::class.java)?.let { user ->
                                        showFragment(EventFragment.newInstance(event, user))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieve current user
     */
    private fun getUser(uid: String) {
        mFireStoreRef.document("users/$uid").get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    mUser = it.result?.toObject(User::class.java)
                    mUser?.let { user ->
                        LoggedUser.username = user.username
                        LoggedUser.email = user.email
                        LoggedUser.uid = user.uid
                        LoggedUser.profilePicTime = user.profilePicTime

                        if (user.profilePicPath.isNotEmpty())
                            mStorageRef = FirebaseStorage.getInstance().getReference(user.profilePicPath)
                        LoggedUser.profilePicPath = user.profilePicPath

                        LoggedUser.commentAddedOnYour = user.commentAddedOnYour
                        LoggedUser.commentAddedOnFav = user.commentAddedOnFav
                        LoggedUser.voteAddedOnYour = user.voteAddedOnYour
                        LoggedUser.voteAddedOnFav = user.voteAddedOnFav
                        LoggedUser.reminder = user.reminder
                        LoggedUser.likedEvents = user.likedEvents
                    }
                }
                else -> showToast(R.string.getUser_error)
            }
            mNavigationDrawer.updateViews(mUser, mStorageRef)
        }
    }

    /**
     * Perform log out action
     */
    fun logOut() {
        if (mAuth.currentUser != null) {
            mAuth.signOut()
            showToast(R.string.signed_out)
            mNavigationDrawer.updateViews(null, null)
        }
    }
}
