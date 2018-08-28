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
import com.aleksanderkapera.liveback.ui.base.DeleteDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.DatePickerDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.DeleteDialogType
import com.aleksanderkapera.liveback.ui.fragment.ImagePickerDialogFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.DocumentReference
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

    private val addEvent = R.string.add_event.asString()
    private val editEvent = R.string.edit_event.asString()

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
        fun startActivity(activity: Activity, event: Event?) {
            val intent = Intent(activity, AddEventActivity::class.java)
            intent.putExtra(INTENT_ADD_EVENT_EVENT, event)

            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.activity_add_event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFireStore = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference

        mEvent = Event()

        (intent.getSerializableExtra(INTENT_ADD_EVENT_EVENT) as? Event)?.let {
            mEvent = it
        }

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
        setupViews()

        addEvent_image_background.setOnClickListener(onAddBackgroundClick)
        addEvent_button_addBackground.setOnClickListener(onAddBackgroundClick)
        addEvent_button_accept.setOnClickListener(onAcceptClick)
        addEvent_button_delete.setOnClickListener { DeleteDialogFragment.newInstance(DeleteDialogType.EVENT, null).show(supportFragmentManager, TAG_EVENT_DELETE) }
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
            Glide.with(this)
                    .load(imageFilePath)
                    .signature(StringSignature(mEvent.backgroundPictureTime.toString()))
                    .into(addEvent_image_background)
            mBackgroundUri = Uri.parse(imageFilePath)

        } else if (requestCode == ImagePickerDialogFragment.REQUEST_CHOOSE_IMAGE && data != null) {
            // Handle image which was picked by user. Load it into image view.
            val uri = data.data

            try {
                Glide.with(this)
                        .load(uri)
                        .signature(StringSignature(mEvent.backgroundPictureTime.toString()))
                        .into(addEvent_image_background)
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
     * Fill views with data when editing event
     */
    private fun setupViews() {
        if (mEvent.eventUid.isNotEmpty()) {
            addEvent_text_title.text = editEvent
            addEvent_view_title.input_input_text.setText(mEvent.title)
            addEvent_view_description.input_input_text.setText(mEvent.description)
            addEvent_view_address.input_input_text.setText(mEvent.address)
            addEvent_view_date.input_input_text.setText(convertLongToDate(mEvent.date, "d.M.yyyy HH:mm"))

            if (mEvent.backgroundPicturePath.isNotEmpty()) {
                Glide.with(this)
                        .using(FirebaseImageLoader())
                        .load(mStorageRef.child(mEvent.backgroundPicturePath))
                        .signature(StringSignature(mEvent.backgroundPictureTime.toString()))
                        .into(addEvent_image_background)

                addEvent_button_addBackground.visibility = View.GONE
                addEvent_text_addBackground.visibility = View.GONE
                addEvent_view_imageOverlay.visibility = View.VISIBLE
            } else {
                addEvent_button_addBackground.visibility = View.VISIBLE
                addEvent_text_addBackground.visibility = View.VISIBLE
                addEvent_view_imageOverlay.visibility = View.GONE
                addEvent_image_background.setImageResource(R.drawable.bg_add_event)
            }

            addEvent_button_delete.visibility = View.VISIBLE
        } else {
            addEvent_button_delete.visibility = View.GONE
            addEvent_text_title.text = addEvent
        }
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
        mSelectedChip = mEvent.category

        mChips.forEachIndexed { index, s ->
            val chip = Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
            chip.text = s
            chip.isCheckable = true
            chip.setOnClickListener {
                mSelectedChip = (it as Chip).text.toString()
                addEvent_layout_chips.clearCheck()
                addEvent_layout_chips.check(it.id)
            }
            if (s == mSelectedChip) {
                chip.isChecked = true
                addEvent_layout_chips.check(index + 1)
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
        val path = if (mEvent.backgroundPicturePath.isNotEmpty())
            mEvent.backgroundPicturePath
        else
            "events/${UUID.randomUUID()}"

        mStorageRef.child(path).putBytes(mUploadBytes).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    it.result.metadata?.let {
                        mEvent.backgroundPicturePath = it.path
                        mEvent.backgroundPictureTime = it.updatedTimeMillis
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
        val docRef: DocumentReference?

        if (mEvent.eventUid.isEmpty()) {
            docRef = mFireStore.collection("events").document()
            mEvent.eventUid = docRef.id
        } else
            docRef = mFireStore.document("events/${mEvent.eventUid}")

        docRef.set(mEvent).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    showToast(R.string.successful_add)
                    MainActivity.startActivity(this, LoggedUser.uid.isEmpty())
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
            mEvent.userUid = LoggedUser.uid
            mEvent.title = title
            mEvent.description = description
            mEvent.address = address
            mEvent.date = date!!
            mEvent.category = mSelectedChip

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