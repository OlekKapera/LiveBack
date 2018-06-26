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
import com.aleksanderkapera.liveback.model.SimpleEvent
import com.aleksanderkapera.liveback.model.SimpleUser
import com.aleksanderkapera.liveback.ui.MainFragment
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {

    companion object {
        fun startActivity(activity: Activity){
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private lateinit var mNavigationDrawer: NavigationViewHelper
    private lateinit var mDrawerLayout: DrawerLayout

    private val mDocRef = FirebaseFirestore.getInstance().document("events/simpleEvent")

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun getDefaultFragment(): BaseFragment {
        return MainFragment.newInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        return when(item?.itemId){
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onButtonClick(view: View){
        val user = SimpleUser("Mari Sheibley", "marisheibley@gmail.com")
        val event = SimpleEvent(user,1529964000000, "Title", "DEsc", "cat", 164,2,82)

        mDocRef.set(event).addOnCompleteListener(this, OnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this,"yeah",Toast.LENGTH_SHORT).show()
            }
            if(it.isCanceled){
                Toast.makeText(this,"booooooooo",Toast.LENGTH_SHORT).show()
            }
        })
    }
}
