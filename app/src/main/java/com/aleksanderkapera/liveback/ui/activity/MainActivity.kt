package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.MainFragment
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
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
}
