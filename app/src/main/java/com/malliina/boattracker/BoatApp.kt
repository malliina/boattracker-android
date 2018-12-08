package com.malliina.boattracker

import android.app.Application
import timber.log.Timber

class BoatApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val tree = if (BuildConfig.DEBUG) Timber.DebugTree() else NoLogging()
        Timber.plant(tree)
    }

    class NoLogging: Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        }
    }
}
