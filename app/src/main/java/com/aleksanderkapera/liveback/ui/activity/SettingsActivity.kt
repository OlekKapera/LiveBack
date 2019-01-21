package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.AppBarLayout
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Switch
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.bus.EventNotificationsReceiver
import com.aleksanderkapera.liveback.model.Event
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.service.NotificationType
import com.aleksanderkapera.liveback.ui.base.BaseActivity
import com.aleksanderkapera.liveback.ui.fragment.ChangePasswordDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.DeleteAccountDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ImagePickerDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.ReminderDialogFragment
import com.aleksanderkapera.liveback.util.*
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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
    private lateinit var mFireMessaging: FirebaseMessaging
    private lateinit var mFirebaseFunctions: FirebaseFunctions
    private lateinit var mEventsCollection: CollectionReference
    private lateinit var mUsersCollection: CollectionReference

    private lateinit var mUploadBytes: ByteArray
    private var mImageUri: Uri? = null

    private lateinit var mPermissionResolvedListener: PermissionResolvedListener
    private lateinit var mPreviousElement: SettingsCaller

    private var mFavCommentChanged = false
    private var mYourCommentChanged = false
    private var mFavVoteChanged = false
    private var mYourVoteChanged = false
    private var mOldReminder = LoggedUser.reminder
    private lateinit var mLoggedUser: User

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
        mStorageRef = FirebaseStorage.getInstance().reference
        mFireMessaging = FirebaseMessaging.getInstance()
        mFirebaseFunctions = FirebaseFunctions.getInstance()
        mEventsCollection = mFireStore.collection("events")
        mUsersCollection = mFireStore.collection("users")

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
            GlideApp.with(this)
                    .load(imageFilePath)
                    .signature(ObjectKey(LoggedUser.profilePicTime.toString()))
                    .displayRoundPlaceholder()
                    .into(settings_image_profile)
            mImageUri = Uri.parse(imageFilePath)

        } else if (requestCode == ImagePickerDialogFragment.REQUEST_CHOOSE_IMAGE && data != null) {
            // Handle image which was picked by user. Load it into image view.
            val uri = data.data

            try {
                GlideApp.with(this)
                        .load(uri)
                        .signature(ObjectKey(LoggedUser.profilePicTime.toString()))
                        .displayRoundPlaceholder()
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

        settings_switch_yourComment.isChecked = LoggedUser.commentAddedOnYour
        settings_switch_favComment.isChecked = LoggedUser.commentAddedOnFav
        settings_switch_yourVote.isChecked = LoggedUser.voteAddedOnYour
        settings_switch_favVote.isChecked = LoggedUser.voteAddedOnFav

        settings_switch_yourComment.setOnClickListener {
            LoggedUser.commentAddedOnYour = (it as Switch).isChecked
            mYourCommentChanged = true
        }
        settings_switch_favComment.setOnClickListener {
            LoggedUser.commentAddedOnFav = (it as Switch).isChecked
            mFavCommentChanged = true
        }
        settings_switch_yourVote.setOnClickListener {
            LoggedUser.voteAddedOnYour = (it as Switch).isChecked
            mYourVoteChanged = true
        }
        settings_switch_favVote.setOnClickListener {
            LoggedUser.voteAddedOnFav = (it as Switch).isChecked
            mFavVoteChanged = true
        }

        updateReminderText()

        if (LoggedUser.profilePicPath.isNotEmpty())
            GlideApp.with(this)
                    .load(mStorageRef.child(LoggedUser.profilePicPath))
                    .signature(ObjectKey(LoggedUser.profilePicTime.toString()))
                    .displayRoundPlaceholder()
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
        mStorageRef.child("users/${LoggedUser.uid}").putBytes(mUploadBytes).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    it.result?.metadata?.let {
                        it.path.let {
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
        val user = User(LoggedUser.uid, LoggedUser.username, LoggedUser.email, LoggedUser.profilePicPath,
                LoggedUser.commentAddedOnYour, LoggedUser.commentAddedOnFav, LoggedUser.voteAddedOnYour,
                LoggedUser.voteAddedOnFav, LoggedUser.reminder, LoggedUser.profilePicTime, LoggedUser.likedEvents)

        docRef.set(user).addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    showToast(successful)

                    resubscribeNotifications()
                    rescheduleReminders()

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

    /**
     * Subscribe or unsubscribe from notifications when user changes notification settings
     */
    private fun resubscribeNotifications() {
        LoggedUser.likedEvents.forEach { eventUid ->
            if (mFavCommentChanged and !LoggedUser.yourEvents.contains(eventUid)) {
                resubscribeEachEvent(settings_switch_favComment.isChecked, NotificationType.COMMENT, eventUid)
            }
            if (mFavVoteChanged and !LoggedUser.yourEvents.contains(eventUid)) {
                resubscribeEachEvent(settings_switch_favVote.isChecked, NotificationType.VOTE, eventUid)
            }
        }

        LoggedUser.yourEvents.forEach { eventUid ->
            if (mYourCommentChanged) {
                resubscribeEachEvent(settings_switch_yourComment.isChecked, NotificationType.COMMENT, eventUid)
            }
            if (mYourVoteChanged) {
                resubscribeEachEvent(settings_switch_yourVote.isChecked, NotificationType.VOTE, eventUid)
            }
        }
    }

    /**
     * Execute un/subscribing for each event regarding which switch has been toggled
     */
    private fun resubscribeEachEvent(subscribe: Boolean, notificationType: NotificationType, eventUid: String) {
        if (subscribe) {
            mFireMessaging.subscribeToTopic(when (notificationType) {
                NotificationType.COMMENT -> "C$eventUid"
                else -> "V$eventUid"
            })
        } else {
            mFireMessaging.unsubscribeFromTopic(when (notificationType) {
                NotificationType.COMMENT -> "C$eventUid"
                else -> "V$eventUid"
            })
        }
    }

    /**
     * If user changed the value of reminder reschedule alarms for each liked or user's event. Retrieve
     * event and it's user. If the event belongs to logged user, there is no need for fetching him
     * so don't that.
     */
    private fun rescheduleReminders() {
        if (LoggedUser.reminder != mOldReminder) {
            val likedEventsUid = LoggedUser.likedEvents
            likedEventsUid.removeAll(LoggedUser.yourEvents)
            val likedEvents = mutableListOf<Event>()
            val users = mutableListOf<User>()
            var usersEvents: List<Event>

            mLoggedUser = User()
            LoggedUser.apply {
                mLoggedUser = User(uid, username, email, profilePicPath, commentAddedOnYour, commentAddedOnFav, voteAddedOnYour, voteAddedOnFav, reminder, profilePicTime, LoggedUser.likedEvents)
            }

            getEventDocuments(likedEventsUid).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result?.let {
                            likedEvents.addAll(it)

                            val userUids = likedEvents.map { it.userUid }.distinct()

                            getUserDocuments(userUids).addOnCompleteListener {
                                when {
                                    it.isSuccessful -> {
                                        it.result?.let {
                                            users.addAll(it)

                                            likedEvents.forEach { event ->
                                                scheduleNotification(event, users.single { user -> user.uid == event.userUid })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            getEventDocuments(LoggedUser.yourEvents).addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        it.result?.let {
                            usersEvents = it.toList()

                            usersEvents.forEach { event ->
                                scheduleNotification(event, mLoggedUser)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get one or more events from firebase and convert returned Json to list of events
     */
    private fun getEventDocuments(eventUids: List<String>): Task<List<Event>> {
        return mFirebaseFunctions.getHttpsCallable("getEventDocuments")
                .call(eventUids)
                .continueWith { task ->
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val events: List<Event> = gson.fromJson(task.result?.data.toString(), object : TypeToken<List<Event>>() {}.type)
                    events
                }
    }

    /**
     * Get one or more users from firebase and convert returned Json to list of users
     */
    private fun getUserDocuments(userUids: List<String>): Task<List<User>> {
        return mFirebaseFunctions.getHttpsCallable("getUserDocuments")
                .call(userUids)
                .continueWith { task ->
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val users: List<User> = gson.fromJson(task.result?.data.toString(), object : TypeToken<List<User>>() {}.type)
                    users
                }
    }

    /**
     * Create notification pending intent. Don't create alarm if the event already happened
     */
    private fun scheduleNotification(event: Event, user: User) {
        val builder = NotificationCompat.Builder(context)
                .setContentTitle(event.title)
                .setContentText(when {
                    LoggedUser.reminder < 60 && mLoggedUser == user -> "${R.string.your_event_starts.asString()} ${R.plurals.starts_in_minutes.asPluralsString(LoggedUser.reminder)}"
                    LoggedUser.reminder >= 60 && mLoggedUser == user -> "${R.string.your_event_starts.asString()} ${R.plurals.starts_in_hours.asPluralsString(LoggedUser.reminder)}"
                    LoggedUser.reminder < 60 && mLoggedUser != user -> "${R.string.event_you_liked.asString()} ${R.plurals.starts_in_minutes.asPluralsString(LoggedUser.reminder)}"
                    else -> "${R.string.event_you_liked.asString()} ${R.plurals.starts_in_hours.asPluralsString(LoggedUser.reminder)}"
                })
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setChannelId(NOTIFICATION_REMINDER_CHANNEL)

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(INTENT_NOTIFICATION_EVENT, event)
        intent.putExtra(INTENT_NOTIFICATION_USER, user)
        val activity = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(activity)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(alarmSound)

        val notification = builder.build()

        val notificationIntent = Intent(applicationContext, EventNotificationsReceiver::class.java)
        notificationIntent.putExtra(NOTIFICATION_RECEIVER_ID, NOTIFICATION_ID_EVENT)
        notificationIntent.putExtra(event.eventUid, notification)
        notificationIntent.putExtra(NOTIFICATION_EVENT_UID, event.eventUid)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, NOTIFICATION_ID_EVENT, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //cancel previous alarm
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.cancel(pendingIntent)

        //set new one if it's in the future
        if (event.date > (System.currentTimeMillis() + (LoggedUser.reminder * 60000)) && LoggedUser.reminder != 0)
            alarmManager.set(AlarmManager.RTC_WAKEUP, event.date - (LoggedUser.reminder * 60000), pendingIntent)
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