package com.malliina.boattracker.ui.map

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.malliina.boattracker.BuildConfig
import com.malliina.boattracker.ProfileActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.malliina.boattracker.R
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.AppCenter

class MapActivity: AppCompatActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, BuildConfig.MapboxAccessToken)
        setContentView(R.layout.map_activity)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        AppCenter.start(application, "768ec01e-fe9c-46b2-a05a-5389fa9d148f",
            Analytics::class.java, Crashes::class.java)
    }

    fun profileClicked(button: View) {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "some data")
        }
        startActivity(intent)
    }

    fun locationClicked(button: View) {
        info("Location")
    }

    private fun info(text: String) {
        Log.i(localClassName, text)
    }
}
