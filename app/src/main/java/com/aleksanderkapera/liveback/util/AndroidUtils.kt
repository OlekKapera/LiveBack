package com.aleksanderkapera.liveback.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.aleksanderkapera.liveback.App
import java.util.*


val context = App.context
val resources = context.resources

fun isApiBelow(api: Int): Boolean {
    return Build.VERSION.SDK_INT < api
}

fun Int.asColor() = ContextCompat.getColor(context, this)

fun Int.asDimen() = resources.getDimension(this)

fun Int.asDrawable() = ContextCompat.getDrawable(context, this)

fun Int.asString() = context.getString(this)

fun Int.asStringArray() = resources.getStringArray(this)

fun Int.asPluralsString(number: Int) = resources.getQuantityString(this, number, number)

fun Int.asFont() = ResourcesCompat.getFont(context, this)

fun hideKeyboard(activity: Activity) {
    val view = activity.currentFocus
    if (view != null) {
        view.clearFocus()
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun hideKeyboardOnTouchOutside(view: View, activity: Activity) {
    if (view !is EditText) {
        view.setOnTouchListener(View.OnTouchListener { _, _ ->
            hideKeyboard(activity)
            return@OnTouchListener false
        })
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            hideKeyboardOnTouchOutside(innerView, activity)
        }
    }
}

fun dpToPx(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return (dp * displayMetrics.density + 0.5).toInt()
}

fun getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0)
        result = resources.getDimensionPixelSize(resourceId)

    return result
}

fun getNavigationBarHeight(): Int {
    val resources = resources
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}

/**
 * Moves toolbar from top of the screen under the status bar.
 */
fun setToolbarMargin(view: View) {
    val params = view.layoutParams as CoordinatorLayout.LayoutParams
    params.setMargins(0, getStatusBarHeight(), 0, 0)
    view.layoutParams = params
}

//section returns only null checked vars
fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4) -> R?): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}
//end section

/**
 * Return localized resources
 */
fun getLocalizedResources(context: Context, desiredLocale: Locale): Resources {
    var conf = context.resources.configuration
    conf = Configuration(conf)
    conf.setLocale(desiredLocale)
    val localizedContext = context.createConfigurationContext(conf)
    return localizedContext.resources
}

/**
 * Translate slovak categories to english
 */
fun translateCategories(slovak: String, english: String): String {
    if (slovak.isNotEmpty()) {
        return when (slovak) {
            "Koncert" -> "Concert"
            "Voľný čas" -> "Free Time"
            "Škola" -> "School"
            "Párty" -> "Party"
            "Reštaurácia" -> "Restaurant"
            "Bar" -> "Bar"
            "Športy" -> "Sports"
            else -> "Other"
        }
    }

    if (english.isNotEmpty()) {
        return when (english) {
            "Concert" -> "Koncert"
            "Free Time" -> "Voľný čas"
            "School" -> "Škola"
            "Party" -> "Párty"
            "Restaurant" -> "Reštaurácia"
            "Bar" -> "Bar"
            "Sports" -> "Športy"
            else -> "Iné"
        }
    }

    return ""
}