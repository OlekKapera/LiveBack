package com.aleksanderkapera.liveback

import android.app.Application
import android.content.Context

class App: Application() {

    //providing context for whole app
    companion object {
        private lateinit var sInstance: App

        fun applicationContext() : Context {
            return sInstance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
    }
}