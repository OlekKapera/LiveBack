package com.aleksanderkapera.liveback.ui.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import com.aleksanderkapera.liveback.R
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

/**
 * Activity implementation which handles fragment backstack implementation by its own. It's more simple and more
 * customizable and have more features compared to android's fragment manager backstack.
 * NOTE: Always set your activity container as "container"
 */
abstract class FragmentActivity: BaseActivity() {

    interface OnBackPressListener {
        /**
         * Returns true if event has been consumed by listener
         * and should be not delegated to activity.
         */
        fun onBackPressed(): Boolean
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentBackStack = LinkedList()

        if(savedInstanceState == null){
            putDefaultFragment()
        } else {
            val frags = savedInstanceState.getStringArrayList(ARG_SAVE_FRAGMENTS)

            if(frags.isNotEmpty()){
                fragmentBackStack.clear()
                val transaction = supportFragmentManager.beginTransaction()
                for(fragmentString: String in frags){
                    val fragment = supportFragmentManager.findFragmentByTag(fragmentString)
                    if(fragment != null) transaction.hide(fragment)
                    fragmentBackStack.add(fragment as BaseFragment)
                }
                transaction.show(fragmentBackStack.last)
                transaction.commit()
            }
        }
    }

    override fun onBackPressed() {
        //show previous fragment if there's some
        // if topFragment is null then user pressed back multiple times too fast after each other and
        // activity is trying to get non existing item from the list
        val topFragment = getLastFragment() ?: return


        // do not allow back button if fragment does not allow it.
        if(!topFragment.allowBackButton())return

        if(topFragment is OnBackPressListener){
            val consumed = (topFragment as OnBackPressListener).onBackPressed()
            if(consumed)return
        }

        popFragment()

        // finish activity if no fragment in backstack
        if(fragmentBackStack.isEmpty()) finish()
    }

    //region FRAGMENTS
    protected fun putDefaultFragment() {putFragment(getDefaultFragment(),true)}

    protected abstract fun getDefaultFragment(): BaseFragment

    private lateinit var fragmentBackStack: LinkedList<BaseFragment>

    /**
     * Puts fragment on top of fragment content view
     *
     * @param fragment       fragment to show
     * @param clearBackStack clear fragment back stack
     */
    fun putFragment(fragment: BaseFragment, clearBackStack: Boolean, removeLast: Boolean = false){
        val transaction = supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation())

        try {
            val top = fragmentBackStack.last
            if(removeLast){
                fragmentBackStack.remove(top)
                transaction.remove(top)
            } else {
                transaction.hide(top)
            }
        } catch (e: NoSuchElementException){
            // no element in stack
        }

        //clear backstack if needed
        if(clearBackStack){
            for (f: Fragment in fragmentBackStack){
                transaction.remove(f)
            }
            fragmentBackStack.clear()
        }

        transaction.add(R.id.container, fragment, fragment.javaClass.name)
        fragmentBackStack.add(fragment)

        transaction.commit()
    }

    fun getLastFragment(): BaseFragment?{
        return try {
            fragmentBackStack.last
        } catch (e: NoSuchElementException){
            e.printStackTrace()
            null
        }
    }

    override  fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val fragments = ArrayList<String>()
        for(f: Fragment in fragmentBackStack){
            fragments.add(f.javaClass.name)
        }
        outState.putStringArrayList(ARG_SAVE_FRAGMENTS, fragments)
    }

    companion object {
        private const val ARG_SAVE_FRAGMENTS = "saved_fragments"
    }

    fun popFragment(){
        try{
            // fragment on top - remove this fragment
            val top = fragmentBackStack.last
            fragmentBackStack.removeLast()

            // second fragment, show this one
            val toShow = fragmentBackStack.last

            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(top.getEnterAnimation(), top.getExitAnimation())
                    .remove(top)
                    .show(toShow).commit()
        } catch (e: NoSuchElementException){
            // no element in stack
        }
    }

    fun showTopFragment(){
        try {
            supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .show(fragmentBackStack.last)
                    .commit()
        } catch (e: NoSuchElementException){
            e.printStackTrace()
        }
    }

    override fun showFragment(fragment: BaseFragment) {
        val toShow: BaseFragment? = supportFragmentManager.findFragmentByTag(fragment.javaClass.name) as BaseFragment

        if (toShow != null) {
            supportFragmentManager
                    .beginTransaction()
                    .hide(fragmentBackStack.last)
                    .show(toShow)
                    .commit()
            fragmentBackStack.removeAt(fragmentBackStack.indexOf(toShow))
            fragmentBackStack.add(toShow)
        } else {
            putFragment(fragment, false)
        }
    }

    fun clearFragmentBackStack() =  fragmentBackStack.clear()
    //endregion
}