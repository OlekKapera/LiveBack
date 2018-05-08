package com.aleksanderkapera.liveback.ui.activity

import android.os.Bundle
import android.view.Menu
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : BaseActivity() {

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        searchBar.setMenuItem(menu?.findItem(R.id.searchButton))
        return true
    }
}
