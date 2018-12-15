package com.aleksanderkapera.liveback.util

import android.graphics.PorterDuff
import android.support.v4.widget.CircularProgressDrawable
import com.aleksanderkapera.liveback.R
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideOption
import com.bumptech.glide.request.RequestOptions

/**
 * Glide Extension class containing loader
 */
@GlideExtension
object GlideExtensions {

    @GlideOption
    @JvmStatic
    fun displayPlaceholder(options: RequestOptions) {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN)
        circularProgressDrawable.start()

        options.placeholder(circularProgressDrawable)
    }

    @GlideOption
    @JvmStatic
    fun displayRoundPlaceholder(options: RequestOptions) {
        displayPlaceholder(options)

        options.circleCrop()
    }
}