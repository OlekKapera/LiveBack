package com.aleksanderkapera.liveback.ui.base

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.Nullable
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.aleksanderkapera.liveback.R


abstract class BaseActivity: PermissionsAskingActivity() {

    abstract fun getLayoutRes(): Int

    override  fun onCreate(@Nullable savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())
        ButterKnife.bind(this)
        getExtras(intent)
    }

    protected fun getExtras(intent: Intent){}

    @Optional
    @OnClick(R.id.back)
    fun onBackClick() = onBackPressed()

    @Optional
    @OnClick(R.id.menu)
    fun onMenuClick(){
        //TODO make implementation for opening side menu
        print("mada")
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
