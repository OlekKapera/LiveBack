package com.aleksanderkapera.liveback.ui.widget

import android.app.Activity
import android.support.v4.widget.DrawerLayout
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.util.AndroidUtils
import com.aleksanderkapera.liveback.util.ImageUtils
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
        activity.navigation_row_events.setOnClickListener(onEventsClick)
        activity.navigation_row_profile.setOnClickListener(onProfileClick)
        activity.navigation_row_settings.setOnClickListener(onSettingsClick)
        activity.navigation_row_logOut.setOnClickListener(onLogOutClick)

        activity.navigation_header_image.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(AndroidUtils.getResources(),R.drawable.mari_profile,110,110))
        activity.navigation_container_background.setImageBitmap(ImageUtils.decodeSampledBitmapFromResource(AndroidUtils.getResources(),R.drawable.bg_drawer,200,600))
    }

    // region OnClickListeners
    private val onEventsClick = View.OnClickListener{ view ->

    }

    private val onProfileClick = View.OnClickListener { view ->

    }

    private val onSettingsClick = View.OnClickListener { view ->

    }

    private val onLogOutClick = View.OnClickListener { view ->

    }
    // endregion
}