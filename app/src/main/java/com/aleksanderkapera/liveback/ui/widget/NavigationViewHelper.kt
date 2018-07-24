package com.aleksanderkapera.liveback.ui.widget

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.DrawerLayout
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.util.ImageUtils
import com.aleksanderkapera.liveback.util.getStatusBarHeight
import com.aleksanderkapera.liveback.util.resources
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_header.*
import kotlinx.android.synthetic.main.navigation_menu.*

/**
 * Created by kapera on 26-May-18.
 */
class NavigationViewHelper (private val activity: Activity, val drawer: DrawerLayout) {

    init {
        setupViews()
    }

    private fun setupViews(){
        // Move profile image below status bar
        val params = activity.navigation_header_image.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(0, getStatusBarHeight() + 16, 0, 0)
        activity.navigation_header_image.layoutParams = params

        activity.navigation_header_image.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(resources,R.drawable.mari_profile,110,110))
        activity.navigation_container_background.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(resources,R.drawable.bg_drawer,200,600))

        activity.navigation_row_events.setOnClickListener(onEventsClick)
        activity.navigation_row_profile.setOnClickListener(onProfileClick)
        activity.navigation_row_settings.setOnClickListener(onSettingsClick)
        activity.navigation_row_logOut.setOnClickListener(onLogOutClick)
    }

    // region OnClickListeners
    private val onEventsClick = View.OnClickListener{

    }

    private val onProfileClick = View.OnClickListener {

    }

    private val onSettingsClick = View.OnClickListener {

    }

    private val onLogOutClick = View.OnClickListener {

    }
    // endregion
}