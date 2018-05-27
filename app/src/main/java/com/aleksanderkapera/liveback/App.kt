package com.aleksanderkapera.liveback

import android.app.Application

class App: Application() {

    //providing context for whole app
    companion object {
        private lateinit var sInstance: App
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
    }
}