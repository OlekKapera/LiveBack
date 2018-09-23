package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventsReceivedEvent
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.Filter
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.fragment.MainFragment
import com.aleksanderkapera.liveback.ui.fragment.SortType
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
import com.aleksanderkapera.liveback.util.INTENT_MAIN_FILTER
import com.aleksanderkapera.liveback.util.INTENT_MAIN_LOGGING
import com.aleksanderkapera.liveback.util.LoggedUser
import com.aleksanderkapera.liveback.util.asString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
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
    private lateinit var mEventsCol: CollectionReference
    private lateinit var mEvents: MutableList<Event>

    var lastDocument: DocumentSnapshot? = null
    private var mUser: User? = null
    private var mStorageRef: StorageReference? = null
    private var isAnonymousUser = false
    private val mEventsPerPage = 5
    private lateinit var mFilter: Filter

    override fun getLayoutRes(): Int = R.layout.activity_main

    override fun getDefaultFragment(): BaseFragment = MainFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mFireStoreRef = FirebaseFirestore.getInstance()

        isAnonymousUser = intent.getBooleanExtra(INTENT_MAIN_LOGGING, false)
        mFilter = intent.getParcelableExtra(INTENT_MAIN_FILTER) ?: Filter()

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
     * Perform search action for events.
     */
    fun search(query: String) {
        main_view_load.show()

        lastDocument?.let {
            mEventsCol
                    .orderBy("title", Query.Direction.ASCENDING)
                    .startAt(query)
                    .endAt("$query\uf8ff")
                    .limit(mEventsPerPage.toLong())
                    .startAfter(it)
                    .get().addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                mEvents.addAll(it.result.toObjects(Event::class.java))
                                if (it.result.documents.isNotEmpty())
                                    lastDocument = it.result.documents.last()

                                EventBus.getDefault().post(EventsReceivedEvent(mEvents))
                            }
                            else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                        }

                        //hide loader
                        main_view_load.hide()
                    }
        } ?: run {
            mEventsCol
                    .orderBy("title", Query.Direction.ASCENDING)
                    .startAt(query)
                    .endAt("$query\uf8ff")
                    .limit(mEventsPerPage.toLong())
                    .get().addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                mEvents = it.result.toObjects(Event::class.java)
                                if (it.result.documents.isNotEmpty())
                                    lastDocument = it.result.documents.last()

                                EventBus.getDefault().post(EventsReceivedEvent(mEvents))
                            }
                            else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                        }

                        //hide loader
                        main_view_load.hide()
                    }
        }
    }

    /**
     * Retrieve events from database. When lastDocument is not null it means that initial call has been made
     * and it's calling for adding more events.
     */
    fun getEvents() {
        main_view_load.show()

        val orderString = when (mFilter.sortBy) {
            SortType.TITLE -> "title"
            SortType.LIKES -> "likes"
            else -> "date"
        }

        val orderDirection = when (mFilter.directionAsc) {
            true -> Query.Direction.ASCENDING
            else -> Query.Direction.DESCENDING
        }

        lastDocument?.let {
            mEventsCol
                    .whereGreaterThanOrEqualTo("likes", mFilter.likesFrom)
                    .whereLessThanOrEqualTo("likes", mFilter.likesTo)
                    .orderBy(orderString, orderDirection)
                    .limit(mEventsPerPage.toLong())
                    .startAfter(it)
                    .get().addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                mEvents.addAll(it.result.toObjects(Event::class.java))
                                if (it.result.documents.isNotEmpty())
                                    lastDocument = it.result.documents.last()

                                EventBus.getDefault().post(EventsReceivedEvent(mEvents))
                            }
                            else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                        }

                        //hide loader
                        main_view_load.hide()
                    }
        } ?: run {
            mEventsCol
                    .orderBy(orderString, orderDirection)
                    .limit(mEventsPerPage.toLong())
                    .get().addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                mEvents = it.result.toObjects(Event::class.java)
                                lastDocument = it.result.documents.last()
                                EventBus.getDefault().post(EventsReceivedEvent(mEvents))
                            }
                            else -> Toast.makeText(this, mGenericErrorString, Toast.LENGTH_SHORT).show()
                        }

                        //hide loader
                        main_view_load.hide()
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
                    mUser = it.result.toObject(User::class.java)
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
