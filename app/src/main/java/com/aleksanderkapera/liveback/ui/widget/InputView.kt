package com.aleksanderkapera.liveback.ui.widget

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aleksanderkapera.liveback.R
import kotlinx.android.synthetic.main.widget_input.view.*

/**
 * Created by kapera on 30-Jul-18.
 */
class InputView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    init {
        View.inflate(context, R.layout.widget_input, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.InputView)

            val icon = a.getDrawable(R.styleable.InputView_inputIcon)
            val hint = a.getString(R.styleable.InputView_inputHint)
            val editable = a.getBoolean(R.styleable.InputView_editable, true)

            input_image_icon.setImageDrawable(icon)
            input_layout_text.hint = hint

            if (!editable)
                input_input_text.isFocusable = editable
        }
    }
}