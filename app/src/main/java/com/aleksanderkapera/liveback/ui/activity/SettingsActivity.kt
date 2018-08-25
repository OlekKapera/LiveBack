package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.AppBarLayout
import android.util.Log
import android.widget.Switch
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.ui.fragment.ChangePasswordDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.DeleteAccountDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ImagePickerDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ReminderDialogFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.StringSignature
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.app_bar_settings.*
import kotlinx.android.synthetic.main.container_settings_credentials.*
import kotlinx.android.synthetic.main.container_settings_notifications.*
import kotlinx.android.synthetic.main.container_settings_reminder.*
import java.io.File
import java.io.IOException

/**
 * Created by kapera on 21-Aug-18.
 */
class SettingsActivity : BaseActivity() {

    private val none = R.string.none.asString()
    private val tenMin = R.string.ten_before.asString()
    private val thirtyMin = R.string.thirty_before.asString()
    private val hour = R.string.hour_before.asString()
    private val twoHours = R.string.two_hours_before.asString()
    private val threeHours = R.string.three_hours_before.asString()
    private val sixHours = R.string.six_hours_before.asString()
    private val twelveHours = R.string.twelve_hours_before.asString()
    private val twentyFourHours = R.string.twenty_four_hours_before.asString()
    private val successful = R.string.profile_successful.asString()
    private val updateError = R.string.profile_update_error.asString()

    private val mUserImage = R.drawable.ic_user_round_solid.asDrawable()

    private lateinit var mFireStore: FirebaseFirestore
    private lateinit var mStorageRef: StorageReference

    private lateinit var mUploadBytes: ByteArray
    private var mImageUri: Uri? = null

    private lateinit var mPermissionResolvedListener: PermissionResolvedListener
    private lateinit var mPreviousElement: SettingsCaller

    companion object {
        fun startActivity(activity: Activity, elementType: SettingsCaller) {
            val intent = Intent(activity, SettingsActivity::class.java)

            intent.putExtra(INTENT_SETTINGS_ELEMENTS, elementType)

            activity.startActivity(intent)
        }
    }

