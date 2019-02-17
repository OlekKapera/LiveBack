package com.aleksanderkapera.liveback.ui.widget

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.aleksanderkapera.liveback.R

/**
 * Created by kapera on 25-Jul-18.
 */
class LoadView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mFadeIn: Animation
    private var mFadeOut: Animation

    init {
        val rootView = View.inflate(context, R.layout.widget_loader, this)

        mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in)
        mFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out)

        mFadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {}
        })

        mFadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                visibility = View.GONE
            }
        })

        mFadeIn.duration = resources.getInteger(R.integer.animation_fade_duration).toLong()
        mFadeOut.duration = resources.getInteger(R.integer.animation_fade_duration).toLong()
    }

    fun show() {
        if (visibility == View.GONE)
            startAnimation(mFadeIn)
    }

    fun hide() {
        if (visibility == View.VISIBLE)
            startAnimation(mFadeOut)
    }
}