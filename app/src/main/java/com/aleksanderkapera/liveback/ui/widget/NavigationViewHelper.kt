package com.aleksanderkapera.liveback.ui.widget

import android.support.constraint.ConstraintLayout
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.activity.*
import com.aleksanderkapera.liveback.ui.fragment.ProfileFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_header.*
import kotlinx.android.synthetic.main.navigation_item.view.*
import kotlinx.android.synthetic.main.navigation_menu.*

/**
 * Created by kapera on 26-May-18.
 */
class NavigationViewHelper(private val activity: MainActivity, private val drawer: DrawerLayout) {

    private val needToLogin = R.string.need_to_login.asString()

    // region OnClickListeners
    private val onEventsClick = View.OnClickListener {
        MainActivity.startActivity(activity, LoggedUser.uid.isEmpty())
        drawer.closeDrawer(Gravity.START)
    }

    private val onProfileClick = View.OnClickListener {
        drawer.closeDrawer(Gravity.START)
        when {
            LoggedUser.uid.isEmpty() -> Toast.makeText(activity, needToLogin, Toast.LENGTH_SHORT).show()
            (activity.getLastFragment() as? ProfileFragment)?.mUserid == LoggedUser.uid -> activity.showFragment(ProfileFragment.newInstance(LoggedUser.uid))
            else -> activity.putFragment(ProfileFragment.newInstance(LoggedUser.uid), false)
        }
    }

    private val onAddEventClick = View.OnClickListener {
        drawer.closeDrawer(Gravity.START)
        if (LoggedUser.uid.isEmpty())
            Toast.makeText(activity, needToLogin, Toast.LENGTH_SHORT).show()
        else
            AddEventActivity.startActivity(activity, null)
    }

    private val onSettingsClick = View.OnClickListener {
        drawer.closeDrawer(Gravity.START)
        if (LoggedUser.uid.isEmpty())
            Toast.makeText(activity, needToLogin, Toast.LENGTH_SHORT).show()
        else
            SettingsActivity.startActivity(activity, SettingsCaller.MAIN_ACTIVITY)
    }

    /**
     * Handle log in and log out input
     */
    private val onLogOutClick = View.OnClickListener {
        drawer.closeDrawer(Gravity.START)
        activity.mAuth.currentUser?.let {
            activity.logOut()
            LoggedUser.clear()
            activity.navigation_row_signing.navigation_item_image.setImageDrawable(R.drawable.ic_login.asDrawable())
            activity.navigation_row_signing.navigation_item_text.text = R.string.log_in.asString()
        } ?: run {
            SigningActivity.startActivity(activity)
        }
    }
    // endregion

    init {
        // Move profile image below status bar
        val statusParams = activity.navigation_header_image.layoutParams as ConstraintLayout.LayoutParams
        statusParams.setMargins(0, getStatusBarHeight() + 16, 0, 0)
        activity.navigation_header_image.layoutParams = statusParams

        // Move log out button above navigation bar
        val navParams = activity.navigation_row_signing.layoutParams as RelativeLayout.LayoutParams
        navParams.setMargins(0, 0, 0, getNavigationBarHeight() + 16)
        activity.navigation_row_signing.layoutParams

        updateViews(null, null)

        activity.navigation_header_image.setOnClickListener(onProfileClick)
        activity.navigation_row_events.setOnClickListener(onEventsClick)
        activity.navigation_row_profile.setOnClickListener(onProfileClick)
        activity.navigation_row_addEvent.setOnClickListener(onAddEventClick)
        activity.navigation_row_settings.setOnClickListener(onSettingsClick)
        activity.navigation_row_signing.setOnClickListener(onLogOutClick)
    }

    /**
     * Fill navigation drawer with user data if accessible
     */
    fun updateViews(user: User?, profilePic: StorageReference?) {
        activity.navigation_container_background.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_drawer, 200, 600))

        user?.let {
            activity.navigation_header_userName.text = it.username

            activity.navigation_row_signing.navigation_item_text.text = R.string.log_out.asString()
            activity.navigation_row_signing.navigation_item_image.setImageResource(R.drawable.ic_logout)
        } ?: run {
            activity.navigation_header_userName.text = R.string.app_name.asString()

            activity.navigation_row_signing.navigation_item_text.text = R.string.log_in.asString()
            activity.navigation_row_signing.navigation_item_image.setImageResource(R.drawable.ic_login)
        }

        profilePic?.let {
            Glide.with(activity)
                    .using(FirebaseImageLoader())
                    .load(profilePic)
                    .signature(StringSignature(user?.profilePicTime.toString()))
                    .into(activity.navigation_header_image)
        } ?: run {
            activity.navigation_header_image.setImageDrawable(R.drawable.ic_user_round.asDrawable())
        }
    }

}