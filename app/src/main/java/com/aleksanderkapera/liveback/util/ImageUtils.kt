package com.aleksanderkapera.liveback.util

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.DisplayMetrics
import com.aleksanderkapera.liveback.util.AndroidUtils.Companion.getResources


/**
 * Created by kapera on 24-Jun-18.
 */
class ImageUtils {

    companion object {
        private fun calculateInSampleSize(
                options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            var reqWidth = AndroidUtils.dpToPx(reqWidth)
            var reqHeight = AndroidUtils.dpToPx(reqHeight)

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
            val displayMetrics = getResources().displayMetrics
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
    }
}