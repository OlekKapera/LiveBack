package com.aleksanderkapera.liveback

import android.app.Application

class App : Application() {

    //providing context for whole app
    companion object {
        lateinit var context: App
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}