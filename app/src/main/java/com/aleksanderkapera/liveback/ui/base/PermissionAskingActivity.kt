package com.aleksanderkapera.liveback.ui.base

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.aleksanderkapera.liveback.util.PermissionKind
import com.aleksanderkapera.liveback.util.isApiBelow

abstract class PermissionsAskingActivity: AppCompatActivity(){

    companion object {
        private const val SETTINGS_REQUEST_CODE = 0
    }

    open fun onPermissionResult(granted: Boolean, permissionKind: PermissionKind){
        //child class should override and implement function body
    }

    fun hasPermissions(permissionKind: PermissionKind): Boolean {
        if (isApiBelow(23)) return true

        var hasPermission = true

        for (permission: String in permissionKind.permissions) {
            hasPermission = hasPermission && ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        return hasPermission
    }

    fun requestPermissions(permissionKind: PermissionKind){
        ActivityCompat.requestPermissions(
                this,
                permissionKind.permissions,
                permissionKind.ordinal
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

        onPermissionResult(granted, PermissionKind.values()[requestCode])
    }

    fun openSettings(){
        val settingsIntent = Intent(Settings.ACTION_SETTINGS)

        if(settingsIntent.resolveActivity(packageManager) != null)
            startActivityForResult(settingsIntent, SETTINGS_REQUEST_CODE)
        else Toast.makeText(this,"Can not open Settings.",Toast.LENGTH_SHORT).show()
    }

}
