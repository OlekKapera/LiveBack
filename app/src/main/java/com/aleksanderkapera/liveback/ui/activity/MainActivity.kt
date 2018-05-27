package com.aleksanderkapera.liveback.ui.activity

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import butterknife.BindString
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.ui.widget.NavigationViewHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : BaseActivity() {

    private lateinit var mNavigationDrawer: NavigationViewHelper
    private lateinit var mDrawerLayout: DrawerLayout

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDrawerLayout = findViewById(R.id.main_layout_drawer)

        // set toolbar
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = getString(R.string.app_name)
        }

//        val toggle = ActionBarDrawerToggle(
//                this, main_layout_drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
//        main_layout_drawer.setDrawerListener(toggle)
//        toggle.syncState()

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
