package com.malliina.boattracker

import android.app.Application
import com.malliina.boattracker.backend.BoatHttpClient
import com.malliina.boattracker.backend.GoogleTokenSource
import com.mapbox.mapboxsdk.Mapbox
import timber.log.Timber

class BoatApp : Application() {
    private lateinit var savedSettings: UserSettings
    val settings: UserSettings get() = savedSettings
    private lateinit var httpClient: BoatHttpClient
    val http: BoatHttpClient get() = httpClient

    override fun onCreate() {
        super.onCreate()
        val tree = if (BuildConfig.DEBUG) Timber.DebugTree() else NoLogging()
        Timber.plant(tree)

        val token = BuildConfig.MapboxAccessToken
        Timber.i("Using token %s", token)
        Mapbox.getTelemetry()?.setUserTelemetryRequestState(false)
        Mapbox.getInstance(applicationContext, token)

        savedSettings = UserSettings.load(applicationContext)
        // AppCenter.start(this, "768ec01e-fe9c-46b2-a05a-5389fa9d148f", Analytics::class.java, Crashes::class.java)
        httpClient = BoatHttpClient(GoogleTokenSource(applicationContext))
    }

    class NoLogging : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        }
    }
}
