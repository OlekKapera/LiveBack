package com.aleksanderkapera.liveback.ui.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import butterknife.ButterKnife
import com.aleksanderkapera.liveback.R

/**
 * Created by kapera on 23-May-18.
 */
class NavigationItem(context: Context, ) : FrameLayout {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        View.inflate(context, R.layout.navigation_item, this)
        ButterKnife.bind(this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NavigationItem)

            val title = a.getString(R.styleable.NavigationItem_title)
            val icon = a.getDrawable(R.styleable.NavigationItem_icon)
            val active = a.getBoolean(R.styleable.NavigationItem_active, false)

            mTitle.setText(title)
            mIcon.setImageDrawable(icon)
            setActive(active)

            a.recycle()
        }
    }
}