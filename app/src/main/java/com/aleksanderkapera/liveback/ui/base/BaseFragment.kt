package com.aleksanderkapera.liveback.ui.base

import android.os.Bundle
import android.support.annotation.AnimRes
import android.support.annotation.LayoutRes
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.util.AndroidUtils

abstract class BaseFragment: Fragment() {

    lateinit var appCompatActivity:AppCompatActivity

    /**
     * Returns root layout resource for this fragment.
     */
    @LayoutRes
    abstract fun getLayoutRes(): Int

    /**
     * Place to setup all views. This is called within {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * after inflating layout provided by {@link BaseFragment#getLayoutRes()} and binding views using ButterKnife.
     *
     * @param rootView root view of this fragment, which is not attached to the fragment just yet.
     */
    abstract fun setupViews(rootView: View)

    override fun onCreate(@Nullable savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        if(arguments!=null)
            getExtras(arguments)
    }

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(getLayoutRes(), container, false)
        ButterKnife.bind(this, rootView)

        // set up keyboard hiding on this fragment if it's necessary. Don't forget to implement
        // hideKeyboardOnTouchOutside() method if you need different behavior.
        if(hideKeyboardOnTouchOutside())
            AndroidUtils.hideKeyboardOnTouchOutside(rootView, activity!!)

        appCompatActivity = activity as AppCompatActivity

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Bind data to views
        setupViews(view)
    }

    /**
     * Called during onCreate if needs to resolve arguments. If arguments == null method is not called.
     *
     * @param args bundle passed to this fragment
     */
    fun getExtras(args: Bundle?){}

    /**
     * Returns true if keyboard has to be dismissed when tapped outside of EditText in this fragment.
     */
    fun hideKeyboardOnTouchOutside(): Boolean { return true}

    @Optional
    @OnClick(R.id.back)
    fun onBack() = activity!!.onBackPressed()

    /**
     * Safe version of displaying toast. If fragment host is null
     * (fragment is not attached to activity) toast is not displayed.
     *
     * @param message Test ot be displayed.
     */
    fun showToast(message: String){
        activity?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    /**
     * Convenience method for {@link BaseFragment#showToast(String)}
     *
     * @param stringResource string resource to display in toast message.
     */
    fun showToast(stringResource: Int){
        activity?.let{ showToast(it.getString(stringResource))}
    }

    /**
     * Tells activity if back button is allowed for this fragment. This is meant to be called within
     * {@link FragmentActivity#onBackPressed()} and if this is true, then pop this fragment or do nothing
     * if back is not allowed.
     */
    fun allowBackButton(): Boolean { return true}

    /**
     * Returns default entering animation resource for this fragment. Animation is played when fragment
     * is added to fragment manager. Animation can be changed by overriding this method.
     */
    @AnimRes
    fun getEnterAnimation(): Int { return 0}

    /**
     * Returns default exit animation resource for this fragment. Animation is played when fragment
     * is added to fragment manager. Animation can be changed by overriding this method.
     */
    @AnimRes
    fun getExitAnimation(): Int { return 0}

}
