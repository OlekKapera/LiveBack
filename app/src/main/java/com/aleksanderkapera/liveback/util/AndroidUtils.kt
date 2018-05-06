package com.aleksanderkapera.liveback.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class AndroidUtils{

    companion object {
        fun isApiBelow(api: Int): Boolean {
            return Build.VERSION.SDK_INT < api
        }

        fun hideKeyboard(activity: Activity){
            val view = activity.currentFocus
            if(view != null){
                view.clearFocus()
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken,0)
            }
        }

        fun hideKeyboardOnTouchOutside(view: View, activity: Activity){
            if (view !is EditText){
                view.setOnTouchListener(View.OnTouchListener { _, _ ->
                    hideKeyboard(activity)
                    return@OnTouchListener false
                })
            }
            if (view is ViewGroup){
                for(i in 0..view.childCount){
                    val innerView = view.getChildAt(i)
                    hideKeyboardOnTouchOutside(innerView, activity)
                }
            }
        }
    }
}