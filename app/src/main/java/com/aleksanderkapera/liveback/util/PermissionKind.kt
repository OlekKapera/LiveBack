package com.aleksanderkapera.liveback.util

import android.Manifest
import android.Manifest.permission.*

enum class PermissionKind constructor(vararg permissions: String) {

    CONTACTS(READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS),

    PHONE(CALL_PHONE, READ_PHONE_STATE, READ_CALL_LOG, WRITE_CALL_LOG, ADD_VOICEMAIL, USE_SIP, PROCESS_OUTGOING_CALLS),

    STORAGE(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),

    LOCATION(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),

    CALENDAR(READ_CALENDAR, WRITE_CALENDAR),

    CAMERA(Manifest.permission.CAMERA),

    MICROPHONE(RECORD_AUDIO),

    SENSOR(BODY_SENSORS),

    SMS(SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS);

    private lateinit var permissions:  Array<String>

    fun PermissionKind(permissions: Array<String>) {
        this.permissions = permissions
    }

    fun getPermissions(): Array<String> {
        return this.permissions
    }

}