    override fun getLayoutRes(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFireStore = FirebaseFirestore.getInstance()

        if (LoggedUser.profilePicPath.isNotEmpty())
            mStorageRef = FirebaseStorage.getInstance().reference.child(LoggedUser.profilePicPath)

        mPreviousElement = intent.extras?.get(INTENT_SETTINGS_ELEMENTS) as SettingsCaller

        // set toolbar
        setSupportActionBar(settings_layout_toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowHomeEnabled(true)
        }

        // move only toolbar below status bar
        val toolbarParams = settings_layout_toolbar.layoutParams as AppBarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        settings_layout_toolbar.layoutParams = toolbarParams

        setupViews()

        settings_button_accept.setOnClickListener { updateProfileUpload() }
        settings_image_profile.setOnClickListener { ImagePickerDialogFragment.newInstance().show(supportFragmentManager, TAG_SETTINGS_IMAGE) }
        settings_container_password.setOnClickListener { ChangePasswordDialogFragment.newInstance().show(supportFragmentManager, TAG_SETTINGS_CHANGE_PASSWORD) }
        settings_container_reminder.setOnClickListener { ReminderDialogFragment.newInstance().show(supportFragmentManager, TAG_SETTINGS_REMINDER) }
        settings_container_delete.setOnClickListener { DeleteAccountDialogFragment.newInstance().show(supportFragmentManager, TAG_SETTINGS_DELETE) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            if (deletePhotoContent(mImageUri)) mImageUri = null
            return
        }

        if (requestCode == ImagePickerDialogFragment.REQUEST_CAPTURE_IMAGE) {
            // Handle image returned from camera app. Load it into image view.
            Glide.with(this)
                    .load(imageFilePath)
                    .signature(StringSignature(LoggedUser.profilePicTime.toString()))
                    .into(settings_image_profile)
            mImageUri = Uri.parse(imageFilePath)

        } else if (requestCode == ImagePickerDialogFragment.REQUEST_CHOOSE_IMAGE && data != null) {
            // Handle image which was picked by user. Load it into image view.
            val uri = data.data

            try {
                Glide.with(this)
                        .load(uri)
                        .signature(StringSignature(LoggedUser.profilePicTime.toString()))
                        .into(settings_image_profile)
                mImageUri = uri
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
     * Fill views with user settings
     */
    private fun setupViews() {
        settings_input_name.setText(LoggedUser.username)
        settings_input_email.setText(LoggedUser.email)

        settings_switch_yourEvent.isChecked = LoggedUser.commentAddedOnYour
        settings_switch_favEvent.isChecked = LoggedUser.commentAddedOnFav
        settings_switch_yourVote.isChecked = LoggedUser.voteAddedOnYour
        settings_switch_favVote.isChecked = LoggedUser.voteAddedOnFav

        settings_switch_yourEvent.setOnClickListener { LoggedUser.commentAddedOnYour = (it as Switch).isChecked }
        settings_switch_favEvent.setOnClickListener { LoggedUser.commentAddedOnFav = (it as Switch).isChecked }
        settings_switch_yourVote.setOnClickListener { LoggedUser.voteAddedOnYour = (it as Switch).isChecked }
        settings_switch_favVote.setOnClickListener { LoggedUser.voteAddedOnFav = (it as Switch).isChecked }

        updateReminderText()

        if (LoggedUser.profilePicPath.isNotEmpty())
            Glide.with(this)
                    .using(FirebaseImageLoader())
                    .load(mStorageRef)
                    .signature(StringSignature(LoggedUser.profilePicTime.toString()))
                    .into(settings_image_profile)
        else
            settings_image_profile.setImageDrawable(mUserImage)
    }

    /**
     * Function for registering the activity listener in a fragment
     */
    fun setPermissionResolvedListener(listener: PermissionResolvedListener) {
        mPermissionResolvedListener = listener
    }

    /**
     * Updates reminder text according to user's input
     */
    fun updateReminderText() {
        when (LoggedUser.reminder) {
            0 -> settings_text_reminder.text = none
            10 -> settings_text_reminder.text = tenMin
            30 -> settings_text_reminder.text = thirtyMin
            60 -> settings_text_reminder.text = hour
            120 -> settings_text_reminder.text = twoHours
            180 -> settings_text_reminder.text = threeHours
            360 -> settings_text_reminder.text = sixHours
            720 -> settings_text_reminder.text = twelveHours
            1440 -> settings_text_reminder.text = twentyFourHours
        }
    }

    /**
     * Upload user's profile with updated image if has one
     */
    private fun updateProfileUpload() {
        settings_view_load.show()
        LoggedUser.username = settings_input_name.text.toString()
        LoggedUser.email = settings_input_email.text.toString()

        if (mImageUri != null)
            ImageResize().execute(mImageUri)
        else
            executeProfileUpload()
    }

    /**
     * Upload profile image and when successful then call user profile upload
     */
    private fun executeUpload() {
        mStorageRef.putBytes(mUploadBytes).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    it.result.metadata?.let {
                        it.path?.let {
                            LoggedUser.profilePicPath = it
                        }
                        LoggedUser.profilePicTime = it.updatedTimeMillis
                    }
                    executeProfileUpload()
                }
                else -> {
                    showToast(R.string.image_upload_error)
                    settings_view_load.hide()
                }
            }
        }
    }

    /**
     * Upload only user pojo without background photo
     */
    private fun executeProfileUpload() {
        val docRef = mFireStore.document("users/${LoggedUser.uid}")

        docRef.set(LoggedUser).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    showToast(successful)
                    when (mPreviousElement) {
                        SettingsCaller.MAIN_ACTIVITY -> MainActivity.startActivity(this, false)
                        SettingsCaller.PROFILE_FRAGMENT -> finish()
                    }
                }
                else -> showToast(updateError)
            }
            settings_view_load.hide()
        }
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