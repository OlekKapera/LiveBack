package com.aleksanderkapera.liveback.ui.base

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.Nullable
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.aleksanderkapera.liveback.R


abstract class BaseActivity: PermissionsAskingActivity() {

    // @format:off
    @BindView(R.id.toolbar) lateinit var mToolbar: Toolbar
    // @format:on

    abstract fun getLayoutRes(): Int

    override  fun onCreate(@Nullable savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())
        ButterKnife.bind(this)
        getExtras(intent)

        // set toolbar
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_menu))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    protected fun getExtras(intent: Intent){}

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
    fun addFragmentInto(fragment: BaseFragment, @IdRes res: Int){
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation())
                .add(res, fragment, fragment.javaClass.simpleName)
                .commit()
    }

    open fun showFragment(fragment: BaseFragment){
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(fragment.getEnterAnimation(), fragment.getExitAnimation())
                .show(fragment)
                .commit()
    }
}
