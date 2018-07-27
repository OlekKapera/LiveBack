package com.aleksanderkapera.liveback.ui.widget

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aleksanderkapera.liveback.R
import kotlinx.android.synthetic.main.item_event_about.view.*

/**
 * Created by kapera on 27-Jul-18.
 */
class EventAboutItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    init {
        val rootView = View.inflate(context, R.layout.item_event_about, this)

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.EventAboutItem)

            val icon = a.getDrawable(R.styleable.EventAboutItem_eventItemIcon)
            val title = a.getString(R.styleable.EventAboutItem_eventItemTitle)
            val description = a.getString(R.styleable.EventAboutItem_description)

            rootView.eventItem_image_icon.setImageDrawable(icon)
            rootView.eventItem_text_title.text = title
            rootView.eventItem_text_description.text = description

            a.recycle()
        }
    }
}