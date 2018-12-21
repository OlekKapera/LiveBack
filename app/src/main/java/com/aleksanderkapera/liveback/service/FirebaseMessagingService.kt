package com.aleksanderkapera.liveback.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.activity.MainActivity
import com.aleksanderkapera.liveback.util.INTENT_NOTIFICATION_EVENTUID
import com.aleksanderkapera.liveback.util.NOTIFICATION_ID_EVENT
import com.aleksanderkapera.liveback.util.context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles messages received from Firebase server
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        message?.from?.let {
            if (it.startsWith("/topics/C") or it.startsWith("/topics/V")) {
                message?.data?.let { data ->
                    val eventName = data["eventName"]
                    val eventUid = data["eventUid"]
                    val notificationType = data["notificationType"] ?: "COMMENT"

                    val builder = NotificationCompat.Builder(context)
                            .setContentTitle(eventName)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra(INTENT_NOTIFICATION_EVENTUID, eventUid)
                    val activity = PendingIntent.getActivity(context, NOTIFICATION_ID_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    builder.setContentIntent(activity)

                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    builder.setSound(alarmSound)

                    builder.setContentText(when (NotificationType.valueOf(notificationType)) {
                        NotificationType.COMMENT -> getString(R.string.notification_comment_added, eventName)
                        else -> getString(R.string.notification_vote_added, eventName)
                    })

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(0, builder.build())
                }
            }
        }
    }
}