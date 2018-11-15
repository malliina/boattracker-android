package com.malliina.boattracker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.malliina.boattracker.ui.main.MainFragment
import com.mapbox.mapboxsdk.maps.MapView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

}
