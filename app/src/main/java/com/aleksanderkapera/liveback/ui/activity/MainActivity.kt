package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventsReceivedEvent
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.fragment.MainFragment
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
import com.aleksanderkapera.liveback.util.INTENT_MAIN_LOGGING
import com.aleksanderkapera.liveback.util.LoggedUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus

class MainActivity : FragmentActivity() {

    companion object {
        fun startActivity(activity: Activity, anonymousUser: Boolean) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(INTENT_MAIN_LOGGING, anonymousUser)
            activity.startActivity(intent)
        }
    }

    private lateinit var mNavigationDrawer: NavigationViewHelper
    private lateinit var mDrawerLayout: DrawerLayout

    lateinit var mAuth: FirebaseAuth
    private lateinit var mFireStoreRef: FirebaseFirestore
    private lateinit var mEventsCol: CollectionReference
    private lateinit var mEvents: List<Event>
    private var mUser: User? = null
    private var mStorageRef: StorageReference? = null
    private var isAnonymousUser = false

    override fun getLayoutRes(): Int = R.layout.activity_main

    override fun getDefaultFragment(): BaseFragment = MainFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //show loader
        main_view_load.show()

        mAuth = FirebaseAuth.getInstance()
        mFireStoreRef = FirebaseFirestore.getInstance()

        isAnonymousUser = intent.getBooleanExtra(INTENT_MAIN_LOGGING, false)

        //when no user is logged in open login fragment
        if (mAuth.currentUser != null || isAnonymousUser) {
            mEventsCol = mFireStoreRef.collection("events")
            getEvents()
            mAuth.currentUser?.let {
                getUser(it.uid)
            }

            mDrawerLayout = main_layout_drawer
            mNavigationDrawer = NavigationViewHelper(this, mDrawerLayout)

        } else
            SigningActivity.startActivity(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        searchBar.setMenuItem(menu?.findItem(R.id.searchButton))
        return true
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
     * Retrieve events from database
     */
    private fun getEvents() {
        mEventsCol.orderBy("date", Query.Direction.ASCENDING).get().addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    mEvents = it.result.toObjects(Event::class.java)
                    EventBus.getDefault().post(EventsReceivedEvent(mEvents))
                }
                else -> Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            //hide loader
            main_view_load.hide()
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

                        user.profilePicPath?.let {
                            mStorageRef = FirebaseStorage.getInstance().getReference(it)
                            LoggedUser.profilePicPath = it
                        }

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
