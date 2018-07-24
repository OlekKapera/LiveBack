package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.fragment.ImagePickDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.LoginFragment
import com.aleksanderkapera.liveback.util.ImageUtils
import com.aleksanderkapera.liveback.util.PermissionKind
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_register.*
import java.io.File
import java.io.IOException

/**
 * Created by kapera on 03-Jul-18.
 */
class SigningActivity : FragmentActivity() {

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, SigningActivity::class.java)
            activity.startActivity(intent)
        }
    }

    lateinit var mAuth: FirebaseAuth
    lateinit var mStorageRef: StorageReference

    lateinit var mUploadBytes: ByteArray
    var mProgress = 0

    private lateinit var mPermissionResolvedListener: PermissionResolvedListener

    override fun getLayoutRes(): Int {
        return R.layout.activity_signing
    }

    override fun getDefaultFragment(): BaseFragment {
        return LoginFragment.newInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED)
            return

        if (requestCode == ImagePickDialogFragment.REQUEST_CAPTURE_IMAGE) {
            // Handle image returned from camera app. Load it into image view.
            Glide.with(this).load(ImageUtils.imageFilePath).into(register_image_profile)
//            uploadImage(Uri.parse(ImageUtils.imageFilePath))

        } else if (requestCode == ImagePickDialogFragment.REQUEST_CHOOSE_IMAGE && data != null) {
            // Handle image which was picked by user. Load it into image view.
            val uri = data.data

            try {
                Glide.with(this).load(uri).into(register_image_profile)
//                uploadImage(uri)
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
     * Function for registering the activity listener in a fragment
     */
    fun setPermissionResolvedListener(listener: PermissionResolvedListener) {
        mPermissionResolvedListener = listener
    }

    private fun uploadImage(imagePath: Uri) {
        val resize = ImageResize()
        resize.execute(imagePath)
    }

    private fun executeUploadImage() {
        val childRef = mStorageRef.child("users/${FirebaseAuth.getInstance().currentUser?.uid}")

        val uploadTask = childRef.putBytes(mUploadBytes)
        uploadTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Success!!!.", Toast.LENGTH_SHORT).show()

                val imageUri = it.result.uploadSessionUri
                Toast.makeText(this, "$imageUri", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Could not upload profile picture.", Toast.LENGTH_SHORT).show()
            }
        }.addOnProgressListener {
            var currentProgress = (100 * it.bytesTransferred) / it.totalByteCount
            if (currentProgress > (mProgress + 15)) {
                mProgress = currentProgress.toInt()
                Log.d("TAG", "upload progress: $mProgress")
            }
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

            Log.d("TAG", "before ${mBitmap!!.byteCount / 1000000}")

            val bytes = ImageUtils.getBytesFromBitmap(mBitmap, 100)
            Log.d("TAG", "after ${bytes.size / 1000000}")


            return bytes
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)

            mUploadBytes = result!!
            executeUploadImage()
        }
    }

    interface PermissionResolvedListener {
        fun cameraResolved(allowed: Boolean)
        fun storageResolved(allowed: Boolean)
    }
}