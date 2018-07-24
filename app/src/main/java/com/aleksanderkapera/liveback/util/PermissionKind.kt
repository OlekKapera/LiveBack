package com.aleksanderkapera.liveback.util

import android.Manifest
import android.Manifest.permission.*

enum class PermissionKind(vararg var permissions: String) {

    CONTACTS(READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS),

    PHONE(permissions = *arrayOf(CALL_PHONE, READ_PHONE_STATE, READ_CALL_LOG, WRITE_CALL_LOG, ADD_VOICEMAIL, USE_SIP, PROCESS_OUTGOING_CALLS)),

    STORAGE(permissions = *arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)),

    LOCATION(permissions = *arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)),

    CALENDAR(permissions = *arrayOf(READ_CALENDAR, WRITE_CALENDAR)),

    CAMERA(permissions = *arrayOf(Manifest.permission.CAMERA)),

    MICROPHONE(permissions = *arrayOf(RECORD_AUDIO)),

    SENSOR(permissions = *arrayOf(BODY_SENSORS)),

    SMS(permissions = *arrayOf(SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS));
}
