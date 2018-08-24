package com.aleksanderkapera.liveback.ui.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.activity.AddEventActivity
import com.aleksanderkapera.liveback.ui.activity.SettingsActivity
import com.aleksanderkapera.liveback.ui.activity.SigningActivity
import com.aleksanderkapera.liveback.ui.base.PermissionsAskingActivity
import com.aleksanderkapera.liveback.util.PermissionKind
import com.aleksanderkapera.liveback.util.createImageFile
import kotlinx.android.synthetic.main.dialog_fragment_photo.*
import kotlinx.android.synthetic.main.dialog_fragment_photo.view.*
import java.io.File
import java.io.IOException

/**
 * Created by kapera on 06-Jul-18.
 */
class ImagePickerDialogFragment : DialogFragment(), SigningActivity.PermissionResolvedListener,
        AddEventActivity.PermissionResolvedListener, SettingsActivity.PermissionResolvedListener {

    companion object {
        fun newInstance(): DialogFragment = ImagePickerDialogFragment()

        const val REQUEST_CAPTURE_IMAGE = 100
        const val REQUEST_CHOOSE_IMAGE = 200
    }

    override fun onResume() {
        val params = dialog.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_photo, container, false)

        //register the listener
        when (activity) {
            is SigningActivity -> (activity as SigningActivity).setPermissionResolvedListener(this)
            is AddEventActivity -> (activity as AddEventActivity).setPermissionResolvedListener(this)
            is SettingsActivity -> (activity as SettingsActivity).setPermissionResolvedListener(this)
        }

        rootView.imageDialog_container_takePhoto.setOnClickListener(onTakePhotoClick)
        rootView.imageDialog_container_choosePhoto.setOnClickListener(onChoosePhotoClick)

        return rootView
    }

    override fun cameraResolved(allowed: Boolean) {
        if (allowed) {
            imageDialog_container_takePhoto.setOnClickListener(onTakePhotoClick)
            imageDialog_container_takePhoto.callOnClick()
        }
    }

    override fun storageResolved(allowed: Boolean) {
        if (allowed) {
            imageDialog_container_choosePhoto.setOnClickListener(onChoosePhotoClick)
            imageDialog_container_choosePhoto.callOnClick()
        }
    }

    /**
     * If phone has camera and camera permission, open camera app. Otherwise display error message or ask for permission
     */
    private val onTakePhotoClick = View.OnClickListener {
        val packageManager = activity?.packageManager

        if (packageManager!!.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if ((activity as PermissionsAskingActivity).hasPermissions(PermissionKind.CAMERA)) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    // create file to store image
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                    }

                    if (photoFile != null) {
                        val photoUri = FileProvider.getUriForFile(context!!, "com.aleksanderkapera.liveback.provider", photoFile)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        activity?.startActivityForResult(takePictureIntent, REQUEST_CAPTURE_IMAGE)
                        dismiss()
                    }
                }
            } else {
                (activity as PermissionsAskingActivity).requestPermissions(PermissionKind.CAMERA)
            }
        } else {
            Toast.makeText(context, R.string.no_camera, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open gallery where user can pick their profile photo if has permissions to read/write storage.
     * Otherwise ask for one
     */
    private val onChoosePhotoClick = View.OnClickListener {
        if ((activity as PermissionsAskingActivity).hasPermissions(PermissionKind.STORAGE)) {
            val choosePictureIntent = Intent(Intent.ACTION_PICK)
            choosePictureIntent.type = "image/*"
            activity?.startActivityForResult(choosePictureIntent, REQUEST_CHOOSE_IMAGE)
            dismiss()
        } else {
            (activity as PermissionsAskingActivity).requestPermissions(PermissionKind.STORAGE)
        }
    }
}