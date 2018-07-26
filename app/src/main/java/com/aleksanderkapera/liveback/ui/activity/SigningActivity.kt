package com.aleksanderkapera.liveback.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.model.User
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.ui.base.FragmentActivity
import com.aleksanderkapera.liveback.ui.fragment.ImagePickDialogFragment
import com.aleksanderkapera.liveback.ui.fragment.LoginFragment
import com.aleksanderkapera.liveback.ui.fragment.RegisterFragment
import com.aleksanderkapera.liveback.util.ImageUtils
import com.aleksanderkapera.liveback.util.PermissionKind
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_signing.*
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
    private lateinit var mUsersRef: CollectionReference


    private lateinit var mUploadBytes: ByteArray

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
        mUsersRef = FirebaseFirestore.getInstance().collection("users")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED)
            return

        if (requestCode == ImagePickDialogFragment.REQUEST_CAPTURE_IMAGE) {
            // Handle image returned from camera app. Load it into image view.
            Glide.with(this).load(ImageUtils.imageFilePath).into(register_image_profile)
            (getLastFragment() as RegisterFragment).mImageUri = Uri.parse(ImageUtils.imageFilePath)

        } else if (requestCode == ImagePickDialogFragment.REQUEST_CHOOSE_IMAGE && data != null) {
            // Handle image which was picked by user. Load it into image view.
            val uri = data.data

            try {
                Glide.with(this).load(uri).into(register_image_profile)
                (getLastFragment() as RegisterFragment).mImageUri = uri
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

    fun uploadUser(userPojo: User) {
        mUsersRef.document(userPojo.uid).set(userPojo).addOnSuccessListener {
            MainActivity.startActivity(this)
            Toast.makeText(this, R.string.welcome, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, R.string.signUp_error, Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            //hide loader
            signing_view_load.hide()
        }
    }

    fun uploadImage(imagePath: Uri, userPojo: User) {
        val resize = ImageResize(userPojo)
        resize.execute(imagePath)
    }

    private fun executeUploadImage(userPojo: User) {
        val childRef = mStorageRef.child("users/${mAuth.currentUser?.uid}")

        val uploadTask = childRef.putBytes(mUploadBytes)
        uploadTask.addOnSuccessListener {
            userPojo.profilePicPath = it.metadata?.path
            uploadUser(userPojo)
        }.addOnFailureListener {

            //hide loader
            signing_view_load.hide()
            Toast.makeText(this, R.string.image_upload_error, Toast.LENGTH_SHORT).show()
        }
    }

    inner class ImageResize(val userPojo: User) : AsyncTask<Uri, Int, ByteArray>() {

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

            return ImageUtils.getBytesFromBitmap(mBitmap, 100)
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)

            mUploadBytes = result!!
            executeUploadImage(userPojo)
        }
    }

    interface PermissionResolvedListener {
        fun cameraResolved(allowed: Boolean)
        fun storageResolved(allowed: Boolean)
    }
}