package com.aleksanderkapera.liveback.ui.fragment

import android.animation.Animator
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.ActionBar
import android.view.View
import com.aleksanderkapera.liveback.R
import com.aleksanderkapera.liveback.ui.base.BaseFragment
import com.aleksanderkapera.liveback.util.asString
import com.aleksanderkapera.liveback.util.dpToPx
import com.aleksanderkapera.liveback.util.getStatusBarHeight
import kotlinx.android.synthetic.main.app_bar_event.*
import kotlinx.android.synthetic.main.fragment_event.*
import java.util.logging.Handler


/**
 * Created by kapera on 27-Jul-18.
 */
class EventFragment : BaseFragment() {

    private val mAboutString = R.string.about.asString()
    private val mCommentsString = R.string.comments.asString()
    private val mVoteString = R.string.vote.asString()

    var isFaded = false
    var isAnimating = false
    var currentOffset = 0


    companion object {
        fun newInstance(): BaseFragment = EventFragment()
    }

    override fun getLayoutRes(): Int = R.layout.fragment_event

    override fun setupViews(rootView: View) {
        // set toolbar
        appCompatActivity.setSupportActionBar(event_layout_toolbar)
        val actionbar: ActionBar? = appCompatActivity.supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        // move only toolbar below status bar
        val toolbarParams = event_layout_toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        toolbarParams.setMargins(0, getStatusBarHeight(), 0, 0)
        event_layout_toolbar.layoutParams = toolbarParams

        setToolbarAnimation()
        setupTabs()
    }

    /**
     * Set toolbar animation
     */
    private fun setToolbarAnimation() {
        val view = event_layout_header
        val breakPoint = -dpToPx(48)

        event_layout_appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            currentOffset = verticalOffset

//            val handler = Handler().

            if (!isAnimating) {
                if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange > breakPoint && !isFaded) {
                    //Collapsed
                    fadeIn(view, verticalOffset)
                } else if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange < (breakPoint - 20) && isFaded) {
                    //Expanded
                    fadeOut(view, verticalOffset)
                }
            }
        }
    }

    /**
     * Fade in a view
     */
    private fun fadeIn(view: View, offset: Int) {
        view.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    // @format:off
                        override fun onAnimationRepeat(p0: Animator?) {}
                        override fun onAnimationEnd(p0: Animator?) {
                            isFaded = true
                            isAnimating = false

//                            if(currentOffset != offset)
//                                fadeOut(view, currentOffset)
                        }
                        override fun onAnimationCancel(p0: Animator?) {}
                        override fun onAnimationStart(p0: Animator?) {
                            isAnimating = true
                        }
                        // @format:on
                })
    }

    /**
     * Fade in a view
     */
    private fun fadeOut(view: View, offset: Int) {
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(object : Animator.AnimatorListener {
                    // @format:off
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        isFaded = false
                        isAnimating = false

//                        if(currentOffset != offset)
//                            fadeIn(view, currentOffset)
                    }
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {
                        isAnimating = true
                    }
                    // @format:on
                })
    }

    /**
     * Setup tab layout and view pager
     */
    private fun setupTabs() {
        val adapter = ViewPagerAdapter(appCompatActivity.supportFragmentManager)
        adapter.addFragment(EventAboutFragment.newInstance(), mAboutString)
        adapter.addFragment(EventCommentsFragment.newInstance(), mCommentsString)
        adapter.addFragment(EventVoteFragment.newInstance(), mVoteString)
        event_layout_viewPager.adapter = adapter

        event_layout_tabs.setupWithViewPager(event_layout_viewPager)
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val mFragmentList = mutableListOf<BaseFragment>()
        private val mFragmentListTitle = mutableListOf<String>()

        override fun getItem(position: Int): Fragment = mFragmentList[position]

        override fun getCount(): Int = mFragmentList.size

        override fun getPageTitle(position: Int): CharSequence? = mFragmentListTitle[position]

        public fun addFragment(fragment: BaseFragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentListTitle.add(title)
        }
    }
}