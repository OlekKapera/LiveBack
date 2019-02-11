package com.aleksanderkapera.liveback.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aleksanderkapera.liveback.R
import kotlinx.android.synthetic.main.widget_empty_screen.view.*


/**
 * View for empty recycler.
 */
class EmptyScreenView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.widget_empty_screen, this)

        attrs?.let {
            val a = resources.obtainAttributes(it, R.styleable.EmptyScreenView)

            val message = a.getString(R.styleable.EmptyScreenView_message)
            val color = a.getColor(R.styleable.EmptyScreenView_messageColor, resources.getColor(R.color.darkGrey))

            // set message text
            message?.let {
                emptyScreen_text_message.text = it
                emptyScreen_text_message.setTextColor(color)
            }

            a.recycle()
        }
    }
}