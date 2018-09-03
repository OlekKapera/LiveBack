package com.aleksanderkapera.liveback.util

import android.support.v7.widget.SearchView
import io.reactivex.subjects.PublishSubject

/**
 * Class for implementing optimized searching via RxJava
 */
object RxSearchObservable {

    fun fromView(searchView: SearchView): PublishSubject<String> {
        val subject: PublishSubject<String> = PublishSubject.create()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                subject.onComplete()
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                text?.let { subject.onNext(it) }
                return true
            }
        })

        return subject
    }
}