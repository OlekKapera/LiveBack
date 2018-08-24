package com.aleksanderkapera.liveback.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.widget.ImageView
import com.aleksanderkapera.liveback.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by kapera on 24-Jun-18.
 */
private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    var reqWidth = dpToPx(reqWidth)
    var reqHeight = dpToPx(reqHeight)

    if (height > reqHeight || width > reqWidth) {

        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

/**
 * Decode big image of a size of whole screen
 */
fun decodeSampledBitmapFromResource(res: Resources, resId: Int): Bitmap {

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(res, resId, options)

    // Get device's width and height
    val displayMetrics = resources.displayMetrics
    val reqHeight = (displayMetrics.heightPixels / displayMetrics.density).toInt()
    val reqWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

/**
 * Decode big image of a specific size
 */
fun decodeSampledBitmapFromResource(res: Resources, resId: Int,
                                    reqWidth: Int, reqHeight: Int): Bitmap {

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(res, resId, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

/**
 * Create file with image for further sending to the server
 */
var imageFilePath = ""

@Throws(IOException::class)
fun createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
    val imageFileName = "IMG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir)   /* directory */

    imageFilePath = image.absolutePath

    return image
}

fun getBytesFromBitmap(bitmap: Bitmap?, quality: Int): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

fun deletePhotoContent(imageUri: Uri?): Boolean {
    if (imageUri == null) return false
    val deleted = context.contentResolver.delete(imageUri, null, null)
    // deleted value represents number of deleted rows
    return deleted > 0
}
/**
 * Set background image when user hasn't provided one according to event category
 */
fun setBackgroundCategory(category: String, imageView: ImageView){
    when (category) {
        R.string.category.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_concert, 600, 200))
        R.string.free_time.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_free_time, 600, 200))
        R.string.school.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_school, 600, 200))
        R.string.party.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_party, 600, 200))
        R.string.restaurant.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_restaurant, 600, 200))
        R.string.bar.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_bar, 600, 200))
        R.string.sports.asString() -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_sports, 600, 200))
        else -> imageView.setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.bg_category_other, 600, 200))
    }
}