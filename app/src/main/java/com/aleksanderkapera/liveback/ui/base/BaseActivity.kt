package com.aleksanderkapera.liveback.ui.base

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.Nullable
import butterknife.ButterKnife


abstract class BaseActivity : PermissionsAskingActivity() {

    abstract fun getLayoutRes(): Int

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())
        ButterKnife.bind(this)
        getExtras(intent)
    }

    protected fun getExtras(intent: Intent) {}

    override fun onSupportNavigateUp(): Boolean {
        //TODO make implementation for opening side menu
        return true
    }

    /**
     * Add fragment into layout.
     *
     * @param fragment fragment to insert
     * @param res      layout id which will host fragment
     */
    fun addFragmentInto(fragment: BaseFragment, @IdRes res: Int) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation())
                .add(res, fragment, fragment.javaClass.simpleName)
                .commit()
    }

    open fun showFragment(fragment: BaseFragment) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation())
                .show(fragment)
                .commit()
    }
}