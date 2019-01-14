package com.aleksanderkapera.liveback.bus

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aleksanderkapera.liveback.util.NOTIFICATION_EVENT_UID
import com.aleksanderkapera.liveback.util.NOTIFICATION_RECEIVER_ID
import com.aleksanderkapera.liveback.util.NOTIFICATION_RECEIVER_TEXT

/**
 * Created by kapera on 09-Dec-18.
 */
class EventNotificationsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        val eventUid = intent?.getStringExtra(NOTIFICATION_EVENT_UID)
        val notification = intent?.getParcelableExtra(NOTIFICATION_RECEIVER_TEXT) as Notification
        val notificationId = intent.getIntExtra(NOTIFICATION_RECEIVER_ID, 0)
        notificationManager.notify(notificationId, notification)
    }
}