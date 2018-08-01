package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.design.widget.CollapsingToolbarLayout
import android.view.View
import android.widget.LinearLayout
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.ui.fragment.DatePickerDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ImagePickerDialogFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.app_bar_add_event.*
import kotlinx.android.synthetic.main.fragment_add_event.*
import kotlinx.android.synthetic.main.widget_input.view.*
import java.io.IOException

/**
 * Created by kapera on 30-Jul-18.
 */
class AddEventActivity : BaseActivity() {

    private val mChips = R.array.categories.asStringArray()
    private var mSelectedChip: String = ""
    private lateinit var mBackgroundUri: Uri

    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0

    private val DATE_PICKER = "DATE PICKER"
    private val DIALOG_TAG_OPEN = "ADD EVENT DIALOG OPEN"
    private lateinit var mPermissionResolvedListener: PermissionResolvedListener

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, AddEventActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_add_event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set toolbar
        setSupportActionBar(addEvent_layout_toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowHomeEnabled(true)
        }

        // move only toolbar below status bar
        val toolbarParams = addEvent_layout_toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        addEvent_layout_toolbar.layoutParams = toolbarParams

        setupChips()

        addEvent_image_background.setOnClickListener(onAddBackgroundClick)
        addEvent_button_addBackground.setOnClickListener(onAddBackgroundClick)
        addEvent_button_accept.setOnClickListener(onAcceptClick)
        addEvent_view_date.setOnClickListener(onDateClick)
        addEvent_view_date.input_input_text.setOnClickListener(onDateClick)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED)
            return

        // hide add button and text
        addEvent_button_addBackground.visibility = View.INVISIBLE
        addEvent_text_addBackground.visibility = View.INVISIBLE

        if (requestCode == ImagePickerDialogFragment.REQUEST_CAPTURE_IMAGE) {
            // Handle image returned from camera app. Load it into image view.
            Glide.with(this).load(ImageUtils.imageFilePath).into(addEvent_image_background)
            mBackgroundUri = Uri.parse(ImageUtils.imageFilePath)

        } else if (requestCode == ImagePickerDialogFragment.REQUEST_CHOOSE_IMAGE && data != null) {
            // Handle image which was picked by user. Load it into image view.
            val uri = data.data

            try {
                Glide.with(this).load(uri).into(addEvent_image_background)
                mBackgroundUri = uri
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPermissionResult(granted: Boolean, permissionKind: PermissionKind) {
        super.onPermissionResult(granted, permissionKind)

        if (permissionKind == PermissionKind.STORAGE)
            mPermissionResolvedListener.storageResolved(granted)
        else if (permissionKind == PermissionKind.CAMERA)
            mPermissionResolvedListener.cameraResolved(granted)
    }

    /**
     * Set date text in view when dialog was closed
     */
    fun updateDate() {
        addEvent_view_date.input_input_text.setText("$day.$month.$year $hour:$minute")
    }

    /**
     * Setup mChips with categories
     */
    private fun setupChips() {
        mChips?.forEach {
            val chip = Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
            chip.text = it
            chip.isCheckable = true
            chip.setOnClickListener {
                mSelectedChip = (it as Chip).text.toString()
            }

            addEvent_layout_chips.addView(chip)
        }

        // move chip group above bottom navigation
        val chipGroupParams = addEvent_layout_chipCard.layoutParams as LinearLayout.LayoutParams
        chipGroupParams.setMargins(0, R.dimen.spacing8.asDimen().toInt(), 0, getNavigationBarHeight())
        addEvent_layout_chipCard.layoutParams = chipGroupParams
    }

    /**
     * Function for registering the activity listener in a fragment
     */
    fun setPermissionResolvedListener(listener: PermissionResolvedListener) {
        mPermissionResolvedListener = listener
    }

    /**
     * Open dialog where user can choose a source of event's background image
     */
    private val onAddBackgroundClick = View.OnClickListener {
        ImagePickerDialogFragment.newInstance().show(supportFragmentManager, DIALOG_TAG_OPEN)
    }

    /**
     * Execute upload with user entered data
     */
    private val onAcceptClick = View.OnClickListener {
        val title = addEvent_view_title.input_input_text.text
        val description = addEvent_view_description.input_input_text.text
        val address = addEvent_view_address.input_input_text.text
        val event = Event()
    }

    /**
     * Open dialog where user can pick date and time of event
     */
    private val onDateClick = View.OnClickListener {
        DatePickerDialogFragment().show(supportFragmentManager, DATE_PICKER)
    }

    interface PermissionResolvedListener {
        fun cameraResolved(allowed: Boolean)
        fun storageResolved(allowed: Boolean)
    }
}