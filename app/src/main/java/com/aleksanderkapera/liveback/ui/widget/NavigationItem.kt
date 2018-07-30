package com.aleksanderkapera.liveback.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.RequiresApi
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import butterknife.ButterKnife
import com.aleksanderkapera.liveback.R
import kotlinx.android.synthetic.main.navigation_item.view.*

/**
 * Created by kapera on 23-May-18.
 */
class NavigationItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes){

    init {
        View.inflate(context, R.layout.navigation_item, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NavigationItem)

            val title = a.getString(R.styleable.NavigationItem_title)
            val icon = a.getDrawable(R.styleable.NavigationItem_icon)

            navigation_item_text.text = title
            navigation_item_image.setImageDrawable(icon)

            a.recycle()
        }
    }
}