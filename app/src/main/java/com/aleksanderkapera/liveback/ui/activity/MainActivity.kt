package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Comment
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.model.Vote
import com.aleksanderkapera.liveback.ui.MainFragment
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private lateinit var mNavigationDrawer: NavigationViewHelper
    private lateinit var mDrawerLayout: DrawerLayout

    private lateinit var mEventsCol : CollectionReference
    private lateinit var mEvents : ArrayList<Event>

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getDefaultFragment(): BaseFragment {
        return MainFragment.newInstance(mEvents)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mEventsCol = FirebaseFirestore.getInstance().collection("events")
        mEventsCol.get().addOnCompleteListener {
            when {
                it.isSuccessful -> mEvents = ArrayList(it.result.toObjects(Event::class.java))
                else -> Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
            }

        }

        mDrawerLayout = findViewById(R.id.main_layout_drawer)
        mNavigationDrawer = NavigationViewHelper(this, mDrawerLayout)
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

    fun onButtonClick(view: View) {
        val user = User("Mari ", "marisheibley@gmail.com")
        val event = Event("asd4512f3w5", 1529964000000, "Best Event", "Very long description which I wrote all by myslef while sitting in McDonald's.", "Party", 69, 31, 132)
        val comment = Comment("Awesome Event!",1529964000000,"asd4531w23")
        val vote = Vote ("Make it louder", "I can't hear anything you dumbasses!","1d1as321d3a5w",999,12)

        mEventsCol.add(event).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this,"yeah",Toast.LENGTH_SHORT).show()
            }
            if(it.isCanceled){
                Toast.makeText(this,"booooooooo",Toast.LENGTH_SHORT).show()
            }
        }

//        mEventsCol.whereEqualTo("comments", 2).get().addOnCompleteListener {
//            if (it.isSuccessful) {
//                val myEvent = it.result.toObjects(Event::class.java)
//                Log.v("asd", myEvent[0]?.comments.toString())
//            }
//        }
    }
}
