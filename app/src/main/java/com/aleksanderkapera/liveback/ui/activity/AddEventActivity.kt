package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.chip.Chip
import android.support.design.widget.CollapsingToolbarLayout
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.ui.fragment.DatePickerDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ImagePickerDialogFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_add_event.*
import kotlinx.android.synthetic.main.app_bar_add_event.*
import kotlinx.android.synthetic.main.widget_input.view.*
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by kapera on 30-Jul-18.
 */
class AddEventActivity : BaseActivity() {

    private lateinit var mFireStore: FirebaseFirestore
    private lateinit var mStorageRef: StorageReference

    private lateinit var mUploadBytes: ByteArray

    private val mChips = R.array.categories.asStringArray()
    private var mSelectedChip: String = ""
    private var mBackgroundUri: Uri? = null
    private lateinit var mEvent: Event

    var year = 0
    var month = 0
    var day = 0
    var hour = "0"
    var minute = "0"

    private val DATE_PICKER = "DATE PICKER"
    private val DIALOG_TAG_OPEN = "ADD EVENT DIALOG OPEN"
    private lateinit var mPermissionResolvedListener: PermissionResolvedListener

    private val requiredField = R.string.required_field.asString()
    private val dateError = R.string.date_error.asString()
    private val categoryError = R.string.category_error.asString()

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, AddEventActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.activity_add_event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFireStore = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference

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
            Glide.with(this).load(imageFilePath).into(addEvent_image_background)
            mBackgroundUri = Uri.parse(imageFilePath)

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
     * Upload new event into BE with its background image
     */
    private fun addEventUpload() {
        addEvent_view_load.show()
        if (mBackgroundUri != null)
            ImageResize().execute(mBackgroundUri)
        else
            executeEventUpload()
    }

    /**
     * Upload background image and when successful then call event upload
     */
    private fun executeUpload() {
        mStorageRef.child("events/${UUID.randomUUID()}").putBytes(mUploadBytes).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    it.result.metadata?.path?.let {
                        mEvent.backgroundPicturePath = it
                    }
                    executeEventUpload()
                }
                else -> {
                    showToast(R.string.image_upload_error)
                    addEvent_view_load.hide()
                }
            }
        }
    }

    /**
     * Upload only event pojo without background photo
     */
    private fun executeEventUpload() {
        mFireStore.collection("events").add(mEvent).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    showToast(R.string.successful_add)
                    MainActivity.startActivity(this)
                }
                else -> showToast(R.string.addEvent_error)
            }
            addEvent_view_load.hide()
        }
    }

    /**
     * Validate input fields for empty state
     */
    private fun areValid(title: String, description: String, address: String, date: Long?): Boolean {
        return when {
            title.isEmpty() -> {
                addEvent_view_title.displayError(requiredField)
                false
            }

            description.isEmpty() -> {
                addEvent_view_description.displayError(requiredField)
                false
            }

            address.isEmpty() -> {
                addEvent_view_address.displayError(requiredField)
                false
            }

            date == null -> {
                showToast(dateError)
                false
            }

            mSelectedChip.isEmpty() -> {
                showToast(categoryError)
                false
            }

            else -> true
        }
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
        val title = addEvent_view_title.input_input_text.text.toString()
        val description = addEvent_view_description.input_input_text.text.toString()
        val address = addEvent_view_address.input_input_text.text.toString()
        var date: Long? = null

        if (addEvent_view_date.input_input_text.text.toString().isNotEmpty())
            date = convertStringToLongTime(addEvent_view_date.input_input_text.text.toString())

        if (areValid(title, description, address, date)) {
            val event = Event()
            event.userUid = LoggedUser.uid
            event.userName = LoggedUser.username
            event.userProfilePath = LoggedUser.profilePicPath
            event.title = title
            event.description = description
            event.address = address
            event.date = date!!
            event.category = mSelectedChip
            mEvent = event

            addEventUpload()
        }
    }

    /**
     * Open dialog where user can pick date and time of event
     */
    private val onDateClick = View.OnClickListener {
        DatePickerDialogFragment().show(supportFragmentManager, DATE_PICKER)
    }

    inner class ImageResize : AsyncTask<Uri, Int, ByteArray>() {

        private var mBitmap: Bitmap? = null

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Uri?): ByteArray {
            if (mBitmap == null) {
                try {
                    mBitmap = if (params[0].toString().startsWith("content://"))
                        MediaStore.Images.Media.getBitmap(contentResolver, params[0])
                    else
                        MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(File(params[0]?.path)))
                } catch (ex: IOException) {
                    Log.e("TAG", ex.message)
                }
            }

            return getBytesFromBitmap(mBitmap, 25)
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)

            mUploadBytes = result!!
            executeUpload()
        }
    }

    interface PermissionResolvedListener {
        fun cameraResolved(allowed: Boolean)
        fun storageResolved(allowed: Boolean)
    }
}