package com.aleksanderkapera.liveback.ui.widget

import android.app.Activity
import android.support.v4.widget.DrawerLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.aleksanderkapera.liveback.R

/**
 * Created by kapera on 26-May-18.
 */
class NavigationViewHelper (activity: Activity, val drawer: DrawerLayout) {

    // @format:off
    @BindView(R.id.navigation_row_events) lateinit var eventRow:NavigationItem
    @BindView(R.id.navigation_row_profile) lateinit var profileRow:NavigationItem
    @BindView(R.id.navigation_row_settings) lateinit var settingsRow:NavigationItem
    @BindView(R.id.navigation_row_logOut) lateinit var logOutRow:NavigationItem
    // @format:on

    init {
        ButterKnife.bind(this, activity)
    }

    // region OnClickListeners
    @OnClick(R.id.navigation_row_events)
    fun onEventsClick(){

    }

    @OnClick(R.id.navigation_row_profile)
    fun onProfileClick(){

    }

    @OnClick(R.id.navigation_row_settings)
    fun onSettingsClick(){

    }

    @OnClick(R.id.navigation_row_logOut)
    fun onLogOutClick(){

    }
    // endregion
}